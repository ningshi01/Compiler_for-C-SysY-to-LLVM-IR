package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class FuncRParams {
    //FuncRParams → Exp { ',' Exp }
    public ArrayList<Exp> arr_Exp;
    ArrayList<Token> arr_COMMA;

    public FuncRParams(ArrayList<Exp> arr_Exp, ArrayList<Token> arr_COMMA) {
        this.arr_Exp = arr_Exp;
        this.arr_COMMA = arr_COMMA;
    }

    public FuncRParams() {
    }
}
