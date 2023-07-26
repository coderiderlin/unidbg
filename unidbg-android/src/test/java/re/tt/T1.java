//package com.unicorncourse08;
//import capstone.Capstone;
//import com.github.unidbg.AndroidEmulator;
//import com.github.unidbg.Emulator;
//import com.github.unidbg.LibraryResolver;
//import com.github.unidbg.Module;
//import com.github.unidbg.arm.backend.Backend;
//import com.github.unidbg.arm.backend.CodeHook;
//import com.github.unidbg.arm.backend.WriteHook;
//import com.github.unidbg.arm.backend.dynarmic.Dynarmic;
//import com.github.unidbg.linux.android.AndroidARMEmulator;
//import com.github.unidbg.linux.android.AndroidResolver;
//import com.github.unidbg.linux.android.dvm.AbstractJni;
//import com.github.unidbg.linux.android.dvm.DalvikModule;
//import com.github.unidbg.linux.android.dvm.VM;
//import com.github.unidbg.listener.TraceCodeListener;
//import com.github.unidbg.memory.Memory;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import unicorn.Unicorn;
//
//import java.io.*;
//import java.util.Arrays;
//import java.util.Map;
//
//public class DeOllvm<mouldBase> extends AbstractJni {
//
//    static {
//
//    }
//
//
//    private AndroidEmulator emulator;
//    private final VM vm;
//    public DeStrWriteHook trace;
//    public DeCodeHook codeTrace;
//    public Module module;
//    public static String filePath = " \\unidbg-master\\libs\\libmetasec_ml.so";
//    public static String sscronet_filePath = " \\unidbg-master\\libs\\libsscronet.so";
//    private static final Log log = LogFactory.getLog(DalvikModule.class);
//    public decryptOLLVM(){
//        emulator=new AndroidARMEmulator("com.ss.android.ugc.aweme",null,null);
//        vm = emulator.createDalvikVM();
//        vm.setVerbose(true);
//        vm.setJni(this);
//
//        try {
//            trace = new DeStrWriteHook(false);
//
//            final Memory memory=emulator.getMemory();
//            //这里的android版本指定后会自动load相关的so
//            LibraryResolver resolver = new AndroidResolver(23);
//            memory.setLibraryResolver(resolver);
//            //设置内存写入的监控
//            emulator.getBackend().hook_add_new(trace,1,0,emulator);
//
//            emulator.loadLibrary(new File(sscronet_filePath),true);
//            emulator.loadLibrary(new File(" \\unidbg-master\\libs\\libttboringssl.so"));
//            emulator.loadLibrary(new File(" \\unidbg-master\\libs\\libttcrypto.so"));
//            emulator.loadLibrary(new File(" \\unidbg-master\\libs\\libandroid.so"));
//            module = emulator.loadLibrary(new File(filePath),true);
//            codeTrace = new DeCodeHook(module.base);
//            // 添加一个指令集hook回调,并输出当前执行指令在so文件中的偏移地址
//            emulator.getBackend().hook_add_new(codeTrace,module.base+0x7c50,module.base+0x84170,emulator);
//            /*emulator.traceCode(module.base + 0x7c50, module.base + 0x84170, new TraceCodeListener() {
//                @Override
//                public void onInstruction(Emulator<?> emulator, long address, Capstone.CsInsn insn) {
//                    DeCodeHook.write2file("\\unidbg-master\\libs\\traceA.log"
//                            ,Long.toHexString(address-module.base));
//                }
//            });*/
//
//
//            log.info("---------------before call jni_onload");
//            vm.callJNI_OnLoad(emulator,module);
//            log.info("---------------after call jni_onload");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static byte[] readFile(String strFile){
//        try{
//            InputStream is = new FileInputStream(strFile);
//            int iAvail = is.available();
//            byte[] bytes = new byte[iAvail];
//            is.read(bytes);
//            is.close();
//            return bytes;
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null ;
//    }
//
//    public static void writeFile(byte[] data,String savefile){
//        try {
//            FileOutputStream fos=new FileOutputStream(savefile);
//            BufferedOutputStream bos=new BufferedOutputStream(fos);
//            bos.write(data,0,data.length);
//            bos.flush();
//            bos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String bytetoString(byte[] bytearray) {
//        String result = "";
//        char temp;
//
//        int length = bytearray.length;
//        for (int i = 0; i < length; i++) {
//            if(bytearray[i]!=0)
//            {
//                temp = (char) bytearray[i];
//                result += temp;
//            }
//        }
//        return result;
//    }
//
//    public static void main(String[] args){
//        decryptOLLVM destr=new decryptOLLVM();
//        //因为so文件的结构发生变化，简单粗暴地写回原so文件地址已经不行了，会报错；所以这里以下的代码都可以注释掉，没必要了（其他没改文件结构的so是可以直接用来打patch的）；
//        String savepath="D:\\BaiduNetdiskDownload\\unidbg-master\\libs\\libnative-lib_new.so";
//        /*
//         * sodata这个是从文件读取的，但实际在内存中的so长度比文件读取的长，造成了下面System.arraycopy(sodata,0,start,0,offset.intValue())报indexoutofbond错误；
//         * */
//        byte[] sodata=readFile(filePath);
//        long base_addr=destr.module.base;
//        long module_size=destr.module.size;
//        System.out.println(String.format("base_addr:0x%x module_size:%s end_addr:0x%x", base_addr, module_size, base_addr+module_size));
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        //遍历保存的写入地址和写入数据
//        for(Map.Entry<Long, byte[]> item : destr.trace.dstr_datas.entrySet()){
//            //如果范围是在模块内的。则进行处理
//            if(item.getKey()>base_addr && item.getKey()<base_addr+module_size){
//
//                //获取到正确的写入的偏移位置
//                baos = new ByteArrayOutputStream();
//                Long offset=item.getKey()-base_addr-0x1000;
//                System.out.println(String.format("offset:0x%x----data:%s----data:%s",offset,bytetoString(item.getValue()), Arrays.toString(item.getValue())));
//                //先把前半部分取出来
//                byte[] start=new byte[offset.intValue()];
//                //int diffLen = start.length - sodata.length;//得到磁盘so长度和内存so长度差值
//                //byte[] sodata = new byte[offset.intValue()];
//                System.arraycopy(sodata,0,start,0,offset.intValue());
//                //然后把后半部分的大小计算出来
//                int endsize=sodata.length-offset.intValue()-item.getValue().length;
//                //把后半部分的数据填充上
//                byte[] end=new byte[endsize];
//                System.arraycopy(sodata,offset.intValue()+item.getValue().length,end,0,endsize);
//                //将三块位置的数据填充上
//                baos.write(start,0,start.length);
//                baos.write(item.getValue(),0,item.getValue().length);
//                baos.write(end,0,end.length);
//                //最后把so保存起来
//                sodata=baos.toByteArray();
//            }
//        }
//        writeFile(baos.toByteArray(),savepath);
//    }
//}