package Semantics;

import Symbol.SymbolTable;
import Token.*;

import java.util.ArrayList;

public class Semantics {
    private static Semantics semantics=new Semantics();
    public static Semantics getInstance(){
        return semantics;
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
    }
    public void MoveDown(){
        cur = cur.back.get(cur.back.size()-1);
    }
    public void MoveToCur(SymbolTable current){
        cur = current;
    }
}
