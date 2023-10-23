package re.chatgpt;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.TraceHook;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.hook.HookContext;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.hookzz.Dobby;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import re.util.LogUtil;
import re.util.Utils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;

public class Chatgpt  {
    private static final String TAG = Chatgpt.class.getSimpleName();
    public static final String TRACE_OUTPUT_PATH = "unidbg-android/src/test/resources/re/chatgpt/";
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module mod;
    DalvikModule dm;
    PrintStream traceStream = null;
    private TraceHook traceHook = null;


    Chatgpt() {
        emulator = AndroidEmulatorBuilder
                .for64Bit()
                .addBackendFactory(new Unicorn2Factory(true))
                .setProcessName("com.openai.chatgpt")
                .build();
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机,传入APK,Unidbg可以替我们做部分签名校验的工作
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/resources/re/chatgpt/ChatGPT_1.2023.284/com.openai.chatgpt.apk"));
        vm.setJni(new JniHandler());
        emulator.getSyscallHandler().addIOResolver(new FileHandler());
        vm.setVerbose(true);
        emulator.getSyscallHandler().setVerbose(true);



//        String libDir="unidbg-android/src/test/resources/re/sdk29/lib64";
//        List<String> soList = Arrays.asList(
////                "libc.so",
//                "libm.so",
//                "ld-android.so",
//                "liblog.so",
//                "liblog.so",
//                "libdl.so"
//        );
//        for(String so:soList)
//        {
//            String soPath=libDir+"/"+so;
//            LogUtil.i(TAG,"load so:"+soPath);
//            vm.loadLibrary(new File(soPath),false);
//        }

        dm = vm.loadLibrary(new File("unidbg-android/src/test/resources/re/chatgpt/config.arm64_v8a/lib/arm64-v8a/libpairipcore.so"), false);
        mod = dm.getModule();
        // 调用JNI方法
//        emulator.traceCode(dmFekit.getModule().base,dmFekit.getModule().base+dmFekit.getModule().size);

    }


    public void call_StartupLauncher()
    {
        //VMRunner.invoke("R2tKgXCxJ05Y6MgT", null);
//        TraceHook hook = vm.getEmulator().traceCode();
        VMRunnerInvoke("R2tKgXCxJ05Y6MgT", null);
//        hook.stopTrace();
    }

    public Object VMRunnerInvoke(String funcStr, Object[] args) {
        File ff=new File("unidbg-android/src/test/resources/re/chatgpt/vm/"+funcStr);
        try {
            byte[] byteCode = Files.readAllBytes(ff.toPath());
            return executeVM(byteCode,args);
        } catch (IOException e) {
            LogUtil.e(TAG,"load func file error:"+e.getMessage());
        }
        return null;
    }
    public Object executeVM(byte[] byteCode, Object[] arg){

        DvmClass VmRunner = vm.resolveClass("com.pairip.VMRunner");
        return VmRunner.callStaticJniMethodObject(emulator,"executeVM([B[Ljava/lang/Object;)Ljava/lang/Object;",byteCode,arg);
//        LogUtil.i(TAG,"executeVm with code size:"+byteCode.length);
//        List<Object> list = new ArrayList<>();
//        list.add(vm.getJavaVM());
//        list.add(0);
//
//        list.add(byteCode);
//        list.add(arg);
//
//        // 参数准备完成
//        // call function
//        Number number = mod.callFunction(emulator,
//                0x521cc,
//                list.toArray());
//        LogUtil.i(TAG,"executeVm ret object hash:"+number.intValue());
//        if(number.intValue()<0)
//        {
//            return null;
//        }
//        Object result= vm.getObject(number.intValue()).getValue();
//        return result;
    }
    public void callJNI_OnLoad() {

        startTrace("trace_jniOnload");

//        emulator.traceCode();

        try {
            dm.callJNI_OnLoad(emulator);
        } catch (Exception ex) {
            LogUtil.e(TAG, LogUtil.getStackTraceString(ex));
        }
        traceHook.stopTrace();
    }

    private void startTrace(String traceName) {
        try {
            traceStream = new PrintStream(new FileOutputStream(TRACE_OUTPUT_PATH + traceName), false);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

//            emulator.traceCode(dmFekit.getModule().base+func_start,dmFekit.getModule().base+func_end)
//            emulator.traceCode(dmFekit.getModule().base,dmFekit.getModule().base+dmFekit.getModule().size)
        traceHook = emulator.traceCode();
        traceHook.setRedirect(traceStream);
    }

    static void waitCtrlC() {
        LogUtil.i(TAG, String.format("pid:%s,exec done.press ctrl-c to exit.", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]));
        while (true) {
            Utils.sleep(3000);
        }
    }

    public static void main(String[] args) {
        try {
            Chatgpt me = new Chatgpt();

            Dobby dobby = Dobby.getInstance(me.emulator);
            dobby.replace(me.mod.base+0x3f300, new ReplaceCallback() { // decStdString
                //                @Override
//                public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
////                    System.out.println("decStdString.onCall arg0=" + context.getIntArg(0));
//                    return HookStatus.RET(emulator, originFunction);
//                }
                @Override
                public void postCall(Emulator<?> emulator, HookContext context) {
                    LogUtil.i(TAG,String.format("decStdString at %08X :%s",context.getLR(), context.getPointerArg(0).getPointer(0).getString(0)));
                }
            }, true);


            me.callJNI_OnLoad();

            LogUtil.e(TAG,"calling startupLauncher..");
            Debugger dbg = me.emulator.attach();
//            dbg.addBreakPoint(me.mod,0x364c0);
//            dbg.addBreakPoint(me.mod,0x3e8fc);



            me.call_StartupLauncher();
//            waitCtrlC();
        } catch (Exception ex) {
            LogUtil.e(TAG, "error on main:" + LogUtil.getStackTraceString(ex));
        }
    }
}
