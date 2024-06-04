package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class ConstDef {
    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public Token para_Ident = new Token();
    public ArrayList<Token> arr_LBRACK = new ArrayList<>();
    public ArrayList<ConstExp> arr_ConstExp = new ArrayList<>();
    public ArrayList<Token> arr_RBRACK = new ArrayList<>();
    public Token para_ASSIGN = new Token();
    public ConstInitVal para_ConstInitVal = new ConstInitVal();

    public ConstDef(Token para_Ident, ArrayList<Token> arr_LBRACK, ArrayList<ConstExp> arr_ConstExp, ArrayList<Token> arr_RBRACK, Token para_ASSIGN, ConstInitVal para_ConstInitVal) {
        this.para_Ident = para_Ident;
        this.arr_LBRACK = arr_LBRACK;
        this.arr_ConstExp = arr_ConstExp;
        this.arr_RBRACK = arr_RBRACK;
        this.para_ASSIGN = para_ASSIGN;
        this.para_ConstInitVal = para_ConstInitVal;
    }

    public ConstDef() {
    }
}
