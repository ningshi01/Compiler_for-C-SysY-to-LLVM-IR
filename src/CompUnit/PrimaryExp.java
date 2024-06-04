package CompUnit;

import Token.Token;

public class PrimaryExp {
    //PrimaryExp → '(' Exp ')' | LVal | Number
    public Token para_LPARENT;
    public Exp para_Exp;
    Token para_RPARENT;
    public LVal para_LVal;
    public CNumber para_CNumber;

    public PrimaryExp(Token para_LPARENT, Exp para_Exp, Token para_RPARENT, LVal para_LVal, CNumber para_CNumber) {
        this.para_LPARENT = para_LPARENT;
        this.para_Exp = para_Exp;
        this.para_RPARENT = para_RPARENT;
        this.para_LVal = para_LVal;
        this.para_CNumber = para_CNumber;
    }

    public PrimaryExp() {
    }
    public int getInt(){
        if(para_LPARENT.TData!=null){
            return para_Exp.getInt();
        }else if(para_LVal.para_Ident!=null){
            return para_LVal.getInt();
        }else{
            return para_CNumber.getInt();
        }
    }
    public boolean isIntCon(){
        if(para_LPARENT.TData!=null){
            return para_Exp.isIntCon();
        }else if(para_LVal.para_Ident!=null) {
            return para_LVal.isIntCon();
        }else{
            return para_CNumber.isIntCon();
        }
    }
    public String getRparaType(){
        return para_LVal.getRparaType();
    }
}
