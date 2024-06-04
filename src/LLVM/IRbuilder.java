package LLVM;

import Symbol.Symbol;

import java.util.ArrayList;

public class IRbuilder {
    public static Module module = Module.getInstance();
    public static ArrayList<String> ToTxT = new ArrayList<>();
    public static void llvm(String content){
        ToTxT.add(content);
    }
    public static void llvm_Init(){
        ToTxT.add("" +
                "declare i32 @getint()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n");
    }
    public static void llvm_Deal(){
        for(Symbol s:module.GlobalDeclList){
            llvm(s.toString());
        }llvm("\n");
        for (Function f: module.FuncList){
            llvm(f.toString());
        }
    }
    public static void llvm_Done(ArrayList<String> arr_llvm){
        ToTxT.forEach(e->{
            arr_llvm.add(e);
        });
    }
    public static void ToIR(ArrayList<String> arr_llvm){
        llvm_Init();
        llvm_Deal();
        llvm_Done(arr_llvm);
    }
}
