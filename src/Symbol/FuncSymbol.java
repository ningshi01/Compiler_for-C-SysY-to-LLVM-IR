package Symbol;

import LLVM.Value;

import java.util.ArrayList;

public class FuncSymbol extends Symbol{//派生类-函数/过程
    //形参个数
    public int para_num;
    public ArrayList<Integer> para_types=new ArrayList<>();

    public FuncSymbol(String name, Value value) {
        super(name, value);
    }

    public FuncSymbol(String name, int pattern, int para_num, ArrayList<Integer> para_types) {
        super(name, 3, pattern);
        this.para_num = para_num;
        this.para_types = para_types;
    }
}
