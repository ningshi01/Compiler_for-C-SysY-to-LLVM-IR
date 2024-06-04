package CompUnit;

import Token.Token;

public class FuncDef {
    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public FuncType para_FuncType = new FuncType();
    public Token para_Ident = new Token();
    public Token para_LPARENT = new Token();
    public FuncFParams para_FuncFParams = new FuncFParams();
    public Token para_RPARENT = new Token();
    public Block para_Block = new Block();

    public FuncDef(FuncType para_FuncType, Token para_Ident, Token para_LPARENT, FuncFParams para_FuncFParams, Token para_RPARENT, Block para_Block) {
        this.para_FuncType = para_FuncType;
        this.para_Ident = para_Ident;
        this.para_LPARENT = para_LPARENT;
        this.para_FuncFParams = para_FuncFParams;
        this.para_RPARENT = para_RPARENT;
        this.para_Block = para_Block;
    }

    public FuncDef() {
    }
}
