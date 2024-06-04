import Token.*;

import java.io.IOException;
import java.util.ArrayList;


public class Compiler {
    public static String forInput = "testfile.txt";
    public static String forOutput = "output.txt";
    public static String forError = "error.txt";
    public static String forllvm = "llvm_ir.txt";
    public static String forMips = "mips.txt";
    private static ArrayList<Errors> arr_Error = new ArrayList<>();
    public static ArrayList<Errors> _Error(){
        return arr_Error;
    }
    private static ArrayList<String> arr_llvm = new ArrayList<>();
    public static ArrayList<String> _LLVM(){
        return arr_llvm;
    }
    private static ArrayList<String> arr_mips = new ArrayList<>();
    public static ArrayList<String> _MIPS(){
        return arr_mips;
    }

    public static void main(String[] args) throws IOException {
        StringBuilder theData = new StringBuilder();
        ArrayList<TokenMap> arr_ListMap = new ArrayList<>();
        ArrayList<Token> arr_token = new ArrayList<>();
        ArrayList<String> arr_Parser = new ArrayList<>();
        boolean error_flag = false;
        Init.read(theData,forInput);
        Init.initMap(arr_ListMap);
//        词法分析
        Deal.analyse_1(theData,arr_token,arr_ListMap);
//        语法分析
        Deal.analyse_2(arr_token,arr_Parser);
//        错误处理 true-输出 | false-不输出
        Deal.analyse_3(arr_Error,error_flag,true);
//        语义分析,生成中间代码
        Deal.analyse_4();
//        生成目标代码
        Deal.analyse_5();

//        //词法分析写入output
//        Init.write1(arr_token,forOutput);
//        //语法分析写入output
//        Init.write2(arr_Parser,forOutput);
//        //错误处理写入error
//        Init.write3(arr_Error,forError);
        //中间代码写入llvm_ir
        Init.write4(arr_llvm,forllvm);
        //MIPS写入mips
        Init.write5(arr_mips,forMips);
    }
    public static Init.tokenType findType(ArrayList<TokenMap> arr_ListMap, String tokenName){
        // 遍历ArrayList查找匹配的tokenName
        for (TokenMap tokenMap : arr_ListMap) {
            if (tokenMap.tokenName.equals(tokenName)) {
                return tokenMap.tokenType;
            }
        }
        return null;
    }
}