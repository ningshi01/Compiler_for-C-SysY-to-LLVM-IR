package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class FuncFParam {
    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public BType para_BType;
    public Token para_Ident;
    public ArrayList<Token> arr_LBRACK=new ArrayList<>();
    public ArrayList<Token> arr_RBRACK=new ArrayList<>();
    public ArrayList<ConstExp> arr_ConstExp=new ArrayList<>();
    //错误处理识别参数类型
    /*
    * 0 变量
    * 1 一维
    * 2 二维
    * */
    public int type;
    public FuncFParam(BType para_BType, Token para_Ident, ArrayList<Token> arr_LBRACK, ArrayList<Token> arr_RBRACK, ArrayList<ConstExp> arr_ConstExp,int type) {
        this.para_BType = para_BType;
        this.para_Ident = para_Ident;
        this.arr_LBRACK = arr_LBRACK;
        this.arr_RBRACK = arr_RBRACK;
        this.arr_ConstExp = arr_ConstExp;
        this.type = type;
    }

    public FuncFParam() {
    }
}
