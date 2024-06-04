import LLVM.*;
import LLVM.Module;
import Symbol.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForMIPS {
    private static ForMIPS instance = new ForMIPS();
    public static ForMIPS getInstance(){ return instance;}
    private Module module;


    private static void mips(String str) {
        Compiler._MIPS().add(str+'\n');
    }
    private void init(){
        module = Module.getInstance();
    }
    public void genMips(){
        init();
        mips(".data\n");
        mips("\n.text\n");
        //全局变量部分
        for(Symbol GlobalSymbol :module.GlobalDeclList){
            mips("\n# "+GlobalSymbol.toString()+"\n");
            if(GlobalSymbol.selfType != Value.SelfType.ARRAY){//全局变量

            }else{//全局数组

            }
        }

        mips("jal main");
        mips("li $v0, 10");
        mips("syscall\n");
        for(Function func : module.FuncList){//开始识别函数
            /*先跳过引用IO函数,但先前未存储到函数表里*/
            mips(func.name+":");
            for(BasicBlock block: func.BblockList){//遍历基本块
                mips(block.getName());
                for(Instruction instr: block.InstrList){//遍历块内指令
                    mips("\n# "+instr.toString());
                }
            }
            mips("\n");
        }
    }
}
