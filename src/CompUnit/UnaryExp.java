package CompUnit;

import LLVM.SymbolManager;
import Semantics.Semantics;
import Symbol.Symbol;
import Symbol.VarSymbol;
import Token.Init;
import Token.Token;
import org.xml.sax.Parser;

public class UnaryExp {
    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public PrimaryExp para_PrimaryExp;
    public Token para_Ident;
    public Token para_LPARENT;
    public FuncRParams para_FuncRParams;
    public Token para_RPARENT;
    public UnaryOp para_UnaryOp;
    public UnaryExp para_UnaryExp;
    public int RegisterID;
    public boolean flag_Intcon=true;
    public UnaryExp(PrimaryExp para_PrimaryExp, Token para_Ident, Token para_LPARENT, FuncRParams para_FuncRParams,Token para_RPARENT,UnaryOp para_UnaryOp, UnaryExp para_UnaryExp,int RegisterID) {
        this.para_PrimaryExp = para_PrimaryExp;
        this.para_Ident = para_Ident;
        this.para_LPARENT = para_LPARENT;
        this.para_FuncRParams = para_FuncRParams;
        this.para_RPARENT = para_RPARENT;
        this.para_UnaryOp = para_UnaryOp;
        this.para_UnaryExp = para_UnaryExp;
    }

    public UnaryExp() {
    }
    public int getInt() {
        int tmp = 114514;
        if(para_PrimaryExp.para_CNumber!=null){
            tmp=para_PrimaryExp.getInt();
        }else if(para_UnaryOp.para_PMN!=null){
            tmp=para_UnaryExp.getInt();
            if(para_UnaryOp.para_PMN.type== Init.tokenType.PLUS){
                tmp = tmp;
            }else if(para_UnaryOp.para_PMN.type == Init.tokenType.MINU){
                tmp = - tmp;
            }else{
                if(tmp>0){
                    tmp = -1;
                }else{
                    tmp = 1;
                }
            }
        }else{//计算函数的返回值
            tmp=-1;
        }
        return tmp;
    }
    public boolean isIntCon() {
        if(flag_Intcon==false)
            return false;
        if(para_PrimaryExp.para_CNumber!=null){
            return para_PrimaryExp.isIntCon();
        }else if(para_UnaryExp.para_PrimaryExp!=null&&para_UnaryExp.para_PrimaryExp.para_CNumber!=null){
            return true;
        }else if(para_UnaryExp.flag_Intcon==false){
            return para_UnaryExp.isIntCon();
        }else if(para_UnaryOp.para_PMN!=null){
            return false;
        }else{//计算函数的返回值
            return false;
        }
    }
    public String getRparaType(){
        if(para_PrimaryExp.para_CNumber!=null)
            return para_PrimaryExp.getRparaType();
        else if(para_UnaryOp.para_PMN!=null)
            return para_UnaryExp.getRparaType();
        else{
            return "i32";
        }
    }
    public String getInt_h(){
        if(isIntCon())
            return ""+this.getInt();
        else
            return "%"+RegisterID;
    }
}
