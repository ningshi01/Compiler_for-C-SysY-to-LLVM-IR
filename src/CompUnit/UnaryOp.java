package CompUnit;

import Token.Token;

public class UnaryOp {
    //UnaryOp → '+' | '−' | '!'
    public Token para_PMN;

    public UnaryOp(Token para_PMN) {
        this.para_PMN = para_PMN;
    }

    public UnaryOp() {
    }
}
