package Symbol;

import LLVM.Value;

public class ParaSymbol extends Symbol{//派生类-参数
    public ParaSymbol(String name) {
        super(name, 4, 0);
    }

    public ParaSymbol(String name, Value value) {
        super(name, value);
    }
}
