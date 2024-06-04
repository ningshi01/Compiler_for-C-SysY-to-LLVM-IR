import CompUnit.*;
import CompUnit.ConstDecl;
import CompUnit.Decl;
import CompUnit.VarDecl;
import CompUnit.FuncDef;
import LLVM.Function;
import LLVM.Value;
import Semantics.Semantics;
import Symbol.*;
import Token.*;

import java.util.ArrayList;

public class Parser {
    private int index;
    private int flag_circulate=0;//循环体中状态标识符
    private Symbol flag_Rpara;//调用函数参数类型标识符
    private boolean flag_in_func=true;//函数分析中状态标识符
    private boolean flag_global_decl=true;//全局变量声明中状态标识符
    private int Now_RID=1;//函数体中当前使用到寄存器ID
    private ArrayList<Token> arr_token=new ArrayList<>();
    private ArrayList<String> arr_Parser=new ArrayList<>();
    private static Semantics semantics = Semantics.getInstance();

    public ArrayList<Function> FuncList = new ArrayList<>();//函数表
    private Function cur_func = new Function();//当前函数
    private Function find_FuncInList(String point){
        for(int i=0;i<FuncList.size();i++) {
            Function e = FuncList.get(i);
            if (e.name.equals(point)) {
                return e;
            }
        }
        return null;
    }
    public Init.tokenType toToken(int off){
        return this.arr_token.get(index+off).type;
    }
    public void nextsym(){
        arr_Parser.add(this.arr_token.get(index).type+" "+this.arr_token.get(index).TData);
//        System.out.println(arr_token.get(index).lineNum);
        if(this.index<this.arr_token.size()-1)
            this.index++;
    }

