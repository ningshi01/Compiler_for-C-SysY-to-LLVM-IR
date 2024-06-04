package CompUnit;

import LLVM.SymbolManager;
import LLVM.Value;
import Semantics.Semantics;
import Symbol.Symbol;
import Symbol.VarSymbol;
import Token.Token;

import java.util.ArrayList;

public class LVal {
    //LVal → Ident {'[' Exp ']'}
    public Token para_Ident;
    public ArrayList<Token> arr_LBRACK= new ArrayList<>();
    public ArrayList<Exp> arr_Exp= new ArrayList<>();
    public ArrayList<Token> arr_RBRACK= new ArrayList<>();
    public String Rpara_type = "i32";//默认Rpara调用时为i32
    public String lval_before_equal_registerID = null;//调用lval=exp时该lval的存储寄存器号
    public String lval_before_equal_type = null;//调用lval=exp时该lval的存储类型
    public int vFor_Array = 0;
    public LVal(Token para_Ident, ArrayList<Token> arr_LBRACK, ArrayList<Exp> arr_Exp, ArrayList<Token> arr_RBRACK) {
        this.para_Ident = para_Ident;
        this.arr_LBRACK = arr_LBRACK;
        this.arr_Exp = arr_Exp;
        this.arr_RBRACK = arr_RBRACK;
    }

    public LVal() {
    }
    public int getInt(){
        //查询关键字的getInt()
        int tmp=-1;
        SymbolManager semantics = SymbolManager.getInstance();
        if(semantics.curnow().checkToRoot(para_Ident.TData)){
            Symbol symbol =semantics.curnow().getToRoot(para_Ident.TData);
            if (symbol.selfType== Value.SelfType.VAR||symbol.selfType== Value.SelfType.CONST){//是变量/常量
                tmp = symbol.returnValue;
            }else if(symbol.selfType== Value.SelfType.PARA){
                return  1;
            }else{//是数组
                return vFor_Array;
            }
        }
        return tmp;
    }
    public boolean isIntCon(){
        return false;
    }
    public String getRparaType(){
        return Rpara_type;
    }
}
