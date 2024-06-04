package CompUnit;

import Token.Token;

public class MainFuncDef {
    //MainFuncDef → 'int' 'main' '(' ')' Block
    Token para_int;
    Token para_main;
    Token para_LPARENT;
    Token para_RPARENT;
    public Block para_Block;

    public MainFuncDef(Token para_int, Token para_main, Token para_LPARENT, Token para_RPARENT, Block para_Block) {
        this.para_int = para_int;
        this.para_main = para_main;
        this.para_LPARENT = para_LPARENT;
        this.para_RPARENT = para_RPARENT;
        this.para_Block = para_Block;
    }

    public MainFuncDef() {
    }
}
