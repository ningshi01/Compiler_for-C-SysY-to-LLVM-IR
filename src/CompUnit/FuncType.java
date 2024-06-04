package CompUnit;

import Token.Init;
import Token.Token;

public class FuncType {
    //FuncType → 'void' | 'int'
    public Token para_type;

    public FuncType(Token para_type) {
        this.para_type = para_type;
    }

    public FuncType() {
    }
    public int val() {
        if(para_type.type != Init.tokenType.VOIDTK)
            return 1;
        return 0;
    }
}
