package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class VarDef {
    //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    public Token para_Ident = new Token();
    public ArrayList<Token> arr_LBRACK = new ArrayList<>();
    public ArrayList<ConstExp> arr_ConstExp = new ArrayList<>();
    public ArrayList<Token> arr_RBRACK = new ArrayList<>();
    public Token para_ASSIGN = new Token();
    public InitVal para_InitVal = new InitVal();

    public VarDef(Token para_Ident, ArrayList<Token> arr_LBRACK, ArrayList<ConstExp> arr_ConstExp, ArrayList<Token> arr_RBRACK, Token para_ASSIGN, InitVal para_InitVal) {
        this.para_Ident = para_Ident;
        this.arr_LBRACK = arr_LBRACK;
        this.arr_ConstExp = arr_ConstExp;
        this.arr_RBRACK = arr_RBRACK;
        this.para_ASSIGN = para_ASSIGN;
        this.para_InitVal = para_InitVal;
    }

    public VarDef() {
    }
}
