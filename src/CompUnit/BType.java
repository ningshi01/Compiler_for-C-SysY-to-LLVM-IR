package CompUnit;

import Token.Token;

public class BType {
    //BType → 'int'
    public Token para_int = new Token();
    public BType(Token para_int) {
        this.para_int = para_int;
    }

    public BType() {
    }
}