    private static CompUnit instance = new CompUnit();
    public static CompUnit getInstance(){
        return instance;
    }
    public void analyse(int index, ArrayList<Token> arr_token, ArrayList<String> arr_Parser){
        this.index = index;
        this.arr_token = arr_token;
        this.arr_Parser = arr_Parser;
        instance = A_CompUnit();
//        //打印一下FuncList
//        for (Function f :FuncList){
//            System.out.println("FuncName:"+f.name+"\n"+"FParas:");
//            for(Symbol para:f.paraList){
//                System.out.println("类型："+para.returnType+"          种类："+para.selfType+"            名字："+para.Name);
//            }
//            System.out.println();
//        }
    }
    public CompUnit A_CompUnit(){
        //CompUnit -> {Decl} {FuncDef} MainFuncDef
        ArrayList<Decl> arr_Decl = new ArrayList<>();
        ArrayList<FuncDef> arr_FuncDef = new ArrayList<>();
        MainFuncDef para_MainFuncDef = new MainFuncDef();
        while(toToken(1)!= Init.tokenType.MAINTK && toToken(2)!= Init.tokenType.LPARENT){
            arr_Decl.add(A_Decl());
        }
        flag_global_decl = false;
        while(toToken(1)!= Init.tokenType.MAINTK){
            arr_FuncDef.add(A_FuncDef());
            Now_RID = 1;
        }
        para_MainFuncDef = A_MainFuncDef();
        arr_Parser.add("<CompUnit>");
        return new CompUnit(arr_Decl,arr_FuncDef,para_MainFuncDef);
    }
    public Decl A_Decl(){
        //Decl -> ConstDecl | VarDecl
        ConstDecl para_ConstDecl = new ConstDecl();
        VarDecl para_VarDecl = new VarDecl();
        if(toToken(0)== Init.tokenType.CONSTTK){
            para_ConstDecl = A_ConstDecl();
        }else if(toToken(0)== Init.tokenType.INTTK){
            para_VarDecl = A_VarDecl();
        }else{}
//        arr_Parser.add("<Decl>");
        return new Decl(para_ConstDecl,para_VarDecl);
    }
    public ConstDecl A_ConstDecl(){
        //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        Token para_const=new Token();
        BType para_BType=new BType();
        ArrayList<ConstDef> arr_ConstDef= new ArrayList<ConstDef>();
        ArrayList<Token> arr_COMMA = new ArrayList<Token>();
        Token para_SEMICN = new Token();
        if(toToken(0)== Init.tokenType.CONSTTK) {
            para_const = arr_token.get(index);
            nextsym();
        }
        para_BType = A_BType();
        arr_ConstDef.add(A_ConstDef());
        while(toToken(0)== Init.tokenType.COMMA){
            arr_COMMA.add(arr_token.get(index));
            nextsym();
            arr_ConstDef.add(A_ConstDef());
        }
        if(toToken(0)== Init.tokenType.SEMICN) {
            para_SEMICN = arr_token.get(index);
            nextsym();
        }else{
            //Error-i
            para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
            Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
        }
        arr_Parser.add("<ConstDecl>");
        return new ConstDecl(para_const,para_BType,arr_ConstDef,arr_COMMA,para_SEMICN);
    }
    public BType A_BType(){
        //BType → 'int'
        Token para_int = new Token();
        para_int = arr_token.get(index);
        nextsym();
//        arr_Parser.add("<>");
        return new BType(para_int);
    }
    public ConstDef A_ConstDef(){
        //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        Token para_Ident = new Token();
        ArrayList<Token> arr_LBRACK = new ArrayList<>();
        ArrayList<ConstExp> arr_ConstExp = new ArrayList<>();
        ArrayList<Token> arr_RBRACK = new ArrayList<>();
        Token para_ASSIGN = new Token();
        ConstInitVal para_ConstInitVal = new ConstInitVal();
        if(toToken(0)== Init.tokenType.IDENFR){
            para_Ident = arr_token.get(index);
            nextsym();
        }
        //符号占位
        String name = para_Ident.TData;
        //Error-b
        if(semantics.curnow().checkInCur(name)){
            Errors._b(Compiler._Error(),para_Ident.lineNum);
            name = "error_Const";
        }
        semantics.curnow().addToLists(new Symbol(name));

        while(toToken(0)== Init.tokenType.LBRACK){
            arr_LBRACK.add(arr_token.get(index));
            nextsym();
            arr_ConstExp.add(A_ConstExp());
            //Error-k
            if (toToken(0)!= Init.tokenType.RBRACK){
                arr_RBRACK.add(new Token("]",Init.tokenType.RBRACK,arr_token.get(index-1).lineNum));
                Errors._k(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                arr_RBRACK.add(arr_token.get(index));
                nextsym();
            }
        }
        para_ASSIGN = arr_token.get(index);
        nextsym();
        para_ConstInitVal = A_ConstInitVal();
        arr_Parser.add("<ConstDef>");
        if (arr_LBRACK.size() == 0) {
            //const定义单个常量
            Value v = new Value();{
                v.selfType = Value.SelfType.CONST;
                v.returnValue = para_ConstInitVal.getInt();
                v.returnType = Value.ReturnType.INT;
            }
            semantics.curnow().editInCur(name,new Symbol(name,v));
        } else {
            //const定义数组
            Value v = new Value();
            {
                v.selfType = Value.SelfType.ARRAY;
                v.Array_Const = true;
                arr_ConstExp.forEach(e -> {
                    v.selfLevels.add(e.getInt());
                });
                v.returnType = Value.ReturnType.INT;
                para_ConstInitVal.getNums(v.returnNums);
            }
            semantics.curnow().editInCur(name,new Symbol(name,v));
        }
        semantics.curnow().deleteInCur("error_Const");
        semantics.curnow().ToDone(name);//标识已经结束添加符号
        return new ConstDef(para_Ident,arr_LBRACK,arr_ConstExp,arr_RBRACK,para_ASSIGN,para_ConstInitVal);
    }
    public ConstInitVal A_ConstInitVal(){
        //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        ConstExp para_ConstExp = new ConstExp();
        Token para_LBRACE = new Token();
        ArrayList<ConstInitVal> arr_ConstInitVal = new ArrayList<>();
        ArrayList<Token> arr_COMMA=new ArrayList<>();
        Token para_RBRACE = new Token();
        if(toToken(0)== Init.tokenType.LBRACE){
            para_LBRACE = arr_token.get(index);
            nextsym();
            if(toToken(0)!= Init.tokenType.RBRACE){
                arr_ConstInitVal.add(A_ConstInitVal());
                while(toToken(0)== Init.tokenType.COMMA){
                    arr_COMMA.add(arr_token.get(index));
                    nextsym();
                    arr_ConstInitVal.add(A_ConstInitVal());
                }
            }
            para_RBRACE = arr_token.get(index);
            nextsym();
        }else{
            para_ConstExp = A_ConstExp();
        }
        arr_Parser.add("<ConstInitVal>");
        return new ConstInitVal(para_ConstExp,para_LBRACE,arr_ConstInitVal,arr_COMMA,para_RBRACE);
    }
    public VarDecl A_VarDecl(){
        //VarDecl → BType VarDef { ',' VarDef } ';'
        BType para_BType = new BType();
        ArrayList<VarDef> arr_VarDef = new ArrayList();
        ArrayList<Token> arr_COMMA = new ArrayList<>();
        Token para_SEMICN = new Token();
        para_BType = A_BType();
        arr_VarDef.add(A_VarDef());
        while(toToken(0)== Init.tokenType.COMMA){
            arr_COMMA.add(arr_token.get(index));
            nextsym();
            arr_VarDef.add(A_VarDef());
        }
        //Error-i
        if(toToken(0)!= Init.tokenType.SEMICN){
            para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
            Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
        }else {
            para_SEMICN = arr_token.get(index);
            nextsym();
        }
        arr_Parser.add("<VarDecl>");
        return new VarDecl(para_BType,arr_VarDef,arr_COMMA,para_SEMICN);
    }
    public VarDef A_VarDef(){
        //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        Token para_Ident = new Token();
        ArrayList<Token> arr_LBRACK = new ArrayList<>();
        ArrayList<ConstExp> arr_ConstExp = new ArrayList<>();
        ArrayList<Token> arr_RBRACK = new ArrayList<>();
        Token para_ASSIGN = new Token();
        InitVal para_InitVal = new InitVal();
        para_Ident = arr_token.get(index);
        nextsym();
        //符号占位
        String name = para_Ident.TData;
        //Error-b
        if(semantics.curnow().checkInCur(name)){
            Errors._b(Compiler._Error(),para_Ident.lineNum);
            name = "error_Var";
        }
        semantics.curnow().addToLists(new Symbol(name));

        while(toToken(0)== Init.tokenType.LBRACK){
            arr_LBRACK.add(arr_token.get(index));
            nextsym();
            arr_ConstExp.add(A_ConstExp());
            //Error-k
            if (toToken(0)!= Init.tokenType.RBRACK){
                arr_RBRACK.add(new Token("]",Init.tokenType.RBRACK,arr_token.get(index-1).lineNum));
                Errors._k(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                arr_RBRACK.add(arr_token.get(index));
                nextsym();
            }
        }
        if(toToken(0)== Init.tokenType.ASSIGN){
            para_ASSIGN = arr_token.get(index);
            nextsym();
            para_InitVal = A_InitVal();
        }
        arr_Parser.add("<VarDef>");
        if (arr_LBRACK.size() == 0) {
            //int定义单个变量
            Value v = new Value();{
                v.selfType = Value.SelfType.VAR;
                v.returnValue = para_InitVal.getInt();
                v.returnType = Value.ReturnType.INT;
            }
            semantics.curnow().editInCur(name,new Symbol(name,v));
        } else {
            //int定义数组
            Value v = new Value();{
                v.selfType = Value.SelfType.ARRAY;
                v.returnType = Value.ReturnType.INT;
                arr_ConstExp.forEach(e -> {
                    v.selfLevels.add(e.getInt());
                });
                if(para_InitVal.para_Exp!=null){//如果有初值
                    para_InitVal.getNums(v.returnNums);
                }
            }
            semantics.curnow().editInCur(name,new Symbol(name,v));
        }
        semantics.curnow().deleteInCur("error_Var");
        semantics.curnow().ToDone(name);//标识已经结束添加符号
        return new VarDef(para_Ident,arr_LBRACK,arr_ConstExp,arr_RBRACK,para_ASSIGN,para_InitVal);
    }
    public InitVal A_InitVal(){
        //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        Exp para_Exp = new Exp();
        Token para_LBRACE = new Token();
        ArrayList<InitVal> arr_InitVal = new ArrayList<>();
        ArrayList<Token> arr_COMMA = new ArrayList<>();
        Token para_RBRACE = new Token();
        if(toToken(0)== Init.tokenType.LBRACE){
            para_LBRACE = arr_token.get(index);
            nextsym();
            if(toToken(0)!= Init.tokenType.RBRACE){
                arr_InitVal.add(A_InitVal());
                while(toToken(0)== Init.tokenType.COMMA){
                    arr_COMMA.add(arr_token.get(index));
                    nextsym();
                    arr_InitVal.add(A_InitVal());
                }
            }
            para_RBRACE = arr_token.get(index);
            nextsym();
        }else{
            para_Exp = A_Exp();
        }
        arr_Parser.add("<InitVal>");
        return new InitVal(para_Exp,para_LBRACE,arr_InitVal,arr_COMMA,para_RBRACE);
    }
    public FuncDef A_FuncDef(){
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncType para_FuncType = new FuncType();
        Token para_Ident = new Token();
        Token para_LPARENT = new Token();
        FuncFParams para_FuncFParams = new FuncFParams();
        Token para_RPARENT = new Token();
        Block para_Block = new Block();
        para_FuncType = A_FuncType();
        para_Ident = arr_token.get(index);
        //定义函数体/过程
        Function f = new Function();{
            f.name = para_Ident.TData;
            if(para_FuncType.para_type.type == Init.tokenType.INTTK) {
                f.retType = Value.ReturnType.INT;
            }else if(para_FuncType.para_type.type == Init.tokenType.VOIDTK){
                f.retType = Value.ReturnType.VOID;
            }
        }cur_func = f;
        //定义一个函数符号
        Value v = new Value();{
            v.selfType = Value.SelfType.FUNC;
            if(para_FuncType.para_type.type == Init.tokenType.INTTK) {
                v.returnType = Value.ReturnType.INT;
            }else if(para_FuncType.para_type.type == Init.tokenType.VOIDTK){
                v.returnType = Value.ReturnType.VOID;
            }
        }
        //添加当前符号表项
        String name = para_Ident.TData;
        int pattern = para_FuncType.val();
        int para_num = 0;
        ArrayList<Integer> para_types=new ArrayList<>();
        //Error-b
        if(semantics.curnow().checkInCur(name)){
            Errors._b(Compiler._Error(),para_Ident.lineNum);
            f.name = "error_Func";
        }
        semantics.curnow().addToLists(new Symbol(name,v));
        //添加子表并标记为waiting
        semantics.curnow().addToBacks(new SymbolTable(),true);


        nextsym();
        para_LPARENT = arr_token.get(index);
        nextsym();
        if(toToken(0)== Init.tokenType.INTTK){
            semantics.MoveDown();//进入刚创建的符号表
            para_FuncFParams = A_FuncFParams();
            //在退出子表前把参数压入paraList
            for(Symbol paraSymbol:semantics.curnow().lists){
                f.paraList.add(paraSymbol);
            }
            semantics.MoveUp();//退出刚创建的符号表
        }
        cur_func = f;
        FuncList.add(cur_func);//先加进去占位防止函数内调用本体
        //Error-j
        if (toToken(0)!= Init.tokenType.RPARENT){
            para_RPARENT = new Token(")",Init.tokenType.RPARENT,arr_token.get(index-1).lineNum);
            Errors._j(Compiler._Error(),arr_token.get(index-1).lineNum);
        }else {
            para_RPARENT = arr_token.get(index);
            nextsym();
        }
        para_num = para_FuncFParams.arr_FuncFParam.size();
        if(para_num>0) {
            para_FuncFParams.arr_FuncFParam.forEach(e -> {
                para_types.add(e.type);
            });
        }
        flag_in_func = true;
        para_Block = A_Block();
        flag_in_func = false;
        //Error-g
        if(!cur_func.hasReturn){
            Function ff =cur_func;
            if(ff.retType == Value.ReturnType.INT)
                Errors._g(Compiler._Error(),arr_token.get(index-1).lineNum);
        }
        arr_Parser.add("<FuncDef>");
        semantics.curnow().deleteInCur("error_Func");
        if(f.name.equals("error_Func")){
            FuncList.remove(cur_func);//从函数表中弹出
        }
        return new FuncDef(para_FuncType,para_Ident,para_LPARENT,para_FuncFParams,para_RPARENT,para_Block);
    }
    public MainFuncDef A_MainFuncDef(){
        //MainFuncDef → 'int' 'main' '(' ')' Block
        Token para_int;
        Token para_main;
        Token para_LPARENT;
        Token para_RPARENT;
        Block para_Block;

        //定义Main函数体/过程
        //添加当前符号表项
        String name = "Main";
        int pattern = 0;
        int para_num = 0;
        ArrayList<Integer> para_types=new ArrayList<>();
        Function f = new Function();{
            f.name = "main";
            f.retType = Value.ReturnType.INT;
        }
        //定义一个main函数符号
        Value v = new Value();{
            v.selfType = Value.SelfType.FUNC;
            v.returnType = Value.ReturnType.INT;
        }
        semantics.curnow().addToLists(new Symbol("main",v));
        semantics.curnow().addToBacks(new SymbolTable(),true);
        cur_func = f;

        para_int = arr_token.get(index);nextsym();
        para_main = arr_token.get(index);nextsym();
        para_LPARENT = arr_token.get(index);nextsym();
        //Error-j
        if (toToken(0)!= Init.tokenType.RPARENT){
            para_RPARENT = new Token(")",Init.tokenType.RPARENT,arr_token.get(index-1).lineNum);
            Errors._j(Compiler._Error(),arr_token.get(index-1).lineNum);
        }else {
            para_RPARENT = arr_token.get(index);
            nextsym();
        }
        para_Block = A_Block();
        arr_Parser.add("<MainFuncDef>");

        //Error-g
        if(!cur_func.hasReturn){
            if(cur_func.retType == Value.ReturnType.INT)
                Errors._g(Compiler._Error(),arr_token.get(index).lineNum);
        }
        FuncList.add(cur_func);
        return new MainFuncDef(para_int,para_main,para_LPARENT,para_RPARENT,para_Block);
    }
    public FuncType A_FuncType(){
        //FuncType → 'void' | 'int'
        Token para_type=new Token();
        para_type = arr_token.get(index);
        nextsym();
        arr_Parser.add("<FuncType>");
        return new FuncType(para_type);
    }
    public FuncFParams A_FuncFParams(){
        //FuncFParams → FuncFParam { ',' FuncFParam }
        ArrayList<FuncFParam> arr_FuncFParam = new ArrayList<>();
        ArrayList<Token> arr_COMMA = new ArrayList<>();
        arr_FuncFParam.add(A_FuncFParam());
        while(toToken(0)== Init.tokenType.COMMA){
            arr_COMMA.add(arr_token.get(index));
            nextsym();
            arr_FuncFParam.add(A_FuncFParam());
        }
        arr_Parser.add("<FuncFParams>");
        return new FuncFParams(arr_FuncFParam,arr_COMMA);
    }
    public FuncFParam A_FuncFParam(){
        //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        BType para_BType;
        Token para_Ident;
        ArrayList<Token> arr_LBRACK=new ArrayList<>();
        ArrayList<Token> arr_RBRACK=new ArrayList<>();
        ArrayList<ConstExp> arr_ConstExp=new ArrayList<>();
        int type = 0;
        para_BType = A_BType();
        para_Ident = arr_token.get(index);
        nextsym();
        //符号占位
        String name = para_Ident.TData;
        //Error-b
        if(semantics.curnow().checkInCur(name)){
            name = "error_FPara";
            Errors._b(Compiler._Error(),para_Ident.lineNum);
        }
        semantics.curnow().addToLists(new Symbol(name));

        if(toToken(0)== Init.tokenType.LBRACK){
            type = 1;
            arr_LBRACK.add(arr_token.get(index));
            nextsym();
            //Error-k
            if (toToken(0)!= Init.tokenType.RBRACK){
                arr_RBRACK.add(new Token("]",Init.tokenType.RBRACK,arr_token.get(index-1).lineNum));
                Errors._k(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                arr_RBRACK.add(arr_token.get(index));
                nextsym();
            }
            while(toToken(0)== Init.tokenType.LBRACK){
                type = 2;
                arr_LBRACK.add(arr_token.get(index));
                nextsym();
                arr_ConstExp.add(A_ConstExp());
                //Error-k
                if (toToken(0)!= Init.tokenType.RBRACK){
                    arr_RBRACK.add(new Token("]",Init.tokenType.RBRACK,arr_token.get(index-1).lineNum));
                    Errors._k(Compiler._Error(),arr_token.get(index-1).lineNum);
                }else {
                    arr_RBRACK.add(arr_token.get(index));
                    nextsym();
                }
            }
        }
        arr_Parser.add("<FuncFParam>");
        //定义函数参数
        //添加当前符号表项
        if(arr_LBRACK.size()==0){
            //para定义单个常量
            Value v = new Value();
            {
                v.selfType = Value.SelfType.PARA;
                v.returnType = Value.ReturnType.INT;
            }
            semantics.curnow().editInCur(name,new Symbol(name,v));
        }else{
            //para定义数组
            Value v = new Value();
            {
                v.selfType = Value.SelfType.PARA_ARRAY;
                v.returnType = Value.ReturnType.INT;
                v.selfLevels.add(0);//填充参数-数组的第一维
                arr_ConstExp.forEach(e->{
                    v.selfLevels.add(e.getInt());
                });
            }
            semantics.curnow().editInCur(name,new Symbol(name,v));
        }
        semantics.curnow().deleteInCur("error_FPara");
        semantics.curnow().ToDone(name);//标识已经结束添加符号
        return new FuncFParam(para_BType,para_Ident,arr_LBRACK,arr_RBRACK,arr_ConstExp,type);
    }
    public Block A_Block(){
        //Block → '{' { BlockItem } '}'
        Token para_LBRACE;
        ArrayList<BlockItem> arr_BlockItem = new ArrayList<>();
        Token para_RBRACE;
        para_LBRACE = arr_token.get(index);

        //先搜索上一个表是否处于waiting
        if(semantics.curnow().IsWaiting()){
            semantics.curnow().waitTowork();
        }else{
            //为当前表项创建新的子表
            semantics.curnow().addToBacks(new SymbolTable());
        }
        //进入新的子表(自动进入最新创建的子表)
        semantics.MoveDown();

        nextsym();
        while(toToken(0)!= Init.tokenType.RBRACE){
            arr_BlockItem.add(A_BlockItem());
        }
        para_RBRACE = arr_token.get(index);
        nextsym();

        //回到父表
        semantics.MoveUp();

        arr_Parser.add("<Block>");
        return new Block(para_LBRACE,arr_BlockItem,para_RBRACE);
    }
    public BlockItem A_BlockItem(){
        //BlockItem → Decl | Stmt
        Decl para_Decl=null;
        Stmt para_Stmt=null;
        if(toToken(0)== Init.tokenType.CONSTTK || toToken(0)== Init.tokenType.INTTK){
            para_Decl = A_Decl();
        }else{
            para_Stmt = A_Stmt();
        }
//        arr_Parser.add("<BlockItem>");
        return new BlockItem(para_Decl,para_Stmt);
    }
    public Stmt A_Stmt(){
        // Stmt →
        if(toToken(0) == Init.tokenType.LBRACE){
            //3 Block
            Block para_Block = A_Block();
            arr_Parser.add("<Stmt>");
            return new Stmt(3,para_Block);
        } else if (toToken(0) == Init.tokenType.IFTK) {
            //4 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
            Token para_IFTK;
            Token para_LPARENT;
            Cond para_Cond;
            Token para_RPARENT;
            Stmt para_Stmt1;
            Token para_ELSETK = new Token();
            Stmt para_Stmt2 = new Stmt();
            para_IFTK = arr_token.get(index);nextsym();
            para_LPARENT = arr_token.get(index);nextsym();
            para_Cond = A_Cond();
            //Error-j
            if (toToken(0)!= Init.tokenType.RPARENT){
                para_RPARENT = new Token(")",Init.tokenType.RPARENT,arr_token.get(index-1).lineNum);
                Errors._j(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_RPARENT = arr_token.get(index);
                nextsym();
            }
            para_Stmt1 = A_Stmt();
            if(toToken(0)== Init.tokenType.ELSETK) {
                para_ELSETK = arr_token.get(index);
                nextsym();
                para_Stmt2 = A_Stmt();
            }
            arr_Parser.add("<Stmt>");
            return new Stmt(4,para_IFTK,para_LPARENT,para_Cond,para_RPARENT,para_Stmt1,para_ELSETK,para_Stmt2);
        }else if (toToken(0)== Init.tokenType.FORTK){
            //5 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
            Token para_FORTK;
            Token para_LPARENT;
            ForStmt para_ForStmt1=new ForStmt();
            Token para_SEMICN1;
            Cond para_Cond=new Cond();
            Token para_SEMICN2;
            ForStmt para_ForStmt2=new ForStmt();
            Token para_RPARENT;
            Stmt para_Stmt;
            para_FORTK = arr_token.get(index);nextsym();
            para_LPARENT = arr_token.get(index);nextsym();
            if(toToken(0)!= Init.tokenType.SEMICN){
                para_ForStmt1 = A_ForStmt();
            }
            para_SEMICN1 = arr_token.get(index);
            nextsym();
            if(toToken(0)!= Init.tokenType.SEMICN) {
                para_Cond = A_Cond();
            }
            para_SEMICN2 = arr_token.get(index);
            nextsym();
            if(toToken(0)!= Init.tokenType.RPARENT) {
                para_ForStmt2 = A_ForStmt();
            }
            para_RPARENT = arr_token.get(index);
            nextsym();
            flag_circulate ++;
            para_Stmt = A_Stmt();
            flag_circulate --;
            arr_Parser.add("<Stmt>");
            return new Stmt(5,para_FORTK,para_LPARENT,para_ForStmt1,para_SEMICN1,para_Cond,para_SEMICN2,para_ForStmt2,para_RPARENT,para_Stmt);
        }else if (toToken(0)== Init.tokenType.BREAKTK){
            //6 'break' ';'
            Token para_BREAKTK;
            Token para_SEMICN;
            para_BREAKTK = arr_token.get(index);nextsym();
            //Error-m
            if(flag_circulate==0){
                Errors._m(Compiler._Error(),para_BREAKTK.lineNum);
            }
            //Error-i
            if(toToken(0)!= Init.tokenType.SEMICN){
                para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
                Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_SEMICN = arr_token.get(index);
                nextsym();
            }
            arr_Parser.add("<Stmt>");
            return new Stmt(6,para_BREAKTK,para_SEMICN);
        }else if (toToken(0)== Init.tokenType.CONTINUETK){
            //7 'continue' ';'
            Token para_CONTINUETK;
            Token para_SEMICN;
            para_CONTINUETK = arr_token.get(index);nextsym();
            //Error-m
            if(flag_circulate==0){
                Errors._m(Compiler._Error(),para_CONTINUETK.lineNum);
            }
            //Error-i
            if(toToken(0)!= Init.tokenType.SEMICN){
                para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
                Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_SEMICN = arr_token.get(index);
                nextsym();
            }
            arr_Parser.add("<Stmt>");
            return new Stmt(7,para_CONTINUETK,para_SEMICN);
        }else if (toToken(0)== Init.tokenType.RETURNTK){
            //8 'return' [Exp] ';' // 1.有Exp 2.无Exp
            Token para_RETURNTK;
            Exp para_Exp=new Exp();
            Token para_SEMICN;
            para_RETURNTK = arr_token.get(index);nextsym();
            if(toToken(0)!= Init.tokenType.SEMICN && toToken(0)!= Init.tokenType.RBRACE) {
                cur_func.hasReturn = true;
                //Error-f
                Function f = cur_func;
                para_Exp = A_Exp();
                if(f.retType == Value.ReturnType.VOID) {
                    Errors._f(Compiler._Error(), para_RETURNTK.lineNum);
                    para_Exp = null;
                }
            }
            //Error-i
            if(toToken(0)!= Init.tokenType.SEMICN){
                para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
                Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_SEMICN = arr_token.get(index);
                nextsym();
            }
            arr_Parser.add("<Stmt>");
            return new Stmt(8,para_RETURNTK,para_Exp,para_SEMICN);
        } else if (toToken(0)== Init.tokenType.PRINTFTK) {
            //10 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
            Token para_PRINTFTK;
            Token para_LPARENT;
            Token para_FormatString;
            ArrayList<Token> arr_COMMA=new ArrayList<>();
            ArrayList<Exp> arr_Exp = new ArrayList<>();
            Token para_RPARENT;
            Token para_SEMICN;
            para_PRINTFTK = arr_token.get(index);nextsym();
            para_LPARENT = arr_token.get(index);nextsym();
            para_FormatString = arr_token.get(index);nextsym();
            while(toToken(0)!= Init.tokenType.RPARENT && toToken(0)!= Init.tokenType.SEMICN){
                arr_COMMA.add(arr_token.get(index));nextsym();
                arr_Exp.add(A_Exp());
            }
            //Error-j
            if (toToken(0)!= Init.tokenType.RPARENT){
                para_RPARENT = new Token(")",Init.tokenType.RPARENT,arr_token.get(index-1).lineNum);
                Errors._j(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_RPARENT = arr_token.get(index);
                nextsym();
            }
            //Error-i
            if(toToken(0)!= Init.tokenType.SEMICN){
                para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
                Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_SEMICN = arr_token.get(index);
                nextsym();
            }
            //Error-l
            if(arr_Exp.size()!=para_FormatString.FormatNum){
                Errors._l(Compiler._Error(),para_PRINTFTK.lineNum);
            }
            arr_Parser.add("<Stmt>");
            return new Stmt(10,para_PRINTFTK,para_LPARENT,para_FormatString,arr_COMMA,arr_Exp,para_RPARENT,para_SEMICN);
        } else{
            //1/2/9三种情况的讨论
            boolean flag = false;
            for (int i=index;i<arr_token.size()-1;i++){
                if(arr_token.get(i).lineNum==arr_token.get(index).lineNum && i>=index){
                    //在当前识别Token的同一行的后边单词遍历
                    if(arr_token.get(i).type== Init.tokenType.ASSIGN){
                        //如果存在'=',转1/9
                        flag = true;
                    }
                    if(arr_token.get(i).type== Init.tokenType.SEMICN){
                        //如果遍历到';',退出遍历
                        break;
                    }
                }
                if(arr_token.get(i).lineNum>arr_token.get(index).lineNum){
                    //如果遍历到下一行,退出遍历
                    break;
                }
            }
            Token para_SEMICN;
            if(flag == false){
                //2 [Exp] ';' //有无Exp
                Exp para_Exp=new Exp();
                if(toToken(0)!= Init.tokenType.SEMICN){
                    para_Exp = A_Exp();
                }
                //Error-i
                if(toToken(0)!= Init.tokenType.SEMICN){
                    para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
                    Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
                }else {
                    para_SEMICN = arr_token.get(index);
                    nextsym();//保留语句
                }
                arr_Parser.add("<Stmt>");
                return new Stmt(2,para_Exp,para_SEMICN);
            }else{//已知是1/9情况
                LVal para_LVal;
                para_LVal = A_LVal();//先分析一个LVal
                if(toToken(1)!=Init.tokenType.GETINTTK){//后一个单词不是'getint'
                    //1 LVal '=' Exp ';' // 每种类型的语句都要覆盖
                    Token para_ASSIGN;
                    Exp para_Exp;
                    para_ASSIGN = arr_token.get(index);nextsym();
                    para_Exp = A_Exp();
                    //Error-i
                    if(toToken(0)!= Init.tokenType.SEMICN){
                        para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
                        Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
                    }else {
                        para_SEMICN = arr_token.get(index);
                        nextsym();
                    }
                    arr_Parser.add("<Stmt>");
                    if(flag_global_decl)
                        semantics.curnow().editValue(para_LVal.para_Ident.TData,para_Exp.getInt());
                    return new Stmt(1,para_LVal,para_ASSIGN,para_Exp,para_SEMICN);
                }else{//后一个单词是'getint'
                    //9 LVal '=' 'getint''('')'';'
                    Token para_ASSIGN;
                    Token para_GETINTTK;
                    Token para_LPARENT;
                    Token para_RPARENT;
                    para_ASSIGN = arr_token.get(index);nextsym();
                    para_GETINTTK = arr_token.get(index);nextsym();
                    para_LPARENT = arr_token.get(index);nextsym();
                    //Error-j
                    if (toToken(0)!= Init.tokenType.RPARENT){
                        para_RPARENT = new Token(")",Init.tokenType.RPARENT,arr_token.get(index-1).lineNum);
                        Errors._j(Compiler._Error(),arr_token.get(index-1).lineNum);
                    }else {
                        para_RPARENT = arr_token.get(index);
                        nextsym();
                    }
                    //Error-i
                    if(toToken(0)!= Init.tokenType.SEMICN){
                        para_SEMICN = new Token(";",Init.tokenType.SEMICN,arr_token.get(index-1).lineNum);
                        Errors._i(Compiler._Error(),arr_token.get(index-1).lineNum);
                    }else {
                        para_SEMICN = arr_token.get(index);
                        nextsym();
                    }
                    arr_Parser.add("<Stmt>");
                    return new Stmt(9,para_LVal,para_ASSIGN,para_GETINTTK,para_LPARENT,para_RPARENT,para_SEMICN);
                }
            }
        }
    }
    public Exp A_Exp(){
        //Exp → AddExp
        AddExp para_AddExp;
        flag_Rpara = null;
        para_AddExp = A_AddExp();
        Symbol Rpara = flag_Rpara;
        arr_Parser.add("<Exp>");
        return new Exp(para_AddExp,Rpara);
    }
    public Cond A_Cond(){
        // Cond → LOrExp
        LOrExp para_LOrExp;
        para_LOrExp = A_LOrExp();
        arr_Parser.add("<Cond>");
        return new Cond(para_LOrExp);
    }
    public ForStmt A_ForStmt(){
        //ForStmt → LVal '=' Exp
        LVal para_LVal;
        Token para_ASSIGN;
        Exp para_Exp;
        para_LVal = A_LVal();
        para_ASSIGN = arr_token.get(index);nextsym();
        para_Exp = A_Exp();
        arr_Parser.add("<ForStmt>");
        return new ForStmt(para_LVal,para_ASSIGN,para_Exp);
    }
    public LVal A_LVal(){
        //LVal → Ident {'[' Exp ']'}
        Token para_Ident;
        ArrayList<Token> arr_LBRACK= new ArrayList<>();
        ArrayList<Exp> arr_Exp= new ArrayList<>();
        ArrayList<Token> arr_RBRACK= new ArrayList<>();
        para_Ident = arr_token.get(index);nextsym();
        while(toToken(0)== Init.tokenType.LBRACK){
            arr_LBRACK.add(arr_token.get(index));nextsym();
            arr_Exp.add(A_Exp());
            //Error-k
            if (toToken(0)!= Init.tokenType.RBRACK){
                arr_RBRACK.add(new Token("]",Init.tokenType.RBRACK,arr_token.get(index-1).lineNum));
                Errors._k(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                arr_RBRACK.add(arr_token.get(index));
                nextsym();
            }
        }
        //Error-c
        String name = para_Ident.TData;
        if(!semantics.curnow().checkToRoot(name)){
            Errors._c(Compiler._Error(),para_Ident.lineNum);
        }else{
            //Error-h
            if(toToken(0)==Init.tokenType.ASSIGN){
                Symbol s = semantics.curnow().getToRoot(para_Ident.TData);
                if(s.selfType == Value.SelfType.CONST || s.Array_Const == true){
                    Errors._h(Compiler._Error(),para_Ident.lineNum);
                }
            }
        }
        if(semantics.curnow().checkToRoot(name)){
            Symbol s =  semantics.curnow().getToRoot(name);//把这个找到的符号进行辨别
            Symbol re = new Symbol("");//用来存结果的符号
            if(s.selfType == Value.SelfType.VAR||s.selfType== Value.SelfType.PARA||s.selfType == Value.SelfType.CONST)
                flag_Rpara = s;
            else{//说明s是数组
                if(s.selfLevels.size()==1){//如果是一维数组
                    if(arr_LBRACK.size()==0){//一维取一维
                        flag_Rpara = s;
                    }else if(arr_LBRACK.size()==1){//一维取值
                        re.selfType = Value.SelfType.VAR;
                        flag_Rpara = re;
                    }
                }else if(s.selfLevels.size()==2){
                    if(arr_LBRACK.size()==2){//二维取值
                        re.selfType = Value.SelfType.VAR;
                        flag_Rpara = re;
                    }else if(arr_LBRACK.size()==1){//二维取一维
                        re.selfType = Value.SelfType.ARRAY;
                        re.selfLevels.add(s.selfLevels.get(1));//加原二维数组的第二维
                        flag_Rpara = re;
                    }else if(arr_LBRACK.size()==0){//二维取二维
                        flag_Rpara = s;
                    }
                }
            }
        }
        arr_Parser.add("<LVal>");
        return new LVal(para_Ident,arr_LBRACK,arr_Exp,arr_RBRACK);
    }
    public PrimaryExp A_PrimaryExp(){
        //PrimaryExp → '(' Exp ')' | LVal | Number
        Token para_LPARENT=new Token();
        Exp para_Exp=new Exp();
        Token para_RPARENT=new Token();
        LVal para_LVal=new LVal();
        CNumber para_CNumber=new CNumber();
        if(toToken(0)== Init.tokenType.LPARENT){
            para_LPARENT = arr_token.get(index);nextsym();
            para_Exp = A_Exp();
            //Error-j
            if (toToken(0)!= Init.tokenType.RPARENT){
                para_RPARENT = new Token(")",Init.tokenType.RPARENT,arr_token.get(index-1).lineNum);
                Errors._j(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_RPARENT = arr_token.get(index);
                nextsym();
            }
        } else if (toToken(0)== Init.tokenType.INTCON) {
            para_CNumber = A_CNumber();
        } else{
            para_LVal = A_LVal();
        }
        arr_Parser.add("<PrimaryExp>");
        return new PrimaryExp(para_LPARENT,para_Exp,para_RPARENT,para_LVal,para_CNumber);
    }
    public CNumber A_CNumber(){
        //Number → IntConst
        Token para_IntConst;
        para_IntConst = arr_token.get(index);nextsym();
        arr_Parser.add("<Number>");
        return new CNumber(para_IntConst);
    }
    public UnaryExp A_UnaryExp(){
        //UnaryExp →
        PrimaryExp para_PrimaryExp=new PrimaryExp();
        Token para_Ident=new Token();
        Token para_LPARENT=new Token();
        FuncRParams para_FuncRParams=new FuncRParams();
        Token para_RPARENT=new Token();
        UnaryOp para_UnaryOp=new UnaryOp();
        UnaryExp para_UnaryExp=new UnaryExp();
        if(toToken(0)== Init.tokenType.IDENFR && toToken(1) == Init.tokenType.LPARENT){
            // Ident '(' [FuncRParams] ')'
            para_Ident = arr_token.get(index);nextsym();

            //Error-c
            String name = para_Ident.TData;
            if(!semantics.curnow().checkToRoot(name)){
                Errors._c(Compiler._Error(),para_Ident.lineNum);
            }

            para_LPARENT = arr_token.get(index);nextsym();
             if(toToken(0) != Init.tokenType.RPARENT && toToken(0)!= Init.tokenType.SEMICN){
                para_FuncRParams = A_FuncRParams();
            }
            if(semantics.curnow().checkToRoot(name)){
                Function f = find_FuncInList(name);
                if (para_FuncRParams.arr_Exp!=null){
                    if (para_FuncRParams.arr_Exp.size() != f.paraList.size()) {
                        //Error-d
                        Errors._d(Compiler._Error(), para_Ident.lineNum);
                    } else {
                        //Error-e
                        for(int i=0;i<para_FuncRParams.arr_Exp.size();i++){
                            Symbol s_get = para_FuncRParams.arr_Exp.get(i).Rpara;//获得当前Rpara对应Exp中存储的符号
                            if(s_get == null) {
                                s_get = new Symbol("");
                                s_get.selfType = Value.SelfType.VAR;
                            }
                            Symbol s_inList = f.paraList.get(i);//获得当前对应函数的参数表里对应的参数符号
                            if(s_get.selfType == Value.SelfType.VAR || s_get.selfType == Value.SelfType.CONST || s_get.selfType == Value.SelfType.PARA){
                                if(s_inList.selfType != Value.SelfType.PARA){
                                    //var/const/para 匹配 para
                                    Errors._e(Compiler._Error(), para_Ident.lineNum);
                                }
                            }else{
                                if(s_inList.selfType != Value.SelfType.PARA_ARRAY){
                                    //array/para_array 匹配 para_array
                                    Errors._e(Compiler._Error(), para_Ident.lineNum);
                                }
                            }
                        }
                    }
                }else {//para_FuncRParams为空，即空Rpara直接调用函数
                    if (0 != f.paraList.size()) {
                        //Error-d
                        Errors._d(Compiler._Error(), para_Ident.lineNum);
                    }
                }
            }
            //Error-j
            if (toToken(0)!= Init.tokenType.RPARENT){
                para_RPARENT = new Token(")",Init.tokenType.RPARENT,arr_token.get(index-1).lineNum);
                Errors._j(Compiler._Error(),arr_token.get(index-1).lineNum);
            }else {
                para_RPARENT = arr_token.get(index);
                nextsym();
            }
            //如果是左值表达式里面的函数调用，该Exp设为Var类型的回归值
            Symbol re = new Symbol("");
            re.selfType = Value.SelfType.VAR;
            flag_Rpara = re;
        }else if (toToken(0) == Init.tokenType.PLUS || toToken(0) == Init.tokenType.MINU || toToken(0) == Init.tokenType.NOT) {
            // UnaryOp UnaryExp
            para_UnaryOp = A_UnaryOp();
            para_UnaryExp = A_UnaryExp();
        }else{
            // PrimaryExp
            para_PrimaryExp = A_PrimaryExp();
        }
        arr_Parser.add("<UnaryExp>");
        return new UnaryExp(para_PrimaryExp,para_Ident,para_LPARENT,para_FuncRParams,para_RPARENT,para_UnaryOp,para_UnaryExp,-1);
    }
    public UnaryOp A_UnaryOp(){
        //UnaryOp → '+' | '−' | '!'
        Token para_PMN = new Token();
        para_PMN = arr_token.get(index);nextsym();
        arr_Parser.add("<UnaryOp>");
        return new UnaryOp(para_PMN);
    }
    public FuncRParams A_FuncRParams(){
        //FuncRParams → Exp { ',' Exp }
        ArrayList<Exp> arr_Exp = new ArrayList<>();
        ArrayList<Token> arr_COMMA = new ArrayList<>();
        arr_Exp.add(A_Exp());
        while(toToken(0)== Init.tokenType.COMMA){
            arr_COMMA.add(arr_token.get(index));nextsym();
            arr_Exp.add(A_Exp());
        }
        arr_Parser.add("<FuncRParams>");
        return new FuncRParams(arr_Exp,arr_COMMA);
    }
    public MulExp A_MulExp(){
        //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        //改写：UnaryExp { ('*' | '/' | '%') UnaryExp }
        ArrayList<Token> arr_MDM=new ArrayList<>();
        ArrayList<UnaryExp> arr_UnaryExp=new ArrayList<>();
        arr_UnaryExp.add(A_UnaryExp());
        while(toToken(0) == Init.tokenType.MULT || toToken(0) == Init.tokenType.DIV || toToken(0) == Init.tokenType.MOD){
            arr_Parser.add("<MulExp>");
            if (toToken(0) == Init.tokenType.MULT) {
                arr_MDM.add(arr_token.get(index));
                nextsym();
                arr_UnaryExp.add(A_UnaryExp());
            } else if (toToken(0) == Init.tokenType.DIV) {
                arr_MDM.add(arr_token.get(index));
                nextsym();
                arr_UnaryExp.add(A_UnaryExp());
            } else if (toToken(0) == Init.tokenType.MOD) {
                arr_MDM.add(arr_token.get(index));
                nextsym();
                arr_UnaryExp.add(A_UnaryExp());
            }
        }
        arr_Parser.add("<MulExp>");
        return new MulExp(arr_MDM,arr_UnaryExp,-1);
    }
    public AddExp A_AddExp(){
        //AddExp → MulExp | AddExp ('+' | '−') MulExp
        //改写：MulExp {('+' | '−') MulExp}
        ArrayList<Token> arr_PLUSandMINU = new ArrayList<>();
        ArrayList<MulExp> arr_MulExp=new ArrayList<>();
        arr_MulExp.add(A_MulExp());
        while(toToken(0) == Init.tokenType.PLUS || toToken(0) == Init.tokenType.MINU){
            arr_Parser.add("<AddExp>");
            if (toToken(0) == Init.tokenType.PLUS) {
                arr_PLUSandMINU.add(arr_token.get(index));
                nextsym();
                arr_MulExp.add(A_MulExp());
            } else if (toToken(0) == Init.tokenType.MINU) {
                arr_PLUSandMINU.add(arr_token.get(index));
                nextsym();
                arr_MulExp.add(A_MulExp());
            }
        }
        arr_Parser.add("<AddExp>");
        return new AddExp(arr_PLUSandMINU,arr_MulExp,-1);
    }
    public RelExp A_RelExp(){
        //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        //改写：AddExp {('<' | '>' | '<=' | '>=') AddExp}
        ArrayList<Token> arr_OPT=new ArrayList<>();
        ArrayList<AddExp> arr_AddExp=new ArrayList<>();
        arr_AddExp.add(A_AddExp());
        while(toToken(0) == Init.tokenType.LSS||toToken(0) == Init.tokenType.GRE||toToken(0) == Init.tokenType.LEQ||toToken(0) == Init.tokenType.GEQ){
            arr_Parser.add("<RelExp>");
            if (toToken(0) == Init.tokenType.LSS) {
                arr_OPT.add(arr_token.get(index));
                nextsym();
                arr_AddExp.add(A_AddExp());
            } else if (toToken(0) == Init.tokenType.GRE) {
                arr_OPT.add(arr_token.get(index));
                nextsym();
                arr_AddExp.add(A_AddExp());
            } else if (toToken(0) == Init.tokenType.LEQ) {
                arr_OPT.add(arr_token.get(index));
                nextsym();
                arr_AddExp.add(A_AddExp());
            } else if (toToken(0) == Init.tokenType.GEQ) {
                arr_OPT.add(arr_token.get(index));
                nextsym();
                arr_AddExp.add(A_AddExp());
            }
        }
        arr_Parser.add("<RelExp>");
        return new RelExp(arr_OPT,arr_AddExp);
    }
    public EqExp A_EqExp(){
        //EqExp → RelExp | EqExp ('==' | '!=') RelExp
        //改写： RelExp { ('==' | '!=') RelExp}
        ArrayList<Token> arr_EQLorNEQ=new ArrayList<>();
        ArrayList<RelExp> arr_RelExp=new ArrayList<>();
        arr_RelExp.add(A_RelExp());
        while(toToken(0) == Init.tokenType.EQL || toToken(0) == Init.tokenType.NEQ){
            arr_Parser.add("<EqExp>");
            if (toToken(0) == Init.tokenType.EQL) {
                arr_EQLorNEQ.add(arr_token.get(index));
                nextsym();
                arr_RelExp.add(A_RelExp());
            } else if (toToken(0) == Init.tokenType.NEQ) {
                arr_EQLorNEQ.add(arr_token.get(index));
                nextsym();
                arr_RelExp.add(A_RelExp());
            }
        }
        arr_Parser.add("<EqExp>");
        return new EqExp(arr_EQLorNEQ,arr_RelExp);
    }
    public LAndExp A_LAndExp(){
        //LAndExp → EqExp | LAndExp '&&' EqExp
        //改写：EqExp { '&&' EqExp }
        ArrayList<Token> arr_AND=new ArrayList<>();
        ArrayList<EqExp> arr_EqExp=new ArrayList<>();
        arr_EqExp.add(A_EqExp());
        while (toToken(0)== Init.tokenType.AND){
            arr_Parser.add("<LAndExp>");
            arr_AND.add(arr_token.get(index));
            nextsym();
            arr_EqExp.add(A_EqExp());
        }
        arr_Parser.add("<LAndExp>");
        return new LAndExp(arr_AND,arr_EqExp);
    }
    public LOrExp A_LOrExp(){
        //LOrExp → LAndExp | LOrExp '||' LAndExp
        //改写：LAndExp { '||' LAndExp }
        ArrayList<Token> arr_OR=new ArrayList<>();
        ArrayList<LAndExp> arr_LAndExp=new ArrayList<>();
        arr_LAndExp.add(A_LAndExp());
        while (toToken(0)== Init.tokenType.OR){
            arr_Parser.add("<LOrExp>");
            arr_OR.add(arr_token.get(index));nextsym();
            arr_LAndExp.add(A_LAndExp());
        }
        arr_Parser.add("<LOrExp>");
        return new LOrExp(arr_OR,arr_LAndExp);
    }
    public ConstExp A_ConstExp(){
        //ConstExp → AddExp
        AddExp para_AddExp;
        para_AddExp = A_AddExp();
        arr_Parser.add("<ConstExp>");
        return new ConstExp(para_AddExp);
    }
}
