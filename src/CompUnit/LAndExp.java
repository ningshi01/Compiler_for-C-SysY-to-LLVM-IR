package CompUnit;

import LLVM.BasicBlock;
import Token.Token;

import java.util.ArrayList;
import java.util.List;

public class LAndExp {
    //LAndExp → EqExp | LAndExp '&&' EqExp
    //改写：EqExp { '&&' EqExp }
    public ArrayList<Token> arr_AND;
    public ArrayList<EqExp> arr_EqExp;

    public LAndExp(ArrayList<Token> arr_AND, ArrayList<EqExp> arr_EqExp) {
        this.arr_AND = arr_AND;
        this.arr_EqExp = arr_EqExp;
    }

    public LAndExp() {
    }
    public int registerID;
    public int getInt() {
        int tmp=arr_EqExp.get(0).getInt();
        return tmp;
    }
    public boolean isIntCon() {
        if(arr_EqExp.size()>1)
            return false;
        return arr_EqExp.get(0).isIntCon();
    }
    public ArrayList<BasicBlock> needs = new ArrayList<>();//需要回填处理的基本块
}
