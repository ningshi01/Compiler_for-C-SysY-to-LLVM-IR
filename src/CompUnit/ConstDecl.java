package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class ConstDecl {
    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public Token para_const=new Token();
    public BType para_BType=new BType();
    public ArrayList<ConstDef> arr_ConstDef= new ArrayList<ConstDef>();
    public ArrayList<Token> arr_COMMA = new ArrayList<Token>();
    public Token para_SEMICN = new Token();

    public ConstDecl(Token para_const, BType para_BType, ArrayList<ConstDef> arr_ConstDef, ArrayList<Token> arr_COMMA, Token para_SEMICN) {
        this.para_const = para_const;
        this.para_BType = para_BType;
        this.arr_ConstDef = arr_ConstDef;
        this.arr_COMMA = arr_COMMA;
        this.para_SEMICN = para_SEMICN;
    }

    public ConstDecl() {
    }
}
