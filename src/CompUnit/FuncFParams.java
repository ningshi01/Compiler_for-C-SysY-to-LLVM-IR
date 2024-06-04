package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class FuncFParams {
    //FuncFParams → FuncFParam { ',' FuncFParam }
    public ArrayList<FuncFParam> arr_FuncFParam = new ArrayList<>();
    ArrayList<Token> arr_COMMA = new ArrayList<>();

    public FuncFParams(ArrayList<FuncFParam> arr_FuncFParam, ArrayList<Token> arr_COMMA) {
        this.arr_FuncFParam = arr_FuncFParam;
        this.arr_COMMA = arr_COMMA;
    }

    public FuncFParams() {
    }
}
