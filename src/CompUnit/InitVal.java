package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class InitVal {
    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public Exp para_Exp;
    public Token para_LBRACE;
    public ArrayList<InitVal> arr_InitVal;
    public ArrayList<Token> arr_COMMA;
    public Token para_RBRACE;

    public InitVal(Exp para_Exp, Token para_LBRACE, ArrayList<InitVal> arr_InitVal, ArrayList<Token> arr_COMMA, Token para_RBRACE) {
        this.para_Exp = para_Exp;
        this.para_LBRACE = para_LBRACE;
        this.arr_InitVal = arr_InitVal;
        this.arr_COMMA = arr_COMMA;
        this.para_RBRACE = para_RBRACE;
    }

    public InitVal() {
    }
    public int getInt(){
        if (arr_InitVal==null){
            return 0;
        }
        if(arr_InitVal.size()==0){
            return para_Exp.getInt();
        }else{
            int tmp=0;
            for(InitVal initVal:arr_InitVal){
                tmp+=initVal.getInt();
            }
            return tmp;
        }
    }
    public void getNums(ArrayList<Integer> tmps){
        if(arr_InitVal.size()==0){
            tmps.add(para_Exp.getInt());
        }else{
            for(InitVal i:arr_InitVal){
                i.getNums(tmps);
            }
        }
    }
}
