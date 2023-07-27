package re.qqsdk;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.AbstractJni;
import com.github.unidbg.linux.android.dvm.Array;
import com.github.unidbg.linux.android.dvm.DalvikModule;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;
import re.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class Fekit extends AbstractJni {
    private static final String TAG = Fekit.class.getSimpleName();
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module modFekit;

    Fekit(){
        emulator = AndroidEmulatorBuilder
                .for64Bit()
                .addBackendFactory(new Unicorn2Factory(true))
                .setProcessName("com.tencent.mobileqq")
                .build();
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机,传入APK,Unidbg可以替我们做部分签名校验的工作
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/resources/re/qqsdk/com.tencent.mobileqq_v8.9.70.apk"));
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

        DalvikModule dmFekit = vm.loadLibrary(new File("unidbg-android/src/test/resources/re/qqsdk/libfekit.so"),false);
        modFekit = dmFekit.getModule();
        vm.setJni(this);
        // 调用JNI方法
//        emulator.traceCode(dmFekit.getModule().base,dmFekit.getModule().base+dmFekit.getModule().size);



        int func_start = 0x62880;
        int func_end = 0x75074;

        try {
//            emulator.traceCode(dmFekit.getModule().base+func_start,dmFekit.getModule().base+func_end)
//            emulator.traceCode(dmFekit.getModule().base,dmFekit.getModule().base+dmFekit.getModule().size)
        emulator.traceCode()
                .setRedirect(new PrintStream(new FileOutputStream("unidbg-android/src/test/resources/re/qqsdk/trace1.log"),true));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        dmFekit.callJNI_OnLoad(emulator);;   // 调用JNI_OnLoad

    }

    public static void main(String[] args){
        Fekit fekit = new Fekit();
    }
}
