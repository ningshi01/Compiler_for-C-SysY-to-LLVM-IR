package CompUnit;

import Token.Token;

public class CNumber {
    //Number → IntConst
    Token para_IntConst;

    public CNumber(Token para_IntConst) {
        this.para_IntConst = para_IntConst;
    }

    public CNumber() {
    }
    public int getInt(){
        return Integer.parseInt(para_IntConst.TData);
    }
    public boolean isIntCon(){
        return true;
    }
}
