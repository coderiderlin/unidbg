package re.chatgpt;

import com.github.unidbg.linux.android.dvm.*;
import re.util.LogUtil;

public class JniHandler extends AbstractJni {


    private static final String TAG = JniHandler.class.getSimpleName();

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
}
