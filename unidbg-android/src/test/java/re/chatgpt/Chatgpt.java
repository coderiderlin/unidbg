package re.chatgpt;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.TraceHook;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import re.util.LogUtil;
import re.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;

public class Chatgpt extends AbstractJni {
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
        vm.setVerbose(true);


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
        vm.setJni(this);
        // 调用JNI方法
//        emulator.traceCode(dmFekit.getModule().base,dmFekit.getModule().base+dmFekit.getModule().size);

    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {

        LogUtil.i("callStaticObjectMethod", String.format("%s %s", signature, varArg));
        switch (signature) {
//            case "android/os/SystemProperties->get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;":
//                System.out.println("android/os/SystemProperties->get " + varArg);
//                return new StringObject(vm, "705KPGS001091");
//            case "android/app/ActivityThread-&gt;currentActivityThread()Landroid/app/ActivityThread;":
//                return vm.resolveClass("android/app/ActivityThread").newObject(null);
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public int callIntMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {

        String dvmObjectStr = dvmObject.toString();
        if (dvmObject.getValue() instanceof String) {
            dvmObjectStr = (String) dvmObject.getValue();
        }
        LogUtil.i("callIntMethod", String.format("%s %s %s", signature, dvmObjectStr, vaList.toString()));

        switch (signature) {
            case "java/lang/String->hashCode()I": {
                String str = (String) dvmObject.getValue();
                int res = str.hashCode();
                LogUtil.i(TAG, String.format("hashcode of %s is %d", str, res));
                return res;
            }

        }
        return super.callIntMethodV(vm, dvmObject, signature, vaList);
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
            me.callJNI_OnLoad();
//            waitCtrlC();
        } catch (Exception ex) {
            LogUtil.e(TAG, "error on main:" + LogUtil.getStackTraceString(ex));
        }
    }
}
