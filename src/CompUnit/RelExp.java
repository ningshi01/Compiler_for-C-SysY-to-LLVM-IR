package CompUnit;

import LLVM.BasicBlock;
import Token.Token;

import java.util.ArrayList;

public class RelExp {
    //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    //改写：AddExp {('<' | '>' | '<=' | '>=') AddExp}
    public ArrayList<Token> arr_OPT;
    public ArrayList<AddExp> arr_AddExp;

    public RelExp(ArrayList<Token> arr_OPT, ArrayList<AddExp> arr_AddExp) {
        this.arr_OPT = arr_OPT;
        this.arr_AddExp = arr_AddExp;
    }

    public RelExp() {
    }
    public int registerID;
    public int getInt() {
        int tmp=arr_AddExp.get(0).getInt();
        return tmp;
    }
    public boolean isIntCon() {
        if(arr_AddExp.size()>1)
            return false;
        return arr_AddExp.get(0).isIntCon();
    }
    public String getInt_h(){
        if(isIntCon())
            return ""+arr_AddExp.get(0).getInt();
        else
            return "%"+registerID;
    }
    public ArrayList<BasicBlock> needs = new ArrayList<>();//需要回填处理的基本块
}
