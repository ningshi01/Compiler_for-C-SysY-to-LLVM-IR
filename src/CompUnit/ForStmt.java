package CompUnit;

import Token.Token;

public class ForStmt {
    //ForStmt → LVal '=' Exp
    public LVal para_LVal;
    Token para_ASSIGN;
    public Exp para_Exp;

    public ForStmt(LVal para_LVal, Token para_ASSIGN, Exp para_Exp) {
        this.para_LVal = para_LVal;
        this.para_ASSIGN = para_ASSIGN;
        this.para_Exp = para_Exp;
    }

    public ForStmt() {
    }
}
