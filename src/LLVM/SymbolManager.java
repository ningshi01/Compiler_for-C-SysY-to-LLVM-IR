package LLVM;

import Symbol.SymbolTable;

public class SymbolManager {
    private static SymbolManager symbolManager =new SymbolManager();
    public static SymbolManager getInstance(){
        return symbolManager;
    }
    private static final SymbolTable root = new SymbolTable();
    private SymbolTable cur = null;
    public static SymbolTable getRoot() {
        return root;
    }
    public SymbolTable curnow(){
        if(cur == null) {
            cur=getRoot();
        }
        return cur;
    }
    public void MoveUp(){
        cur = cur.front;
//        cur.back.remove(cur.back.size()-1);
    }
    public void MoveDown(){
        cur = cur.back.get(cur.back.size()-1);
    }
    public void MoveToCur(SymbolTable current){
        cur = current;
    }
}
