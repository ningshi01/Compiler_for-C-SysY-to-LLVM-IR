package CompUnit;

import LLVM.BasicBlock;
import Token.Token;

import java.util.ArrayList;

public class LOrExp {
    //LOrExp → LAndExp | LOrExp '||' LAndExp
    //改写：LAndExp { '||' LAndExp }
    public ArrayList<Token> arr_OR;
    public ArrayList<LAndExp> arr_LAndExp;

    public LOrExp(ArrayList<Token> arr_OR, ArrayList<LAndExp> arr_LAndExp) {
        this.arr_OR = arr_OR;
        this.arr_LAndExp = arr_LAndExp;
    }

    public LOrExp() {
    }
    public int registerID;
    public int getInt() {
        int tmp=arr_LAndExp.get(0).getInt();
        return tmp;
    }
    public boolean isIntCon() {
        if(arr_LAndExp.size()>1)
            return false;
        return arr_LAndExp.get(0).isIntCon();
    }
    public ArrayList<BasicBlock> needs = new ArrayList<>();//需要回填处理的基本块
}
