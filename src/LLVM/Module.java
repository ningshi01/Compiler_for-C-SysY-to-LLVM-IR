package LLVM;

import Symbol.Symbol;

import java.util.ArrayList;

public class Module extends Value{//顶层编译单元
    private static Module instance = new Module();
    public static Module getInstance(){
        return instance;
    }
    public ArrayList<Symbol> GlobalDeclList =new ArrayList<>();//全局变量
    public ArrayList<Function> FuncList = new ArrayList<>();//函数
    public boolean InGlobalList(String Ident){
        for(Symbol s:GlobalDeclList){
            if(s.Name.equals(Ident)){
                return true;
            }
        }
        return false;
    }
    public Function findFunc(String Ident){
        for(Function f:FuncList){
            if(f.name.equals(Ident)){
                return f;
            }
        }
        return null;
    }
}
