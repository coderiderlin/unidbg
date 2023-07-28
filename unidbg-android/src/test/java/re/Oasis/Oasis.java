package re.Oasis;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.AbstractJni;
import com.github.unidbg.linux.android.dvm.DalvikModule;
import com.github.unidbg.linux.android.dvm.DalvikVM;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Oasis extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    Oasis(){
        // 创建一个模拟器实例,进程名建议依照实际的进程名填写，可以规避一些so中针对进程名校验
        emulator = AndroidEmulatorBuilder.for64Bit().setProcessName("com.sina.oasis").build();
        // 设置模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机,传入APK,Unidbg可以替我们做部分签名校验的工作
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/resources/re/lvzhou/oasis.apk"));
        // 加载so到虚拟内存,第二个参数的意思表示是否执行动态库的初始化代码
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/resources/re/lvzhou/liboasiscore.so"),true);
        // 获取so模块的句柄
        module = dm.getModule();
        // 设置JNI
        vm.setJni(this);
        // 打印日志
        vm.setVerbose(true);
        // 调用JNI方法
        try {
            emulator.traceCode(dm.getModule().base,dm.getModule().base+dm.getModule().size)
                    .setRedirect(new PrintStream(new FileOutputStream("unidbg-android/src/test/resources/re/lvzhou/trace1.bak.log"),true));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        dm.callJNI_OnLoad(emulator);;   // 调用JNI_OnLoad
    }

    public static void main(String[] args){
        Oasis oasis = new Oasis();
    }
}
