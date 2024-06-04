package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class VarDecl {
    //VarDecl → BType VarDef { ',' VarDef } ';'
    public BType para_BType;
    public ArrayList<VarDef> arr_VarDef = new ArrayList<>();
    public ArrayList<Token> arr_COMMA = new ArrayList<>();
    public Token para_SEMICN;

    public VarDecl(BType para_BType, ArrayList<VarDef> arr_VarDef, ArrayList<Token> arr_COMMA, Token para_SEMICN) {
        this.para_BType = para_BType;
        this.arr_VarDef = arr_VarDef;
        this.arr_COMMA = arr_COMMA;
        this.para_SEMICN = para_SEMICN;
    }

    public VarDecl() {

    }
}
