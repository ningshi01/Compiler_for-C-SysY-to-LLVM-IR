package Symbol;

import CompUnit.FuncRParams;

import java.util.ArrayList;

public class SymbolTable {
    public SymbolTable front;//父表
    public ArrayList<Symbol> lists;//当前表项
    public ArrayList<SymbolTable> back;//子表序列
    public int level;//维度，root--最外层符号表--维度为0
    public boolean waiting;//用来标记函数/过程创建符号表
    public boolean IsWaiting(){
        if (back.size()==0)
            return false;
        return back.get(back.size()-1).waiting;
    }
    public boolean hasReturn;//用来标记是否拥有return语句
    public boolean IsReturn(){
        if (back.size()==0)
            return false;
        return back.get(back.size()-1).hasReturn;
    }
    public void waitTowork(){
        back.get(back.size()-1).waiting = false;
    }
    public SymbolTable() {
        this.lists = new ArrayList<Symbol>();
        this.back = new ArrayList<SymbolTable>();
        this.level = 0;
    }
    //当前表项添加符号
    public void addToLists(Symbol symbol){
        lists.add(symbol);
    }
    //创建子表序列
    public void addToBacks(SymbolTable symbolTable){
        symbolTable.front = this;
        symbolTable.level = this.level+1;
        back.add(symbolTable);
    }
    public void addToBacks(SymbolTable symbolTable, boolean waiting){
        symbolTable.front = this;
        symbolTable.level = this.level+1;
        symbolTable.waiting = true;
        back.add(symbolTable);
    }
    //向上查询到最外层
    public boolean checkToRoot(String point){
        while(true){
            for(Symbol symbol:lists){
                if(point.equals(symbol.Name)){
                    return true;
                }
            }
            if(level == 0)
                return false;
            if(front!=null)
                return front.checkToRoot(point);
        }
    }
    //回归查询到的函数符号
    public FuncSymbol getToRoot_F(String point){
        while(true){
            for(Symbol symbol:lists){
                if(point.equals(symbol.Name)){
                    FuncSymbol s = (FuncSymbol) symbol;
                    return s;
                }
            }
            if(level == 0)
                return null;
            if(front!=null)
                return front.getToRoot_F(point);
        }
    }
    //回归查询到的符号
    public Symbol getToRoot(String point){
        while(true){
            for(Symbol symbol:lists){
                if(point.equals(symbol.Name) && symbol.hasDone){
                    return symbol;
                }
            }
            if(level == 0)
                return null;
            if(front!=null)
                return front.getToRoot(point);
        }
    }
    public SymbolTable backToRootTable_forFunc(){
        while (true) {
            if (front!=null){
                return front.backToRootTable_forFunc();
            }else{
                return this;
            }
        }
    }
    //仅查询同级作用域
    public boolean checkInCur(String point){
        for(Symbol symbol:lists){
            if(point.equals(symbol.Name) && symbol.hasDone){
                return true;
            }
        }
        return false;
    }
    //同级作用域编辑选中符号项
    public void editInCur(String name, Symbol symbol){
        for(int i=lists.size()-1;i>=0;i--){
            if(name.equals(lists.get(i).Name)){
                symbol.RegisterID = lists.get(i).RegisterID;
                lists.set(i,symbol);
                return;
            }
        }
    }
    public void editValue(String name, int value){
        for(int i=lists.size()-1;i>=0;i--){
            if(name.equals(lists.get(i).Name)){
                if(lists.get(i).Type <= 1) {
                    VarSymbol varSymbol = (VarSymbol) lists.get(i);
                    varSymbol.var_num = value;
                    lists.set(i, varSymbol);
                    return;
                }
            }
        }
        if(level == 0)
            return;
        if(front!=null)
            front.editValue(name,value);
    }
    public void editInCur(String name,int RID){
        for(int i=lists.size()-1;i>=0;i--){
            if(name.equals(lists.get(i).Name)){
                Symbol symbol = lists.get(i);
                symbol.RegisterID = RID;
                lists.set(i,symbol);
                return;
            }
        }
    }
    public void ToDone(String name){
        for(int i=lists.size()-1;i>=0;i--){
            if(name.equals(lists.get(i).Name) && lists.get(i).hasDone){
                Symbol symbol = lists.get(i);
                symbol.hasDone = true;
                lists.set(i,symbol);
                return;
            }
        }
    }
    //同级作用域删除选中符号项
    public void deleteInCur(String name){
        for(int i=lists.size()-1;i>=0;i--){
            if(lists.get(i).Name == name){
                lists.remove(i);
                return;
            }
        }
    }
    public void print(){
        if(front==null) {
            System.out.println("root:"+this);
        }
        helpPrint();
        System.out.println("当前表项：");
        for(Symbol symbol:lists){
            helpPrint();
            symbol.print();
        }
        helpPrint();
        System.out.println("子表：");
        if(back.size()!=0){
            for(SymbolTable st:back){
                System.out.println();
                st.print();
            }
        }
    }
    private void helpPrint(){
        for(int i=0;i<this.level;i++){
            System.out.print("----");
        }
    }
}
