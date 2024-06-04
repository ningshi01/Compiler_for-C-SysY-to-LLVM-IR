package CompUnit;

import LLVM.BasicBlock;
import Token.Token;

import java.util.ArrayList;

public class EqExp {
    //EqExp → RelExp | EqExp ('==' | '!=') RelExp
    //改写： RelExp { ('==' | '!=') RelExp}
    public ArrayList<Token> arr_EQLorNEQ;
    public ArrayList<RelExp> arr_RelExp;

    public EqExp(ArrayList<Token> arr_EQLorNEQ,ArrayList<RelExp> arr_RelExp) {
        this.arr_EQLorNEQ = arr_EQLorNEQ;
        this.arr_RelExp = arr_RelExp;
    }

    public EqExp() {
    }

    public int registerID;
    public int getInt() {
        int tmp=arr_RelExp.get(0).getInt();
        return tmp;
    }
    public boolean isIntCon() {
        if(arr_RelExp.size()>1)
            return false;
        return arr_RelExp.get(0).isIntCon();
    }
    public ArrayList<BasicBlock> needs = new ArrayList<>();//需要回填处理的基本块
}
