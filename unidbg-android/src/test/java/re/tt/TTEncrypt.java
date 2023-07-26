package re.tt;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import re.util.Utils;

import java.io.File;
import java.io.IOException;

public class TTEncrypt extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private final DvmClass TTEncryptUtils;

    public String apkPath = "unidbg-android/src/test/resources/re/tt/jrtt_742.apk";

    TTEncrypt() {
        emulator = AndroidEmulatorBuilder.for32Bit().build();
        final Memory memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));
//        vm = emulator.createDalvikVM(new File(apkPath));
        vm = emulator.createDalvikVM();
//        DalvikModule dm = vm.loadLibrary("ttEncrypt", true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/resources/re/tt/libttEncrypt.so"), false); // 加载libttEncrypt.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数

        module = dm.getModule();
        emulator.traceCode();
        vm.setVerbose(true);
        vm.setJni(this);
//        dm.callJNI_OnLoad(emulator);

        TTEncryptUtils = vm.resolveClass("com/bytedance/frameworks/core/encrypt/TTEncryptUtils");
        dm.callJNI_OnLoad(emulator);
        System.out.println("TTEncryptUtils:"+TTEncryptUtils);
    }

    public void call_ttEncrypt() {
        String input = "test";

        ByteArray result = TTEncryptUtils.callStaticJniMethodObject(
                emulator, "ttEncrypt([BI)[B",
                new ByteArray(vm, input.getBytes()),
                input.length()
        );

        System.out.printf("input:%s\n",Utils.bytesToHexString(input.getBytes()));
        System.out.printf("result:%s\n",Utils.bytesToHexString(result.getValue()));
    }

    public static void main(String[] args) {
        TTEncrypt ttEncrypt = new TTEncrypt();
//        ttEncrypt.call_ttEncrypt();

        ttEncrypt.destroy();
    }

    private void destroy() {
        try {
            emulator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}