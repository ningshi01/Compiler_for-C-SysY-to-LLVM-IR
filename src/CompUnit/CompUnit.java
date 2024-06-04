package CompUnit;

import java.util.ArrayList;

public class CompUnit {
    public ArrayList<Decl> arr_Decl = new ArrayList<>();
    public ArrayList<FuncDef> arr_FuncDef = new ArrayList<>();
    public MainFuncDef para_MainFuncDef = new MainFuncDef();
    public CompUnit(ArrayList<Decl> arr_Decl, ArrayList<FuncDef> arr_FuncDef, MainFuncDef para_MainFuncDef) {
        this.arr_Decl = arr_Decl;
        this.arr_FuncDef = arr_FuncDef;
        this.para_MainFuncDef = para_MainFuncDef;
    }

    public CompUnit() {
    }
}
