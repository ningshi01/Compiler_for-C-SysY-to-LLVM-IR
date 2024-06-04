package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class ConstInitVal {
    //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public ConstExp para_ConstExp = new ConstExp();
    public Token para_LBRACE = new Token();
    public ArrayList<ConstInitVal> arr_ConstInitVal = new ArrayList<>();
    public ArrayList<Token> arr_COMMA=new ArrayList<>();
    public Token para_RBRACE = new Token();
    public ConstInitVal(ConstExp para_ConstExp, Token para_LBRACE, ArrayList<ConstInitVal> arr_ConstInitVal, ArrayList<Token> arr_COMMA, Token para_RBRACE) {
        this.para_ConstExp = para_ConstExp;
        this.para_LBRACE = para_LBRACE;
        this.arr_ConstInitVal = arr_ConstInitVal;
        this.arr_COMMA = arr_COMMA;
        this.para_RBRACE = para_RBRACE;
    }
    public ConstInitVal() {
    }
    public int getInt(){
        if(arr_ConstInitVal.size()==0){
            return para_ConstExp.getInt();
        }else{
            int tmp=0;
            for(ConstInitVal constInitVal:arr_ConstInitVal){
                tmp+=constInitVal.getInt();
            }
            return tmp;
        }
    }
    public void getNums(ArrayList<Integer> tmps){
        if(arr_ConstInitVal.size()==0){
            tmps.add(para_ConstExp.getInt());
        }else{
            for(ConstInitVal i:arr_ConstInitVal){
                i.getNums(tmps);
            }
        }
    }
}
