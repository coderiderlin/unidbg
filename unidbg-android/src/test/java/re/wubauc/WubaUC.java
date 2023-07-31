package re.wubauc;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import sun.security.pkcs.ParsingException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WubaUC extends AbstractJni implements IOResolver {

    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private final DvmClass RsaCryptService;

    private final DvmObject<?> context ;

    private final boolean logging;

    WubaUC(boolean logging) {
        this.logging = logging;
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.wuba").build(); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析
        vm = emulator.createDalvikVM(); // 创建Android虚拟机
        vm.setJni(this);
        vm.setVerbose(logging); // 设置是否打印Jni调用细节
        emulator.getSyscallHandler().addIOResolver(this);
        //DalvikModule dmA = vm.loadLibrary(new File("unidbg-android/src/test/java/com/xiaojianbang/ndk/libxiaojianbangA.so"), false); // 加载libttEncrypt.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数
        DalvikModule dm = vm.loadLibrary(new File("D:\\project\\unidbg-master\\unidbg-android\\src\\main\\java\\com\\wuba\\uc\\libcom_wuba_uc_rsa.so"), false); // 加载libttEncrypt.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数
        dm.callJNI_OnLoad(emulator); // 手动执行JNI_OnLoad函数
        module = dm.getModule(); // 加载好的 libxiaojianbang.so 对应为一个模块
        RsaCryptService = vm.resolveClass("com/wuba/uc/RsaCryptService");
        context = vm.resolveClass("android/content/Context").newObject(null);
        DvmClass X509Certificate = vm.resolveClass("java/security/cert/X509Certificate");
        vm.resolveClass("java/security/cert/Certificate", X509Certificate);
        //vm.resolveClass("java/math/BigInteger")
    }

    void destroy() throws IOException {
        emulator.close();
        if (logging) {
            System.out.println("destroy");
        }
    }

    public static void main(String[] args) throws Exception {
        WubaUC test = new WubaUC(true);
        test.callFunc();
        test.destroy();
    }

    void callFunc() {
        RsaCryptService.callStaticJniMethodObject(emulator, "init(Landroid/content/Context;)V",context); // 初始化
        // System.out.println("init:");


        ByteArray encodeResult = RsaCryptService.callStaticJniMethodObject(emulator, "encrypt([BI)[B",
                new ByteArray(vm,"111111".getBytes()),0x10); // 执行Jni方法
        System.out.println("encodeResult: " + new String(encodeResult.getValue()));
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) throws ParsingException {
        if ("android/content/Context->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;".equals(signature)){
            String toString = varArg.getObjectArg(0).getValue().toString();

            System.out.println("getSharedPreferences  :" +toString);

            int intArg = varArg.getIntArg(1);

            System.out.println("getSharedPreferences1  :" +intArg);

            return vm.resolveClass("android/content/SharedPreferences;").newObject(signature);
        }
        if ("android/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;".equals(signature)){
            return vm.resolveClass("Landroid/content/SharedPreferences$Editor;").newObject(signature);
        }
        if ("Landroid/content/SharedPreferences$Editor;->putInt(Ljava/lang/String;I)Landroid/content/SharedPreferences$Editor;".equals(signature)){
            String toString = varArg.getObjectArg(0).getValue().toString();

            System.out.println("putInt  :" +toString);

            int intArg = varArg.getIntArg(1);

            System.out.println("putInt  :" +intArg);

            return vm.resolveClass("android/content/SharedPreferences$Editor").newObject(signature);
        }
        if ("android/content/Context->getPackageName()Ljava/lang/String;".equals(signature)){

            return new StringObject(vm,"android/content/pm/PackageManager");
        }
        if ("java/lang/Class->toByteArray()[B".equals(signature)){

            return new ByteArray(vm,signature.getBytes(StandardCharsets.UTF_8));
        }
        if ("java/security/cert/CertificateFactory->generateCertificate(Ljava/io/InputStream;)Ljava/security/cert/Certificate;".equals(signature)){
            // DvmClass dvmClass = vm.resolveClass("java/security/cert/Certificate");
            return vm.resolveClass("java/security/cert/Certificate").newObject(null);
        }
        if ("java/security/cert/X509Certificate->getPublicKey()Ljava/security/PublicKey;".equals(signature)){

            return  new StringObject(vm,"bc587820f3ad01608b64af88af1ee883bed3e4954457da8f7b5af76403c40329bc4f5e66e46f83eacfba28b2a4d3a61553ee237db6c22f49f5377e4d6ac3b63b3cd36a9746f9dc6bdd7c2aae26020a6b6fc1ac6399b4da58f8ed3944686cfaca328d1dd581ef86d1dbe87c12af0130e8ce728d00814e968bee602752d25ac691");

        }
        if ("java/lang/String->getModulus()Ljava/math/BigInteger;".equals(signature)){
            return vm.resolveClass("java/math/BigInteger").newObject(null);
        }
        if ("java/math/BigInteger->toString(I)Ljava/lang/String;".equals(signature)){
            return new StringObject(vm,"12312321");
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public boolean callBooleanMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        if ("Landroid/content/SharedPreferences$Editor;->commit()Z".equals(signature)){
            return true;
        }
        if ("java/lang/String->equals(Ljava/lang/Object;)Z".equals(signature)){
            return true;
        }
        return super.callBooleanMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public int callIntMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        if ("android/content/SharedPreferences;->getInt(Ljava/lang/String;I)I".equals(signature)){
            String toString = varArg.getObjectArg(0).getValue().toString();

            System.out.println("callIntMethod getInt  :" +toString);

            int intArg = varArg.getIntArg(1);

            System.out.println("callIntMethod getInt  :" +intArg);

            return 1;
        }
        return super.callIntMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        if ("android/content/pm/PackageInfo->signatures:[Landroid/content/pm/Signature;".equals(signature)){
            Object value = dvmObject.getValue();
            System.out.println("value :"+value);

            return new ArrayObject(vm.resolveClass("android/content/pm/PackageManager"));
        }

        return super.getObjectField(vm, dvmObject, signature);
    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if ("java/security/cert/CertificateFactory->getInstance(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;".equals(signature)){
            String toString = varArg.getObjectArg(0).getValue().toString();
            System.out.println("CertificateFactory getInstance  :" +toString);
            //DvmClass dvmClass1 = vm.resolveClass("java/security/cert/CertificateFactory");
            return vm.resolveClass("java/security/cert/CertificateFactory").newObject(null);
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> newObject(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) throws ParsingException {
        if ("java/io/ByteArrayInputStream-><init>([B)V".equals(signature)) {



            ByteArray array = varArg.getObjectArg(0);
            assert array != null;
            return new ByteArray(vm,signature.getBytes(StandardCharsets.UTF_8));
        }

        return super.newObject(vm, dvmClass, signature, varArg);
    }

    @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {
        System.out.println("pathname:"+pathname);
        return null;
    }


}