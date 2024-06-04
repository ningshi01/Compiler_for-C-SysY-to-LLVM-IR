package Symbol;

import LLVM.Value;

public class VarSymbol extends Symbol{//派生类-变量
    //值
    public int var_num;
    /*flag-const
    0-const
    1-int
     */
    public int IsConst;

    public VarSymbol(String name) {
        super(name);
    }

    public VarSymbol(String name, Value value) {
        super(name, value);
    }

    public VarSymbol(String name, int var_num, int isConst) {
        super(name, isConst, 0);
        this.var_num = var_num;
        this.IsConst = isConst;
    }
}
