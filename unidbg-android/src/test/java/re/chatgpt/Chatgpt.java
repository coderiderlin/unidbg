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
import com.github.unidbg.pointer.UnidbgPointer;
import re.util.LogUtil;
import re.util.Utils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chatgpt {
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

        dm = vm.loadLibrary(new File("unidbg-android/src/test/resources/re/chatgpt/ChatGPT_1.2023.284/config.arm64_v8a/lib/arm64-v8a/libpairipcore.so"), false);
        mod = dm.getModule();
        // 调用JNI方法
//        emulator.traceCode(dmFekit.getModule().base,dmFekit.getModule().base+dmFekit.getModule().size);

    }


    public void call_StartupLauncher() {
        //VMRunner.invoke("R2tKgXCxJ05Y6MgT", null);
//        TraceHook hook = vm.getEmulator().traceCode();
        VMRunnerInvoke("gBLSES2bZk2l9pzz", null); //1.2023.284
//        VMRunnerInvoke("R2tKgXCxJ05Y6MgT", null); //1.2023.281
//        hook.stopTrace();
    }

    public Object VMRunnerInvoke(String funcStr, Object[] args) {
        File ff = new File("unidbg-android/src/test/resources/re/chatgpt/vm/1.2023.284/" + funcStr);
        try {
            byte[] byteCode = Files.readAllBytes(ff.toPath());
            return executeVM(byteCode, args);
        } catch (IOException e) {
            LogUtil.e(TAG, "load func file error:" + e.getMessage());
        }
        return null;
    }

    public Object executeVM(byte[] byteCode, Object[] arg) {

        DvmClass VmRunner = vm.resolveClass("com.pairip.VMRunner");
        return VmRunner.callStaticJniMethodObject(emulator, "executeVM([B[Ljava/lang/Object;)Ljava/lang/Object;", byteCode, arg);
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
        PrintStream streamTrace = null;
        try {
            streamTrace = new PrintStream(new FileOutputStream("unidbg-android/src/test/resources/re/chatgpt/trace.log", true));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        try {
            Chatgpt me = new Chatgpt();


            Debugger dbg = me.emulator.attach();

            Dobby dobby = Dobby.getInstance(me.emulator);
//            long offset_decStdStr=0x3f300; //1.2023.281
            long offset_decStdStr = 0x3f274; //1.2023.284
//            dbg.addBreakPoint(me.mod.base+0x3F344);
            dobby.replace(me.mod.base + offset_decStdStr, new ReplaceCallback() { // decStdString
                @Override
                public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
                    UnidbgPointer outStdStr =context.getPointerArg(0);
                    context.push(outStdStr);
                    return HookStatus.RET(emulator, originFunction);
                }

                @Override
                public void postCall(Emulator<?> emulator, HookContext context) {
                    UnidbgPointer outStdStr=context.pop();
                    int mode=outStdStr.getByte(0)&1; //1 denote std::string in long mode else in short mode
                    String str="";
                    if(mode==1)str=outStdStr.getPointer(8*2).getString(0);
                    else str=outStdStr.getString(1);

                    LogUtil.i(TAG, String.format("dec std::string -> mod:%d %s", mode,str));
                }
            }, true);


            dobby.replace(me.mod.base + 0x13A10, new ReplaceCallback() { // def_syscall
                @Override
                public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
                    LogUtil.i(TAG, "on def_syscall");
                    return HookStatus.RET(emulator, originFunction);
                }
            }, false);


            dobby.replace(me.mod.base + 0x4077c, new ReplaceCallback() { // guess_anti
                @Override
                public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
                    LogUtil.i(TAG, "on guess_anti");
                    return HookStatus.RET(emulator, originFunction);
                }
            }, false);


            dobby.replace(me.mod.base + 0x4AC6c, new ReplaceCallback() { // FindClass err
                @Override
                public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
                    LogUtil.i(TAG, String.format("on FindClass err x8:%08x", context.getLongByReg(8)));
                    return HookStatus.RET(emulator, originFunction);
                }
            }, false);

            me.callJNI_OnLoad();

            LogUtil.e(TAG, "calling startupLauncher..");
//            dbg.addBreakPoint(me.mod,0x364c0);
//            dbg.addBreakPoint(me.mod,0x3e8fc);

// Trace range
            List<List<Long>> ranges = Arrays.asList(
                    Arrays.asList(0x3F274L, 0x3F364L - 4L) // executeVM
            );

            TraceHook traceHook = me.emulator.traceCode();
            traceHook.setRedirect(streamTrace);
//
//            List<TraceHook> traceHooks = new ArrayList<>();
//            for (List<Long> range : ranges) {
////                TraceHook traceHook = me.emulator.traceCode(me.mod.base + range.get(0), me.mod.base + range.get(1));
////                traceHook.setRedirect(streamTrace);
//                traceHooks.add(traceHook);
//            }


            me.call_StartupLauncher();

            LogUtil.e(TAG,"test end.");
//            waitCtrlC();
        } catch (Exception ex) {
            LogUtil.e(TAG, "error on main:" + LogUtil.getStackTraceString(ex));
        }

        streamTrace.flush();
        streamTrace.close();
    }
}
