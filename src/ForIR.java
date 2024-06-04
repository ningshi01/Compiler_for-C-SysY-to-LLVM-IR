import CompUnit.*;
import LLVM.Module;
import Symbol.Symbol;
import Symbol.*;
import Token.Init;
import LLVM.*;
import Token.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForIR {
    public SymbolManager sManager = SymbolManager.getInstance();
    public Module module = Module.getInstance();

    //help_var_list_start
    private int Now_RID=0;//函数体中当前使用到寄存器ID编号/基本块编号
    private boolean flag_global_decl=true;//全局变量声明中状态标识符
    private Function cur_func = new Function();//当前函数
    private BasicBlock cur_Bblock = new BasicBlock();//当前基本块
    private boolean flag_array_get_Level = false;//数组取的非值而是某维地址
    private boolean flag_gth = false;//使用了！X（感叹号）
    private int ToVar_level = 0;//参数存储时的层数（当前）
    private int ToVar_level_id = 0;//相同层数时分配id
    //help_var_list_end

    public void help(String result){
//        Instruction help = new Instruction();{
//            help.instrType = Instruction.InstrType.HELP;
//            help.result = result+"------------------------";
//        }cur_Bblock.InstrList.add(help);
    }

    public void analyse(CompUnit root){
        visit(root);
    }
    public void visit(CompUnit compUnit){
        //CompUnit -> {Decl} {FuncDef} MainFuncDef
        for (Decl decl:compUnit.arr_Decl){
            visit(decl);
        }
        flag_global_decl = false;
        for (FuncDef funcDef:compUnit.arr_FuncDef){
            visit(funcDef);
            Now_RID = 0;
        }
        visit(compUnit.para_MainFuncDef);
    }
    public void visit(Decl decl){
        //Decl -> ConstDecl | VarDecl
        if(decl.para_ConstDecl.para_const.TData!=null){
            visit(decl.para_ConstDecl);
        }else{
            visit(decl.para_VarDecl);
        }
    }
    public void visit(ConstDecl constDecl){
        //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        visit(constDecl.para_BType);
        for(ConstDef constDef:constDecl.arr_ConstDef){
            visit(constDef);
        }
    }
    public void visit(BType bType){
        //BType → 'int'

    }
    public void visit(ConstDef constDef){
        //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        int def_Rid=Now_RID;//预留好的寄存器id
        int def_Instr_index=-1;//用来回来寻找alloca进行回填的InstrList的index
        if(!flag_global_decl){//如果是局部变量先分配空间
            Instruction i = new Instruction();
            {
                i.parentBB = cur_Bblock;
                i.result = "%" + (Now_RID++);
                i.instrType = Instruction.InstrType.ALLOCA;
                i.operation1 = "i32";//先统一认定为var而非Array
            }
            cur_Bblock.InstrList.add(i);
            def_Instr_index = cur_Bblock.InstrList.size()-1;
        }
        if(constDef.arr_ConstExp.size()!=0){
            for (ConstExp constExp:constDef.arr_ConstExp){
                visit(constExp);
            }
        }
        visit(constDef.para_ConstInitVal);

        if (constDef.arr_LBRACK.size() == 0) {
            //const定义单个常量
            Value v = new Value();{
                v.selfType = Value.SelfType.CONST;
                if(!flag_global_decl)
                    v.registerID = def_Rid;
                v.returnValue = constDef.para_ConstInitVal.getInt();
                v.returnType = Value.ReturnType.INT;
            }
            sManager.curnow().addToLists(new Symbol(constDef.para_Ident.TData,v));
        }else{
            //const定义数组
            Value v = new Value();{
                v.selfType = Value.SelfType.ARRAY;
                v.Array_Const = true;
                if(!flag_global_decl)
                    v.registerID = def_Rid;
                constDef.arr_ConstExp.forEach(e->{
                    v.selfLevels.add(e.getInt());
                });
                v.returnType = Value.ReturnType.INT;
                constDef.para_ConstInitVal.getNums(v.returnNums);
            }
            sManager.curnow().addToLists(new Symbol(constDef.para_Ident.TData,v));
        }
        if(flag_global_decl)
            module.GlobalDeclList.add(sManager.curnow().lists.get(sManager.curnow().lists.size()-1));
        else if(constDef.arr_LBRACK.size()==0){//是变量非数组
            //执行store指令
            Instruction i =new Instruction();{
                i.parentBB = cur_Bblock;
                i.instrType = Instruction.InstrType.STORE;
                if(constDef.para_ConstInitVal.para_ConstExp.isIntCon())
                    i.operation1 = ""+constDef.para_ConstInitVal.para_ConstExp.getInt();
                else
                    i.operation1 = "%"+constDef.para_ConstInitVal.para_ConstExp.RegisterID;
                i.operation2 = "%"+sManager.curnow().getToRoot(constDef.para_Ident.TData).registerID;
            }cur_Bblock.InstrList.add(i);
        }else{//是数组
            //先回填alloca指令
            Symbol s = sManager.curnow().getToRoot(constDef.para_Ident.TData);
            Instruction i_alloca = cur_Bblock.InstrList.get(def_Instr_index);//拿到标记好的alloca指令
            i_alloca.operation1 = s.get_array_type_all();
            //再逐个getelementptr赋值过去
            int nums_index = -1;
            int nums_x = 0;
            int nums_y = 0;
            for(int num:s.returnNums){
                nums_index++;
                //执行getelementptr指令
                Instruction i1 = new Instruction();{
                    i1.parentBB=cur_Bblock;
                    i1.instrType = Instruction.InstrType.GETELEMENTPTR;
                    i1.result = "%" + (Now_RID++);
                    i1.operation1 = s.get_array_type_all();
                    i1.operation2 = i_alloca.result;
                    if(s.selfLevels.size()==1){//一维数组
                        nums_x = nums_index;
                        i1.operation3 = ""+nums_x;
                        i1.operation4 = null;
                    }else{
                        nums_x = nums_index/s.selfLevels.get(1);
                        nums_y = nums_index%s.selfLevels.get(1);
                        i1.operation3 = "" + nums_x;
                        i1.operation4 = "" + nums_y;
                    }
                }cur_Bblock.InstrList.add(i1);
                //执行store指令
                Instruction i2 = new Instruction();{
                    i2.parentBB = cur_Bblock;
                    i2.instrType = Instruction.InstrType.STORE;
                    i2.operation1 = "" + num;
                    i2.operation2 = "%" + (Now_RID-1);
                }cur_Bblock.InstrList.add(i2);
            }
        }
    }
    public void visit(ConstInitVal constInitVal){
        //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if(constInitVal.arr_ConstInitVal.size()==0){
            visit(constInitVal.para_ConstExp);
        }else {
            for(ConstInitVal constInitVal1: constInitVal.arr_ConstInitVal){
                visit(constInitVal1);
            }
        }
    }
    public void visit(VarDecl varDecl){
        //VarDecl → BType VarDef { ',' VarDef } ';'
        visit(varDecl.para_BType);
        for(VarDef varDef:varDecl.arr_VarDef){
            visit(varDef);
        }
    }
    public void visit(VarDef varDef){
        //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        int def_Rid=Now_RID;
        int def_Instr_index=-1;//用来回来寻找alloca进行回填的InstrList的index
        if(!flag_global_decl){//如果是局部变量先分配空间
            Instruction i = new Instruction();
            {
                i.parentBB = cur_Bblock;
                i.result = "%" + (Now_RID++);
                i.instrType = Instruction.InstrType.ALLOCA;
                i.operation1 = "i32";
            }
            cur_Bblock.InstrList.add(i);
            def_Instr_index = cur_Bblock.InstrList.size()-1;
        }
        for(ConstExp constExp: varDef.arr_ConstExp){
            visit(constExp);
        }
        if(varDef.para_ASSIGN.TData!=null){
            visit(varDef.para_InitVal);
        }

        if (varDef.arr_LBRACK.size() == 0) {
            //int定义单个变量
            Value v = new Value();{
                v.selfType = Value.SelfType.VAR;
                if(!flag_global_decl)
                    v.registerID = def_Rid;
//                if(flag_global_decl)
                    v.returnValue = varDef.para_InitVal.getInt();
//                else
//                    v.returnValue = -1;
                v.returnType = Value.ReturnType.INT;
            }
            sManager.curnow().addToLists(new Symbol(varDef.para_Ident.TData,v));
        } else {
            //int定义数组
            Value v = new Value();{
                v.selfType = Value.SelfType.ARRAY;
                if(!flag_global_decl)
                    v.registerID = def_Rid;
                varDef.arr_ConstExp.forEach(e->{
                    v.selfLevels.add(e.getInt());
                });
                v.returnType = Value.ReturnType.INT;
                if(varDef.para_InitVal.para_Exp!=null){//如果有初值
                    varDef.para_InitVal.getNums(v.returnNums);
                }
            }
            sManager.curnow().addToLists(new Symbol(varDef.para_Ident.TData,v));
        }
        if(flag_global_decl)
            module.GlobalDeclList.add(sManager.curnow().lists.get(sManager.curnow().lists.size()-1));
        else if(varDef.para_InitVal.para_Exp==null && varDef.arr_LBRACK.size()==0){}//筛选掉（非数组）无InitVal的情况-不输出指令
        else if(varDef.arr_LBRACK.size()==0){//是变量非数组
            Instruction i =new Instruction();{
                i.parentBB = cur_Bblock;
                i.instrType = Instruction.InstrType.STORE;
                if(varDef.para_InitVal.para_Exp.isIntCon())
                    i.operation1 = ""+varDef.para_InitVal.para_Exp.getInt();
                else
                    i.operation1 = "%"+varDef.para_InitVal.para_Exp.RegisterID;
                i.operation2 = "%"+sManager.curnow().getToRoot(varDef.para_Ident.TData).registerID;
            }cur_Bblock.InstrList.add(i);
        }else{//是数组
            //先回填alloca指令
            Symbol s = sManager.curnow().getToRoot(varDef.para_Ident.TData);
            Instruction i_alloca = cur_Bblock.InstrList.get(def_Instr_index);//拿到标记好的alloca指令
            i_alloca.operation1 = s.get_array_type_all();
            //再逐个getelementptr赋值过去
            int nums_index = -1;
            int nums_x = 0;
            int nums_y = 0;
            for(int num:s.returnNums){
                nums_index++;
                //执行getelementptr指令
                Instruction i1 = new Instruction();{
                    i1.parentBB=cur_Bblock;
                    i1.instrType = Instruction.InstrType.GETELEMENTPTR;
                    i1.result = "%" + (Now_RID++);
                    i1.operation1 = s.get_array_type_all();
                    i1.operation2 = i_alloca.result;
                    if(s.selfLevels.size()==1){//一维数组
                        nums_x = nums_index;
                        i1.operation3 = ""+nums_x;
                        i1.operation4 = null;
                    }else{//二维数组
                        nums_x = nums_index/s.selfLevels.get(1);
                        nums_y = nums_index%s.selfLevels.get(1);
                        i1.operation3 = "" + nums_x;
                        i1.operation4 = "" + nums_y;
                    }
                }cur_Bblock.InstrList.add(i1);
                //执行store指令
                Instruction i2 = new Instruction();{
                    i2.parentBB = cur_Bblock;
                    i2.instrType = Instruction.InstrType.STORE;
                    i2.operation1 = "" + num;
                    i2.operation2 = "%" + (Now_RID-1);
                }cur_Bblock.InstrList.add(i2);
            }
        }
    }
    public void visit(InitVal initVal){
        //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if(initVal.arr_InitVal.size()==0){
            visit(initVal.para_Exp);
        }else{
            for(InitVal initVal1:initVal.arr_InitVal){
                visit(initVal1);
            }
        }
    }
    public void visit(FuncDef funcDef){
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        visit(funcDef.para_FuncType);
        Function f = new Function();{
            f.name = funcDef.para_Ident.TData;
            if(funcDef.para_FuncType.para_type.type == Init.tokenType.INTTK) {
                f.retType = Value.ReturnType.INT;
            }else if(funcDef.para_FuncType.para_type.type == Init.tokenType.VOIDTK){
                f.retType = Value.ReturnType.VOID;
            }
        }cur_func = f;
        //定义一个函数符号
        Value v = new Value();{
            v.selfType = Value.SelfType.FUNC;
            if(funcDef.para_FuncType.para_type.type == Init.tokenType.INTTK) {
                v.returnType = Value.ReturnType.INT;
            }else if(funcDef.para_FuncType.para_type.type == Init.tokenType.VOIDTK){
                v.returnType = Value.ReturnType.VOID;
            }
        }
        sManager.curnow().addToLists(new Symbol(funcDef.para_Ident.TData,v));
        sManager.curnow().addToBacks(new SymbolTable(),true);
        //把参数压入符号表
        sManager.MoveDown();
        if(funcDef.para_FuncFParams.arr_FuncFParam!=null){
            visit(funcDef.para_FuncFParams);
        }
        //在退出子表前把参数压入paraList
        for(Symbol paraSymbol:sManager.curnow().lists){
            f.paraList.add(paraSymbol);
        }
        sManager.MoveUp();
        cur_func = f;
        //定义一个基本块
        BasicBlock bb = new BasicBlock();{
            bb.fromFunc = cur_func;
            bb.BBid = Now_RID++;
        }
        cur_Bblock = bb;
        module.FuncList.add(cur_func);//先加进去占位防止函数内调用本体

        //提前visit参数一下
        for(Symbol para:f.paraList){
            LVal lVal = new LVal();{
                lVal.para_Ident = new Token();
                lVal.para_Ident.TData = para.Name;
                sManager.MoveDown();
                visit(lVal);
                sManager.MoveUp();
            }
        }

        visit(funcDef.para_Block);
        module.FuncList.remove(cur_func);//再移除占位的函数
        cur_func.BblockList.add(cur_Bblock);
        module.FuncList.add(cur_func);
    }
    public void visit(MainFuncDef mainFuncDef){
        //MainFuncDef → 'int' 'main' '(' ')' Block
        Function f = new Function();{
            f.name = "main";
            f.retType = Value.ReturnType.INT;
        }
        //定义一个main函数符号
        Value v = new Value();{
            v.selfType = Value.SelfType.FUNC;
            v.returnType = Value.ReturnType.INT;
        }
        sManager.curnow().addToLists(new Symbol("main",v));
        sManager.curnow().addToBacks(new SymbolTable(),true);
        cur_func = f;
        //定义一个基本块
        BasicBlock bb = new BasicBlock();{
            bb.fromFunc = cur_func;
            bb.BBid = Now_RID++;
        }
        cur_Bblock = bb;
        visit(mainFuncDef.para_Block);
        cur_func.BblockList.add(cur_Bblock);
        module.FuncList.add(cur_func);
    }
    public void visit(FuncType funcType){
        //FuncType → 'void' | 'int'

    }
    public void visit(FuncFParams funcFParams){
        //FuncFParams → FuncFParam { ',' FuncFParam }
        for(FuncFParam funcFParam:funcFParams.arr_FuncFParam){
            visit(funcFParam);
        }
    }
    public void visit(FuncFParam funcFParam){
        //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        visit(funcFParam.para_BType);
        for (ConstExp constExp:funcFParam.arr_ConstExp){
//            visit(constExp);
        }
        if(funcFParam.arr_LBRACK.size()==0){
            //para定义单个常量
            Value v = new Value();
            {
                v.selfType = Value.SelfType.PARA;
                if (!flag_global_decl)
                    v.registerID = Now_RID++;
                v.returnType = Value.ReturnType.INT;
            }
            sManager.curnow().addToLists(new Symbol(funcFParam.para_Ident.TData,v));
        }else{
            //para定义数组
            Value v = new Value();
            {
                v.selfType = Value.SelfType.PARA_ARRAY;
                if (!flag_global_decl)
                    v.registerID = Now_RID++;
                v.returnType = Value.ReturnType.INT;
                v.selfLevels.add(0);//填充参数-数组的第一维
                funcFParam.arr_ConstExp.forEach(e->{
                    v.selfLevels.add(e.getInt());
                });
            }
            sManager.curnow().addToLists(new Symbol(funcFParam.para_Ident.TData,v));
        }
    }
    public void visit(Block block){
        //Block → '{' { BlockItem } '}'
        //先搜索上一个表是否处于waiting
        if(sManager.curnow().IsWaiting()){
            sManager.curnow().waitTowork();
        }else{
            sManager.curnow().addToBacks(new SymbolTable());
        }
        sManager.MoveDown();
        for(BlockItem blockItem:block.arr_BlockItem){
            visit(blockItem);
        }
        sManager.MoveUp();
    }
    public void visit(BlockItem blockItem){
        //BlockItem → Decl | Stmt
        if(blockItem.para_Decl!=null){
            visit(blockItem.para_Decl);
        }else {
            visit(blockItem.para_Stmt);
        }
    }
    public void visit(Stmt stmt){
        // Stmt →
        switch (stmt.type){
            case 1:
                //1 LVal '=' Exp ';' // 每种类型的语句都要覆盖
                visit(stmt.para_LVal);

                //取消LVal的最后一条LOAD指令&& Now_Rid--
                if(cur_Bblock.InstrList.get(cur_Bblock.InstrList.size()-1).instrType== Instruction.InstrType.LOAD){
                    cur_Bblock.InstrList.remove(cur_Bblock.InstrList.size()-1);
                    Now_RID--;
                }
                Instruction i1_para_LVal = new Instruction();
                if(cur_Bblock.InstrList.size()>0){//取一下最后一条指令
                    i1_para_LVal = cur_Bblock.InstrList.get(cur_Bblock.InstrList.size() - 1);
                }

                visit(stmt.para_Exp);
                //执行store指令
                Instruction inst =new Instruction();{
                    inst.parentBB = cur_Bblock;
                    inst.instrType = Instruction.InstrType.STORE;
                    if(stmt.para_Exp.isIntCon())
                        inst.operation1 = ""+stmt.para_Exp.getInt();
                    else
                        inst.operation1 = "%"+stmt.para_Exp.RegisterID;
                    Symbol s = sManager.curnow().getToRoot(stmt.para_LVal.para_Ident.TData);
                    if(s.registerID==-1 && s.selfLevels.size()==0) {//全局变量-变量
                        inst.operation2 = "@" + stmt.para_LVal.para_Ident.TData;
                    }else if(s.registerID==-1 && s.selfLevels.size()!=0){//全局变量-数组
                        inst.operation2 = stmt.para_LVal.lval_before_equal_registerID;
                        Instruction i1_para_Exp = cur_Bblock.InstrList.get(cur_Bblock.InstrList.size()-1);
                        if(i1_para_Exp.result.equals(i1_para_LVal.result)){//表明Exp未输出指令，应该是常数
                            inst.operation3 = "i32";
                        }else if(i1_para_Exp.instrType == Instruction.InstrType.LOAD){
                            inst.operation3 = i1_para_Exp.operation3;
                        }else if(i1_para_Exp.instrType == Instruction.InstrType.GETELEMENTPTR){
                            //先匹配看看是不是二维类型
                            String pattern_2 = "\\[(\\d+ x \\[\\d+ x i32\\])\\]";
                            Pattern r2 = Pattern.compile(pattern_2);
                            Matcher m2 = r2.matcher(i1_para_Exp.operation1);
                            //再匹配一维类型
                            String pattern_1 = "\\[\\d+ x i32\\]";
                            Pattern r1 = Pattern.compile(pattern_1);
                            Matcher m1 = r1.matcher(i1_para_Exp.operation1);
                            if(m2.find() && m1.find())
                                inst.operation3 = m1.group(0)+"*";
                            if(!m2.find() && m1.find())
                                inst.operation3 = "i32*";
                        }
                        inst.operation4 = inst.operation3+"*";
                    }else if(s.registerID!=-1 && s.selfLevels.size()==0){//局部变量-变量
                        inst.operation2 = "%" + sManager.curnow().getToRoot(stmt.para_LVal.para_Ident.TData).registerID;
                        if(s.selfType == Value.SelfType.PARA){
                            if(s.flag_ToVar){
                                inst.operation2 = "%"+s.In_ToVar_registerID;
                            }
                        }
                    }else if(s.registerID!=-1 && s.selfLevels.size()!=0){//局部变量-数组
                        inst.operation2 = stmt.para_LVal.lval_before_equal_registerID;
                        Instruction i1_para_Exp = cur_Bblock.InstrList.get(cur_Bblock.InstrList.size()-1);
                        if(i1_para_Exp.result.equals(i1_para_LVal.result)){//表明Exp未输出指令，应该是常数
                            inst.operation3 = "i32";
                        }else if(i1_para_Exp.instrType == Instruction.InstrType.LOAD){
                            inst.operation3 = i1_para_Exp.operation3;
                        }else if(i1_para_Exp.instrType == Instruction.InstrType.GETELEMENTPTR){
                            //先匹配看看是不是二维类型
                            String pattern_2 = "\\[(\\d+ x \\[\\d+ x i32\\])\\]";
                            Pattern r2 = Pattern.compile(pattern_2);
                            Matcher m2 = r2.matcher(i1_para_Exp.operation1);
                            //再匹配一维类型
                            String pattern_1 = "\\[\\d+ x i32\\]";
                            Pattern r1 = Pattern.compile(pattern_1);
                            Matcher m1 = r1.matcher(i1_para_Exp.operation1);
                            if(m2.find() && m1.find())
                                inst.operation3 = m1.group(0)+"*";
                            if(!m2.find() && m1.find())
                                inst.operation3 = "i32*";
                        }
                        inst.operation4 = inst.operation3+"*";
                    }
                }cur_Bblock.InstrList.add(inst);
                break;
            case 2:
                //2 [Exp] ';' //有无Exp两种情况
                if(stmt.para_Exp.para_AddExp!=null)
                    visit(stmt.para_Exp);
                break;
            case 3:
                //3 Block
                visit(stmt.para_Block);
                break;
            case 4:
                //4 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
                //start-预留基本块id
                int BblockID_Stmt1 = -1;
                int BblockID_Stmt2 = -1;
                int BblockID_BasicBlock3 = -1;
                //end  -预留基本块id

                Instruction i0 = new Instruction();{
                    i0.parentBB = cur_Bblock;
                    i0.instrType = Instruction.InstrType.BR;
                    i0.result = "%"+Now_RID;
                }cur_Bblock.InstrList.add(i0);
                //定义if入口
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                BasicBlock b1_if = new BasicBlock();{
                    b1_if.fromFunc = cur_func;
                    b1_if.BBid = Now_RID++;
                    BblockID_Stmt1 = b1_if.BBid;
                }
                cur_Bblock = b1_if;

                help("entry----if");
                //level++
                ToVar_level++;

                visit(stmt.para_Cond);
                BblockID_Stmt1 = cur_Bblock.BBid;

                visit(stmt.para_Stmt1);

                if(stmt.para_ELSETK.TData!=null){//如果有Else
                    //定义基本块Stmt2
                    cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                    BasicBlock b2_if = new BasicBlock();
                    {
                        b2_if.fromFunc = cur_func;
                        b2_if.BBid = Now_RID++;
                        BblockID_Stmt2 = Now_RID-1;
                    }
                    cur_Bblock = b2_if;

                    help("entry--else");

                    visit(stmt.para_Stmt2);

                    help("exit--else");
                }

                help("exit----if");

                //定义基本块BasicBlock3
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                BasicBlock b3_if = new BasicBlock();{
                    b3_if.fromFunc = cur_func;
                    b3_if.BBid = Now_RID++;
                    BblockID_BasicBlock3 = Now_RID-1;
                }
                cur_Bblock = b3_if;

                //Stmt1跳转到BasicBlock3的br回填(借助下一个）
                BasicBlock lastStmt1_Block_if=new BasicBlock();
                if(stmt.para_ELSETK.TData==null)
                    lastStmt1_Block_if = cur_func.BblockList.get(cur_func.BblockList.size()-1);//最新一个即是
                else
                    lastStmt1_Block_if = cur_func.findBeforeBblock(BblockID_Stmt2);
                if(!cur_func.IsBrLast(lastStmt1_Block_if.BBid)) {//先判断最后一条不是BR指令
                    Instruction i2_if = new Instruction();
                    {
                        i2_if.parentBB = cur_Bblock;
                        i2_if.instrType = Instruction.InstrType.BR;
                        i2_if.result = "%" + BblockID_BasicBlock3;
                    }
                    lastStmt1_Block_if.InstrList.add(i2_if);
                }


                if(stmt.para_ELSETK.TData!=null) {//如果有Else
                    //Stmt2跳转到BasicBlock3的br回填(借助下一个)
                    BasicBlock lastStmt2_Block_if = cur_func.BblockList.get(cur_func.BblockList.size()-1);//最新一个即是
                    if(!cur_func.IsBrLast(lastStmt2_Block_if.BBid)) {//先判断最后一条不是BR指令
                        Instruction i3_if = new Instruction();
                        {
                            i3_if.parentBB = cur_Bblock;
                            i3_if.instrType = Instruction.InstrType.BR;
                            i3_if.result = "%" + BblockID_BasicBlock3;
                        }
                        lastStmt2_Block_if.InstrList.add(i3_if);
                    }
                }

                //执行回填Cond-LorExp的操作
                for(int j=0;j<stmt.para_Cond.para_LOrExp.needs.size();j++){
                    BasicBlock need = stmt.para_Cond.para_LOrExp.needs.get(j);
                    Instruction i_need = need.InstrList.get(need.InstrList.size()-1);
                    i_need.operation1 = "%"+BblockID_Stmt1;//如果LorExp正确跳转到下一条LorExp
                    //如果LorExp错误
                    if(j==stmt.para_Cond.para_LOrExp.needs.size()-1){//最后一条LorExp处理
                        if(stmt.para_ELSETK.TData==null) {//如果没有else语句直接跳到BasicBlock
                            i_need.operation2 = "%" + BblockID_BasicBlock3;
                        }else{//如果有else语句跳转进Stmt2
                            i_need.operation2 = "%" + BblockID_Stmt2;
                        }
                    }else{//不是最后一条LorExp跳转进下一个LorExp
                        i_need.operation2 = "%"+cur_func.findNextBblock(need.BBid).BBid;
                    }
                }
                {
                    LOrExp lOrExp = stmt.para_Cond.para_LOrExp;
                    for (int j = 0; j < lOrExp.arr_LAndExp.size(); j++) {
                        //执行回填Cond-LandExp的操作
                        for (int z = 0; z < lOrExp.arr_LAndExp.get(j).needs.size(); z++) {
                            BasicBlock need = lOrExp.arr_LAndExp.get(j).needs.get(z);
                            Instruction i_need = need.InstrList.get(need.InstrList.size() - 1);
                            i_need.operation1 = "%" + cur_func.findNextBblock(need.BBid).BBid;//LandExp正确->下一条LandExp
                            BasicBlock last_need_next = cur_func.findNextBblock(lOrExp.arr_LAndExp.get(j).needs.get(lOrExp.arr_LAndExp.get(j).needs.size() - 1).BBid);
                            i_need.operation2 = last_need_next.InstrList.get(last_need_next.InstrList.size() - 1).operation2;//LandExp错误->(最后一条LorExp->错误)
                        }
                    }
                }

                break;
            case 5:
                //5 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
                int BlockID_forStmt1 = -1;
                int BlockID_cond = -1;
                int BlockID_forStmt2 = -1;
                int BlockID_stmt = -1;
                int BlockID_BasicBlock = -1;
                help("entry----for");

                Instruction i0_for = new Instruction();{
                    i0_for.parentBB = cur_Bblock;
                    i0_for.instrType = Instruction.InstrType.BR;
                    i0_for.result = "%"+Now_RID;
                }cur_Bblock.InstrList.add(i0_for);
                //定义基本块forStmt1
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                BasicBlock b1_for = new BasicBlock();
                {
                    b1_for.fromFunc = cur_func;
                    b1_for.BBid = Now_RID++;
                    BlockID_forStmt1 = b1_for.BBid;
                }
                cur_Bblock = b1_for;

                help("this is forStmt1");

                if(stmt.para_ForStmt1.para_LVal!=null)
                    visit(stmt.para_ForStmt1);


                //定义基本块cond
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                BasicBlock b2_for = new BasicBlock();
                {
                    b2_for.fromFunc = cur_func;
                    b2_for.BBid = Now_RID++;
                    BlockID_cond = b2_for.BBid;
                }
                cur_Bblock = b2_for;

                help("this is Cond");

                if(stmt.para_Cond.para_LOrExp!=null)
                    visit(stmt.para_Cond);
                else{
                    //定义基本块forStmt2
                    cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                    BasicBlock b3_for = new BasicBlock();
                    {
                        b3_for.fromFunc = cur_func;
                        b3_for.BBid = Now_RID++;
                    }
                    cur_Bblock = b3_for;
                }

                help("this is forStmt2");
                BlockID_forStmt2 = cur_Bblock.BBid;

                if(stmt.para_ForStmt2.para_LVal!=null)
                    visit(stmt.para_ForStmt2);


                //定义基本块Stmt
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                BasicBlock b4_for = new BasicBlock();{
                    b4_for.fromFunc = cur_func;
                    b4_for.BBid = Now_RID++;
                    BlockID_stmt = b4_for.BBid;
                }
                cur_Bblock = b4_for;

                help("this is Stmt");

                visit(stmt.para_Stmt);

                help("exit----for");

                //定义基本块BasicBlock
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                BasicBlock b5_for = new BasicBlock();{
                    b5_for.fromFunc = cur_func;
                    b5_for.BBid = Now_RID++;
                    BlockID_BasicBlock = b5_for.BBid;
                }
                cur_Bblock = b5_for;

                //forStmt1跳转到Cond的br回填
                Instruction i1_for = new Instruction();{
                    i1_for.parentBB = cur_Bblock;
                    i1_for.instrType = Instruction.InstrType.BR;
                    i1_for.result = "%" + BlockID_cond;
                }
                cur_func.findAndToBr(BlockID_forStmt1,i1_for);

                //Cond跳转到1-Stmt/0-BasicBlock的br回填(借助下一个）
                if(stmt.para_Cond.para_LOrExp!=null){//Cond存在
                    BasicBlock lastcond_Block = cur_func.findBeforeBblock(BlockID_forStmt2);
                    Instruction i2_for = lastcond_Block.InstrList.get(lastcond_Block.InstrList.size() - 1);
                    {
                        i2_for.operation1 = "%" + BlockID_stmt;
                        i2_for.operation2 = "%" + BlockID_BasicBlock;
                    }
                }else{//Cond为空
                    Instruction i2_for = new Instruction();{
                        i2_for.parentBB = cur_Bblock;
                        i2_for.instrType = Instruction.InstrType.BR;
                        i2_for.result = "%" + BlockID_stmt;
                    }
                    cur_func.findAndToBr(BlockID_cond,i2_for);
                }

                //Stmt跳转到forStmt2的br回填(借助下一个）
                BasicBlock lastStmt_Block_for = cur_func.BblockList.get(cur_func.BblockList.size()-1);//最新一个即是
                if(!cur_func.IsBrLast(lastStmt_Block_for.BBid)){
                    Instruction i3_for = new Instruction();
                    {
                        i3_for.parentBB = cur_Bblock;
                        i3_for.instrType = Instruction.InstrType.BR;
                        i3_for.result = "%" + BlockID_forStmt2;
                    }
                    lastStmt_Block_for.InstrList.add(i3_for);
                }

                //forStmt2跳转到Cond的br回填
                Instruction i4_for = new Instruction();{
                    i4_for.parentBB = cur_Bblock;
                    i4_for.instrType = Instruction.InstrType.BR;
                    i4_for.result = "%"+BlockID_cond;
                }
                cur_func.findAndToBr(BlockID_forStmt2,i4_for);

                //break/continue的br回填
                for(BasicBlock b: cur_func.BblockList){
                    if(b.BBid >= BlockID_stmt && b.BBid<BlockID_BasicBlock){
                        //遍历Stmt的基本块
                        if(b.flag_break){//如果是break语句结尾
                            Instruction i_for_break = b.InstrList.get(b.InstrList.size()-1);
                            i_for_break.result = "%"+BlockID_BasicBlock;
                            b.flag_break =false;
                        }
                        if(b.flag_continue){//如果是continue语句结尾
                            Instruction i_for_continue = b.InstrList.get(b.InstrList.size()-1);
                            i_for_continue.result = "%"+BlockID_forStmt2;
                            b.flag_continue =false;
                        }
                    }
                }

                if(stmt.para_Cond.para_LOrExp!=null)
                {//执行回填Cond-LorExp的操作
                    for (int j = 0; j < stmt.para_Cond.para_LOrExp.needs.size(); j++) {
                        BasicBlock need = stmt.para_Cond.para_LOrExp.needs.get(j);
                        Instruction i_need = need.InstrList.get(need.InstrList.size() - 1);
                        i_need.operation1 = "%" + BlockID_stmt;
                        if (j == stmt.para_Cond.para_LOrExp.needs.size() - 1) {
                            i_need.operation2 = "%" + BlockID_BasicBlock;
                        } else {
                            i_need.operation2 = "%" + cur_func.findNextBblock(need.BBid).BBid;
                        }
                    }
                    {
                        LOrExp lOrExp = stmt.para_Cond.para_LOrExp;
                        for (int j = 0; j < lOrExp.arr_LAndExp.size(); j++) {
                            //执行回填Cond-LandExp的操作
                            for (int z = 0; z < lOrExp.arr_LAndExp.get(j).needs.size(); z++) {
                                BasicBlock need = lOrExp.arr_LAndExp.get(j).needs.get(z);
                                Instruction i_need = need.InstrList.get(need.InstrList.size() - 1);
                                i_need.operation1 = "%" + cur_func.findNextBblock(need.BBid).BBid;
                                BasicBlock last_need_next = cur_func.findNextBblock(lOrExp.arr_LAndExp.get(j).needs.get(lOrExp.arr_LAndExp.get(j).needs.size() - 1).BBid);
                                i_need.operation2 = last_need_next.InstrList.get(last_need_next.InstrList.size() - 1).operation2;
                            }
                        }
                    }
                }
                break;
            case 6:
                //6 'break' ';'
                if(!cur_Bblock.flag_break && !cur_Bblock.flag_continue){//该基本块还没写入过break||continue
                    Instruction i1_break = new Instruction();
                    {
                        i1_break.parentBB = cur_Bblock;
                        i1_break.instrType = Instruction.InstrType.BR;
                        i1_break.result = "%" + "flag_break";
                    }
                    help("flag-break!!!");
                    cur_Bblock.InstrList.add(i1_break);
                    cur_Bblock.flag_break = true;
                    //定义基本块
                    cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                    BasicBlock b_break = new BasicBlock();{
                        b_break.fromFunc = cur_func;
                        b_break.BBid = Now_RID++;
                        BlockID_BasicBlock = b_break.BBid;
                    }
                    cur_Bblock = b_break;
                }
                break;
            case 7:
                //7 'continue' ';'
                if(!cur_Bblock.flag_break && !cur_Bblock.flag_continue){//该基本块还没写入过break||continue
                    Instruction i1_continue = new Instruction();
                    {
                        i1_continue.parentBB = cur_Bblock;
                        i1_continue.instrType = Instruction.InstrType.BR;
                        i1_continue.result = "%" + "flag_continue";
                    }
                    help("flag-continue!!!");
                    cur_Bblock.InstrList.add(i1_continue);
                    cur_Bblock.flag_continue = true;
                    //定义基本块
                    cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                    BasicBlock b_continue = new BasicBlock();{
                        b_continue.fromFunc = cur_func;
                        b_continue.BBid = Now_RID++;
                        BlockID_BasicBlock = b_continue.BBid;
                    }
                    cur_Bblock = b_continue;
                }
                break;
            case 8:
                //8 'return' [Exp] ';' // 1.有Exp 2.无Exp
                if(stmt.para_Exp.para_AddExp==null){//无Exp
                    Instruction in = new Instruction();{
                        in.parentBB = cur_Bblock;
                        in.instrType = Instruction.InstrType.RET;
                        in.result_Rid = -1;
                    }
                    cur_Bblock.InstrList.add(in);
                }else{//有Exp
                    visit(stmt.para_Exp);
                    Instruction ins = new Instruction();{
                        ins.parentBB = cur_Bblock;
                        ins.instrType = Instruction.InstrType.RET;
                        if(stmt.para_Exp.isIntCon()){
                            ins.result_Rid = 0;
                            ins.result = ""+stmt.para_Exp.getInt();
                        }else{
                            ins.result_Rid = 0;
                            ins.result = "%"+stmt.para_Exp.RegisterID;
                        }
                    }
                    cur_Bblock.InstrList.add(ins);
                }
                break;
            case 9:
                //9 LVal '=' 'getint''('')'';'
                visit(stmt.para_LVal);
                //start-移除LVAL的重复load
                if(cur_Bblock.InstrList.get(cur_Bblock.InstrList.size()-1).instrType== Instruction.InstrType.LOAD){
                    cur_Bblock.InstrList.remove(cur_Bblock.InstrList.size()-1);
                    Now_RID--;
                }
                //end  -移除LVAL的重复load

                Instruction i2_para_LVal = new Instruction();
                if(cur_Bblock.InstrList.size()>0){//取一下最后一条指令
                    i2_para_LVal = cur_Bblock.InstrList.get(cur_Bblock.InstrList.size() - 1);
                }

                Instruction i1 = new Instruction();{
                    i1.parentBB = cur_Bblock;
                    i1.instrType = Instruction.InstrType.CALL;
                    i1.result = "%"+(Now_RID++)+" = call i32 @";
                    i1.operation1 = "getint(";
                    i1.operation2 = ")";
                }cur_Bblock.InstrList.add(i1);

                //执行store指令
                Instruction inst_getInt =new Instruction();{
                    inst_getInt.parentBB = cur_Bblock;
                    inst_getInt.instrType = Instruction.InstrType.STORE;
                    inst_getInt.operation3 = null;
                    inst_getInt.operation1 = "%"+(Now_RID-1);
                    Symbol s = sManager.curnow().getToRoot(stmt.para_LVal.para_Ident.TData);
                    if(s.registerID==-1 && s.selfLevels.size()==0) {//全局变量-变量
                        inst_getInt.operation2 = "@" + stmt.para_LVal.para_Ident.TData;
                    }else if(s.registerID==-1 && s.selfLevels.size()!=0){//全局变量-数组
                        inst_getInt.operation2 = stmt.para_LVal.lval_before_equal_registerID;
                    }else if(s.registerID!=-1 && s.selfLevels.size()==0){//局部变量-变量
                        inst_getInt.operation2 = "%" + sManager.curnow().getToRoot(stmt.para_LVal.para_Ident.TData).registerID;
                        if(s.selfType == Value.SelfType.PARA){
                            if(s.flag_ToVar){
                                inst_getInt.operation2 = "%"+s.In_ToVar_registerID;
                            }
                        }
                    }else if(s.registerID!=-1 && s.selfLevels.size()!=0){//局部变量-数组
                        inst_getInt.operation2 = stmt.para_LVal.lval_before_equal_registerID;
                    }
                }cur_Bblock.InstrList.add(inst_getInt);
                break;
            case 10:
                //10 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
                for(Exp exp:stmt.arr_Exp){
                    visit(exp);
                }
                //llvm输出函数
                char[] tmp = stmt.para_FormatString.TData.toCharArray();
                for(int i=1,j=0;i<tmp.length-1;i++){
                    if(i+1< tmp.length-1 && tmp[i]=='\\' && tmp[i+1]=='n'){
                        int c = tmp[i];
                        Instruction in = new Instruction();{
                            in.instrType = Instruction.InstrType.PUTCH;
                            in.result = ""+10;
                        }cur_Bblock.InstrList.add(in);
                        i++;
                        continue;
                    }

                    if(tmp[i]!='%'){
                        int c = tmp[i];
                        Instruction in = new Instruction();{
                            in.instrType = Instruction.InstrType.PUTCH;
                            in.result = ""+c;
                        }cur_Bblock.InstrList.add(in);
                    }else{//%d
                        if(stmt.arr_Exp.get(j).isIntCon()) {
                            Instruction in = new Instruction();{
                                in.instrType = Instruction.InstrType.PUTINT;
                                in.result =""+stmt.arr_Exp.get(j).getInt();
                            }cur_Bblock.InstrList.add(in);
                        }
                        else {
                            Instruction in = new Instruction();{
                                in.instrType = Instruction.InstrType.PUTINT;
                                in.result ="%"+stmt.arr_Exp.get(j).RegisterID;
                            }cur_Bblock.InstrList.add(in);
                        }
                        i++;//多跳一个"%d"中的'd'
                        j++;//拿下一个Exp
                    }
                }
                break;
            default:
                break;
        }
    }
    public void visit(Exp exp){
        //Exp → AddExp
        visit(exp.para_AddExp);
        exp.RegisterID = exp.para_AddExp.RegisterID;
    }
    public void visit(Cond cond){
        // Cond → LOrExp
        visit(cond.para_LOrExp);
    }
    public void visit(ForStmt forStmt){
        //ForStmt → LVal '=' Exp
        visit(forStmt.para_LVal);
        visit(forStmt.para_Exp);
        Instruction inst =new Instruction();{
            inst.parentBB = cur_Bblock;
            inst.instrType = Instruction.InstrType.STORE;
            if(forStmt.para_Exp.isIntCon())
                inst.operation1 = ""+forStmt.para_Exp.getInt();
            else
                inst.operation1 = "%"+forStmt.para_Exp.RegisterID;
            if(sManager.curnow().checkToRoot(forStmt.para_LVal.para_Ident.TData)) {
                Symbol s = sManager.curnow().getToRoot(forStmt.para_LVal.para_Ident.TData);
                if (s.registerID==-1)//是全局变量
                    inst.operation2 = "@" + forStmt.para_LVal.para_Ident.TData;
                else
                    inst.operation2 = "%" + sManager.curnow().getToRoot(forStmt.para_LVal.para_Ident.TData).registerID;
            }
        }cur_Bblock.InstrList.add(inst);
    }
    public void visit(LVal lVal){
        //LVal → Ident {'[' Exp ']'}
        for (Exp exp:lVal.arr_Exp){
            visit(exp);
        }
        String name = lVal.para_Ident.TData;

        //应付全局数组取值
        if(sManager.curnow().checkToRoot(name)){
            Symbol s = sManager.curnow().getToRoot(name);
            if(s.selfLevels.size()==lVal.arr_Exp.size()){//在取值
                if(s.selfLevels.size() == 1){//一维取值
                    if(s.returnNums.size()>0 && lVal.arr_Exp.get(0).isIntCon())
                        lVal.vFor_Array = s.returnNums.get(lVal.arr_Exp.get(0).getInt());
                }else {//二维取值
                    if(s.returnNums.size()>0 && lVal.arr_Exp.get(0).isIntCon() && lVal.arr_Exp.get(1).isIntCon())
                        lVal.vFor_Array = s.returnNums.get(lVal.arr_Exp.get(0).getInt()*s.selfLevels.get(1)+lVal.arr_Exp.get(1).getInt());
                }
            }
        }

        if(sManager.curnow().checkToRoot(name)&&!flag_global_decl) {
            Symbol s = sManager.curnow().getToRoot(name);
            if (s.registerID==-1) {//全局变量
                if(s.selfLevels.size()==0){//全局变量-变量
                    //执行load函数
                    Instruction i = new Instruction();
                    {
                        i.parentBB = cur_Bblock;
                        i.result = "%" + (Now_RID++);
                        i.instrType = Instruction.InstrType.LOAD;
                        i.operation2 = "@" + s.Name;//读取全局变量
                    }
                    cur_Bblock.InstrList.add(i);
                }else{//全局变量-数组
                    //执行getelementptr指令
                    Instruction i1 = new Instruction();{
                        i1.parentBB = cur_Bblock;
                        i1.instrType = Instruction.InstrType.GETELEMENTPTR;
                        i1.result = "%" + (Now_RID++);
                        i1.operation1 = s.get_array_type_all();
                        i1.operation2 = "@" + s.Name;
                        if(s.selfLevels.size()==1) {//一维数组
                            if(lVal.arr_Exp.size()==0) {
                                //一维取一维
                                i1.operation3 = "0";
                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type();
                            }
                            else {
                                //一维取值
                                i1.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                            }
                            i1.operation4 = null;
                        }else{//二维数组
                            if(lVal.arr_Exp.size()==0) {
                                //二维取二维
                                i1.operation3 = "0";
                                i1.operation4 = null;
                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type();
                            }else if(lVal.arr_Exp.size()==1){
                                //二维取一维
                                Instruction i2 = new Instruction();{
                                    i2.parentBB = cur_Bblock;
                                    i2.instrType = Instruction.InstrType.GETELEMENTPTR;
                                    i2.result = i1.result;
                                    i2.operation1 = s.get_array_type_all();
                                    i2.operation2 = "@" + s.Name;
                                    i2.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                    i2.operation4 = null;
                                }
                                cur_Bblock.InstrList.add(i2);//先取二维中对应一维的地址

                                i1.result = "%" + (Now_RID++);
                                //再设定为取上一步中的首地址但变换类型
                                i1.operation1 = s.get_array_type_noStar();
                                i1.operation2 = "%" + (Now_RID-2);
                                i1.operation3 = "0";
                                i1.operation4 = null;

                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type_former();
                            }else{
                                //二维取值
                                i1.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                i1.operation4 = "" + lVal.arr_Exp.get(1).getInt_h();
                            }
                        }
                    }cur_Bblock.InstrList.add(i1);
                    if(!flag_array_get_Level){//如果是在取数组内正常值
                        //执行load函数
                        Instruction i2 = new Instruction();
                        {
                            i2.parentBB = cur_Bblock;
                            i2.result = "%" + (Now_RID++);
                            i2.instrType = Instruction.InstrType.LOAD;
                            i2.operation2 = "%" + (Now_RID - 2);
                        }
                        cur_Bblock.InstrList.add(i2);
                    }
                    flag_array_get_Level = false;
                    lVal.lval_before_equal_registerID = i1.result;//获取"="之前的lval的存储寄存器
                }
            } else {//局部变量
                if(s.selfType!= Value.SelfType.PARA && s.selfType!= Value.SelfType.PARA_ARRAY){//局部变量-变量
                    if(s.selfLevels.size()==0){//局部变量-变量（非数组）
                        //执行load函数
                        Instruction i = new Instruction();
                        {
                            i.parentBB = cur_Bblock;
                            i.result = "%" + (Now_RID++);
                            i.instrType = Instruction.InstrType.LOAD;
                            i.operation2 = "%" + s.registerID;//读取局部变量
                        }
                        cur_Bblock.InstrList.add(i);
                    }else{//局部变量-变量（数组）
                        //执行getelementptr指令
                        Instruction i1 = new Instruction();{
                            i1.parentBB = cur_Bblock;
                            i1.instrType = Instruction.InstrType.GETELEMENTPTR;
                            i1.result = "%" + (Now_RID++);
                            i1.operation1 = s.get_array_type_all();
                            i1.operation2 = "%" + s.registerID;
                            if(s.selfLevels.size()==1) {//一维数组
                                if(lVal.arr_Exp.size()==0) {
                                    //一维取一维
                                    i1.operation3 = "0";
                                    flag_array_get_Level = true;
                                    lVal.Rpara_type = s.get_array_type();
                                }
                                else {
                                    //一维取值
                                    i1.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                }
                                i1.operation4 = null;
                            }else{//二维数组
                                if(lVal.arr_Exp.size()==0) {
                                    //二维取二维
                                    i1.operation3 = "0";
                                    i1.operation4 = null;
                                    flag_array_get_Level = true;
                                    lVal.Rpara_type = s.get_array_type();
                                }else if(lVal.arr_Exp.size()==1){
                                    //二维取一维
                                    Instruction i2 = new Instruction();{
                                        i2.parentBB = cur_Bblock;
                                        i2.instrType = Instruction.InstrType.GETELEMENTPTR;
                                        i2.result = i1.result;
                                        i2.operation1 = s.get_array_type_all();
                                        i2.operation2 = "%" + s.registerID;
                                        i2.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                        i2.operation4 = null;
                                    }
                                    cur_Bblock.InstrList.add(i2);//先取二维中对应一维的地址

                                    i1.result = "%" + (Now_RID++);
                                    //再设定为取上一步中的首地址但变换类型
                                    i1.operation1 = s.get_array_type_noStar();
                                    i1.operation2 = "%" + (Now_RID-2);
                                    i1.operation3 = "0";
                                    i1.operation4 = null;

                                    flag_array_get_Level = true;
                                    lVal.Rpara_type = s.get_array_type_former();
                                }else{
                                    //二维取值
                                    i1.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                    i1.operation4 = "" + lVal.arr_Exp.get(1).getInt_h();
                                }
                            }
                        }cur_Bblock.InstrList.add(i1);
                        if(!flag_array_get_Level){//如果是在取数组内正常值
                            //执行load函数
                            Instruction i2 = new Instruction();
                            {
                                i2.parentBB = cur_Bblock;
                                i2.result = "%" + (Now_RID++);
                                i2.instrType = Instruction.InstrType.LOAD;
                                i2.operation2 = "%" + (Now_RID - 2);
                            }
                            cur_Bblock.InstrList.add(i2);
                        }
                        flag_array_get_Level = false;
                        lVal.lval_before_equal_registerID = i1.result;//获取"="之前的lval的存储寄存器
                    }
                }else if(s.selfType== Value.SelfType.PARA && s.flag_ToVar){//局部变量-参数-已存储
                    //执行load函数
                    Instruction i = new Instruction();{
                        i.parentBB = cur_Bblock;
                        i.result = "%" + (Now_RID++);
                        i.instrType = Instruction.InstrType.LOAD;
                        i.operation2 = "%" + s.In_ToVar_registerID;
                    }cur_Bblock.InstrList.add(i);
                }else if(s.selfType== Value.SelfType.PARA && !s.flag_ToVar) {//局部变量-参数-未存储
                    //先alloca一个新寄存器
                    s.In_ToVar_registerID = Now_RID;
                    s.flag_ToVar = true;
                    s.ToVar_level_id = ToVar_level;
                    Instruction i1 = new Instruction();{
                        i1.parentBB = cur_Bblock;
                        i1.instrType = Instruction.InstrType.ALLOCA;
                        i1.operation1 = "i32";
                        i1.result = "%"+(Now_RID++);
                    }cur_Bblock.InstrList.add(i1);
                    //再存储参数寄存器的值到新寄存器中
                    Instruction i2 = new Instruction();{
                        i2.parentBB = cur_Bblock;
                        i2.instrType = Instruction.InstrType.STORE;
                        i2.operation1 = "%"+sManager.curnow().getToRoot(lVal.para_Ident.TData).registerID;
                        i2.operation2 = "%"+(Now_RID-1);
                    }cur_Bblock.InstrList.add(i2);
                    //最后重新load新寄存器中内容
                    Instruction i3 = new Instruction();{
                        i3.parentBB = cur_Bblock;
                        i3.instrType = Instruction.InstrType.LOAD;
                        i3.operation2 = "%"+(Now_RID-1);
                        i3.result = "%"+(Now_RID++);
                    }cur_Bblock.InstrList.add(i3);
                }else if(s.selfType== Value.SelfType.PARA_ARRAY && s.flag_ToVar){//局部变量-参数(数组)-已存储
                    //load寄存器中内容
                    Instruction i0 = new Instruction();{
                        i0.parentBB = cur_Bblock;
                        i0.instrType = Instruction.InstrType.LOAD;
                        i0.operation2 = "%" + s.In_ToVar_registerID;
                        i0.result = "%"+(Now_RID++);
                        i0.operation3 = ""+s.get_array_type();
                        i0.operation4 = i0.operation3+" *";
                    }cur_Bblock.InstrList.add(i0);
                    //执行getelementptr指令
                    Instruction i1 = new Instruction();{
                        i1.parentBB = cur_Bblock;
                        i1.instrType = Instruction.InstrType.GETELEMENTPTR;
                        i1.result = "%" + (Now_RID++);
                        i1.operation1 = s.get_array_type_noStar();
                        i1.operation2 = "%"+(Now_RID-2);
                        i1.operation5 = "";
                        if(s.selfLevels.size()==1) {//一维数组
                            if(lVal.arr_Exp.size()==0) {
                                //一维取一维
                                i1.operation3 = "0";
                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type();
                            }
                            else {
                                //一维取值
                                i1.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                            }
                            i1.operation4 = null;
                        }else{//二维数组
                            if(lVal.arr_Exp.size()==0) {
                                //二维取二维
                                i1.operation3 = "0";
                                i1.operation4 = null;
                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type();
                            }else if(lVal.arr_Exp.size()==1){
                                //二维取一维
//                                Instruction i2 = new Instruction();{
//                                    i2.parentBB = cur_Bblock;
//                                    i2.instrType = Instruction.InstrType.GETELEMENTPTR;
//                                    i2.result = i1.result;
//                                    i2.operation1 = s.get_array_type_noStar();
//                                    i2.operation2 = i0.result;
//                                    i2.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
//                                    i2.operation4 = null;
//                                }
//                                cur_Bblock.InstrList.add(i2);//先取二维中对应一维的地址
//                                i1.result = "%" + (Now_RID++);

                                //再设定为取上一步中的首地址但变换类型
                                i1.operation1 = s.get_array_type_noStar();
                                i1.operation2 = i0.result;
                                i1.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                i1.operation4 = "0";

                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type_former();
                            }else{
                                //二维取值
                                i1.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                i1.operation4 = "" + lVal.arr_Exp.get(1).getInt_h();
                            }
                        }
                    }cur_Bblock.InstrList.add(i1);
                    if(!flag_array_get_Level){//如果是在取数组内正常值
                        //执行load函数
                        Instruction i2 = new Instruction();
                        {
                            i2.parentBB = cur_Bblock;
                            i2.result = "%" + (Now_RID++);
                            i2.instrType = Instruction.InstrType.LOAD;
                            i2.operation2 = "%" + (Now_RID - 2);
                        }
                        cur_Bblock.InstrList.add(i2);
                    }
                    flag_array_get_Level = false;
                    lVal.lval_before_equal_registerID = i1.result;//获取"="之前的lval的存储寄存器
                }else if(s.selfType== Value.SelfType.PARA_ARRAY && !s.flag_ToVar){//局部变量-参数(数组)-未存储
                    //先alloca一个新寄存器
                    s.In_ToVar_registerID = Now_RID;
                    s.flag_ToVar = true;
                    s.ToVar_level_id = ToVar_level;
                    Instruction i1 = new Instruction();{
                        i1.parentBB = cur_Bblock;
                        i1.instrType = Instruction.InstrType.ALLOCA;
                        i1.operation1 = ""+s.get_array_type();
                        i1.result = "%"+(Now_RID++);
                    }cur_Bblock.InstrList.add(i1);
                    //再store参数寄存器的值到新寄存器中
                    Instruction i2 = new Instruction();{
                        i2.parentBB = cur_Bblock;
                        i2.instrType = Instruction.InstrType.STORE;
                        i2.operation1 = "%"+sManager.curnow().getToRoot(lVal.para_Ident.TData).registerID;
                        i2.operation2 = "%"+(Now_RID-1);
                        i2.operation3 = ""+s.get_array_type();
                        i2.operation4 = i2.operation3+" *";
                    }cur_Bblock.InstrList.add(i2);
                    //最后重新load新寄存器中内容
                    Instruction i3 = new Instruction();{
                        i3.parentBB = cur_Bblock;
                        i3.instrType = Instruction.InstrType.LOAD;
                        i3.operation2 = "%"+(Now_RID-1);
                        i3.result = "%"+(Now_RID++);
                        i3.operation3 = ""+s.get_array_type();
                        i3.operation4 = i3.operation3+" *";
                    }cur_Bblock.InstrList.add(i3);
                    //执行getelementptr指令
                    Instruction i4 = new Instruction();{
                        i4.parentBB = cur_Bblock;
                        i4.instrType = Instruction.InstrType.GETELEMENTPTR;
                        i4.result = "%" + (Now_RID++);
                        i4.operation1 = s.get_array_type_noStar();
                        i4.operation2 = "%" + (Now_RID-2);
                        i4.operation5 = "";
                        if(s.selfLevels.size()==1) {//一维数组
                            if(lVal.arr_Exp.size()==0) {
                                //一维取一维
                                i4.operation3 = "0";
                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type();
                            }
                            else {
                                //一维取值
                                i4.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                            }
                            i4.operation4 = null;
                        }else{//二维数组
                            if(lVal.arr_Exp.size()==0) {
                                //二维取二维
                                i4.operation3 = "0";
                                i4.operation4 = null;
                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type();
                            }else if(lVal.arr_Exp.size()==1){
                                //二维取一维
//                                Instruction i5 = new Instruction();{
//                                    i5.parentBB = cur_Bblock;
//                                    i5.instrType = Instruction.InstrType.GETELEMENTPTR;
//                                    i5.result = i4.result;
//                                    i5.operation1 = s.get_array_type_noStar();
//                                    i5.operation2 = "%" + (Now_RID-2);
//                                    i5.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
//                                    i5.operation4 = null;
//                                }
//                                cur_Bblock.InstrList.add(i5);//先取二维中对应一维的地址
//                                i4.result = "%" + (Now_RID++);

                                //再设定为取上一步中的首地址但变换类型
                                i4.operation1 = s.get_array_type_noStar();
                                i4.operation2 = "%" + (Now_RID-2);
                                i4.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                i4.operation4 = "0";

                                flag_array_get_Level = true;
                                lVal.Rpara_type = s.get_array_type_former();
                            }else{
                                //二维取值
                                i4.operation3 = "" + lVal.arr_Exp.get(0).getInt_h();
                                i4.operation4 = "" + lVal.arr_Exp.get(1).getInt_h();
                            }
                        }
                    }cur_Bblock.InstrList.add(i4);
                    if(!flag_array_get_Level){//如果是在取数组内正常值
                        //执行load函数
                        Instruction i5 = new Instruction();
                        {
                            i5.parentBB = cur_Bblock;
                            i5.result = "%" + (Now_RID++);
                            i5.instrType = Instruction.InstrType.LOAD;
                            i5.operation2 = "%" + (Now_RID - 2);
                        }
                        cur_Bblock.InstrList.add(i5);
                    }
                    flag_array_get_Level = false;
                    lVal.lval_before_equal_registerID = i4.result;//获取"="之前的lval的存储寄存器
                }
            }
        }
    }
    public void visit(PrimaryExp primaryExp){
        //PrimaryExp → '(' Exp ')' | LVal | Number
        if(primaryExp.para_LPARENT.TData!=null){
            visit(primaryExp.para_Exp);
        }else if(primaryExp.para_LVal.para_Ident!=null){
            visit(primaryExp.para_LVal);
        }else{
            visit(primaryExp.para_CNumber);
        }
    }
    public void visit(CNumber cNumber){
        //Number → IntConst

    }
    public void visit(UnaryExp unaryExp){
        //UnaryExp →
        if(unaryExp.para_PrimaryExp.para_CNumber!=null){
            // PrimaryExp
            visit(unaryExp.para_PrimaryExp);
        }else if(unaryExp.para_UnaryExp.para_PrimaryExp!=null){
            // UnaryOp UnaryExp
            visit(unaryExp.para_UnaryOp);
            visit(unaryExp.para_UnaryExp);
            unaryExp.flag_Intcon = unaryExp.para_UnaryExp.flag_Intcon;
            if(unaryExp.para_UnaryOp.para_PMN.type == Init.tokenType.PLUS && !flag_global_decl){
                Instruction i = new Instruction();{
                    i.parentBB = cur_Bblock;
                    i.result = "%"+(Now_RID++);
                    i.instrType = Instruction.InstrType.ADD;
                    i.operation1 = "0";
                    if(unaryExp.para_UnaryExp.isIntCon())
                        i.operation2 = ""+unaryExp.para_UnaryExp.getInt();
                    else
                        i.operation2 = "%"+unaryExp.para_UnaryExp.RegisterID;
                }cur_Bblock.InstrList.add(i);
                unaryExp.flag_Intcon = false;
            }else if(unaryExp.para_UnaryOp.para_PMN.type == Init.tokenType.MINU && !flag_global_decl){
                Instruction i = new Instruction();{
                    i.parentBB = cur_Bblock;
                    i.result = "%"+(Now_RID++);
                    i.instrType = Instruction.InstrType.SUB;
                    i.operation1 = "0";
                    if(unaryExp.para_UnaryExp.isIntCon())
                        i.operation2 = ""+unaryExp.para_UnaryExp.getInt();
                    else
                        i.operation2 = "%"+unaryExp.para_UnaryExp.RegisterID;
                }cur_Bblock.InstrList.add(i);
                unaryExp.flag_Intcon = false;
            }else if(unaryExp.para_UnaryOp.para_PMN.type == Init.tokenType.NOT && !flag_global_decl){
//                flag_gth = true;
                //icmp eq 0 一下
                Instruction i = new Instruction();
                {
                    i.parentBB = cur_Bblock;
                    i.result = "%" + (Now_RID++);
                    i.instrType = Instruction.InstrType.ICMP_EQ;
                    i.operation1 = "0";
                    i.operation2 = ""+unaryExp.para_UnaryExp.getInt_h();
                }cur_Bblock.InstrList.add(i);
                //zext指令
                Instruction i2 = new Instruction();{
                    i2.parentBB = cur_Bblock;
                    i2.instrType = Instruction.InstrType.ZEXT;
                    i2.result = "%" + (Now_RID++);
                    i2.operation1 = "%" + (Now_RID-2);
                }
                cur_Bblock.InstrList.add(i2);
            }
        }else {
            // Ident '(' [FuncRParams] ')'
            if(unaryExp.para_FuncRParams.arr_Exp!=null)
                visit(unaryExp.para_FuncRParams);
            Function f = module.findFunc(unaryExp.para_Ident.TData);
            Instruction i =new Instruction();{
                i.parentBB = cur_Bblock;
                if(f.retType== Value.ReturnType.INT)
                    i.result = "%"+(Now_RID++)+" = call i32 @";
                else if(f.retType == Value.ReturnType.VOID)
                    i.result = "call void @";
                i.instrType = Instruction.InstrType.CALL;
                i.operation1 = unaryExp.para_Ident.TData+"(";
                i.operation2 = "";
                if(unaryExp.para_FuncRParams.arr_Exp!=null) {
                    for (int j = 0; j < unaryExp.para_FuncRParams.arr_Exp.size(); j++) {
//                        i.operation2 += unaryExp.para_FuncRParams.arr_Exp.get(j).getRparaType()+" %" + unaryExp.para_FuncRParams.arr_Exp.get(j).RegisterID;
                        i.operation2 += unaryExp.para_FuncRParams.arr_Exp.get(j).getRparaType()+" " + unaryExp.para_FuncRParams.arr_Exp.get(j).getInt_h();
                        if (j != unaryExp.para_FuncRParams.arr_Exp.size() - 1)
                            i.operation2 += ", ";
                    }
                }
                i.operation2+=")";
            }cur_Bblock.InstrList.add(i);
        }
        unaryExp.RegisterID = Now_RID-1;
    }
    public void visit(UnaryOp unaryOp){
        //UnaryOp → '+' | '−' | '!'

    }
    public void visit(FuncRParams funcRParams){
        //FuncRParams → Exp { ',' Exp }
        for(Exp exp:funcRParams.arr_Exp){
            visit(exp);
        }
    }
    public void visit(MulExp mulExp){
        //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        //改写：UnaryExp { ('*' | '/' | '%') UnaryExp }
        for(int i=0;i<mulExp.arr_UnaryExp.size();i++){
            visit(mulExp.arr_UnaryExp.get(i));
            if(i>0 && !flag_global_decl){
                Instruction in = new Instruction();{
                    in.parentBB = cur_Bblock;
                    //结果数
                    in.result = "%" + (Now_RID++);
                    //指令类型
                    if(mulExp.arr_MDM.get(i-1).type == Init.tokenType.MULT)
                        in.instrType = Instruction.InstrType.MUL;
                    else if(mulExp.arr_MDM.get(i-1).type== Init.tokenType.DIV)
                        in.instrType = Instruction.InstrType.SDIV;
                    else if(mulExp.arr_MDM.get(i-1).type== Init.tokenType.MOD)
                        in.instrType = Instruction.InstrType.SREM;
                    //操作数1
                    if(mulExp.arr_UnaryExp.get(i-1).isIntCon()){//如果是纯数字
                        in.operation1 = ""+mulExp.arr_UnaryExp.get(i-1).getInt();
                    }else{
                        in.operation1 = "%"+mulExp.arr_UnaryExp.get(i-1).RegisterID;
                    }
                    //操作数2
                    if(mulExp.arr_UnaryExp.get(i).isIntCon()){//如果是纯数字
                        in.operation2 = ""+mulExp.arr_UnaryExp.get(i).getInt();
                    }else{
                        in.operation2 = "%"+mulExp.arr_UnaryExp.get(i).RegisterID;
                    }
                    mulExp.arr_UnaryExp.get(i).RegisterID = Now_RID-1;
                    mulExp.arr_UnaryExp.get(i).flag_Intcon = false;
                }cur_Bblock.InstrList.add(in);
            }
        }
        mulExp.RegisterID = Now_RID - 1;
    }
    public void visit(AddExp addExp){
        //AddExp → MulExp | AddExp ('+' | '−') MulExp
        //改写：MulExp {('+' | '−') MulExp}
        for(int i=0;i<addExp.arr_MulExp.size();i++){
            visit(addExp.arr_MulExp.get(i));
            if(i>0 && !flag_global_decl){
                Instruction in = new Instruction();{
                    in.parentBB = cur_Bblock;
                    //结果数
                    in.result = "%" + (Now_RID++);
                    //指令类型
                    if(addExp.arr_PLUSandMINU.get(i-1).type== Init.tokenType.PLUS)
                        in.instrType = Instruction.InstrType.ADD;
                    else if(addExp.arr_PLUSandMINU.get(i-1).type== Init.tokenType.MINU)
                        in.instrType = Instruction.InstrType.SUB;
                    //操作数1
                    if(addExp.arr_MulExp.get(i-1).isIntCon()){//如果是纯数字
                        in.operation1 = ""+addExp.arr_MulExp.get(i-1).getInt();
                    }else{
                        in.operation1 = "%"+addExp.arr_MulExp.get(i-1).RegisterID;
                    }
                    //操作数2
                    if(addExp.arr_MulExp.get(i).isIntCon()){//如果是纯数字
                        in.operation2 = ""+addExp.arr_MulExp.get(i).getInt();
                    }else{
                        in.operation2 = "%"+addExp.arr_MulExp.get(i).RegisterID;
                    }
                    addExp.arr_MulExp.get(i).RegisterID = Now_RID-1;
                    addExp.arr_MulExp.get(i).flag_Intcon = false;
                }cur_Bblock.InstrList.add(in);
            }
        }
        addExp.RegisterID = Now_RID - 1;
    }
    public void visit(RelExp relExp){
        //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        //改写：AddExp {('<' | '>' | '<=' | '>=') AddExp}
        for(int j=0;j<relExp.arr_AddExp.size();j++){
            visit(relExp.arr_AddExp.get(j));
            if(j>0){
                //icmp指令
                Instruction i1 = new Instruction();{
                    i1.parentBB = cur_Bblock;
                    if(relExp.arr_OPT.get(j-1).type == Init.tokenType.LSS)
                        i1.instrType = Instruction.InstrType.ICMP_SLT;
                    else if(relExp.arr_OPT.get(j-1).type == Init.tokenType.LEQ)
                        i1.instrType = Instruction.InstrType.ICMP_SLE;
                    else if(relExp.arr_OPT.get(j-1).type == Init.tokenType.GRE)
                        i1.instrType = Instruction.InstrType.ICMP_SGT;
                    else if(relExp.arr_OPT.get(j-1).type == Init.tokenType.GEQ)
                        i1.instrType = Instruction.InstrType.ICMP_SGE;
                    i1.result = "%" + (Now_RID++);
                    if (relExp.arr_AddExp.get(j-1).isIntCon())
                        i1.operation1 = ""+relExp.arr_AddExp.get(j-1).getInt();
                    else
                        i1.operation1 = "%"+relExp.arr_AddExp.get(j-1).RegisterID;

                    if (relExp.arr_AddExp.get(j).isIntCon())
                        i1.operation2 = ""+relExp.arr_AddExp.get(j).getInt();
                    else
                        i1.operation2 = "%"+relExp.arr_AddExp.get(j).RegisterID;
                    relExp.arr_AddExp.get(j).RegisterID = Now_RID;
                }
                cur_Bblock.InstrList.add(i1);
                //zext指令
                Instruction i2 = new Instruction();{
                    i2.parentBB = cur_Bblock;
                    i2.instrType = Instruction.InstrType.ZEXT;
                    i2.result = "%" + (Now_RID++);
                    i2.operation1 = "%" + (Now_RID-2);
                }
                cur_Bblock.InstrList.add(i2);
            }
        }
        relExp.registerID = Now_RID-1;
    }
    public void visit(EqExp eqExp){
        //EqExp → RelExp | EqExp ('==' | '!=') RelExp
        //改写： RelExp { ('==' | '!=') RelExp}
        for(int j=0;j<eqExp.arr_RelExp.size();j++){
            visit(eqExp.arr_RelExp.get(j));
            if(j>0){
                //icmp指令
                Instruction i1 = new Instruction();{
                    i1.parentBB = cur_Bblock;
                    if(eqExp.arr_EQLorNEQ.get(j-1).type == Init.tokenType.EQL)
                        i1.instrType = Instruction.InstrType.ICMP_EQ;
                    else if(eqExp.arr_EQLorNEQ.get(j-1).type == Init.tokenType.NEQ)
                        i1.instrType = Instruction.InstrType.ICMP_NE;
                    i1.result = "%" + (Now_RID++);
                    i1.operation1 = ""+eqExp.arr_RelExp.get(j-1).getInt_h();
                    i1.operation2 = ""+eqExp.arr_RelExp.get(j).getInt_h();
                    eqExp.arr_RelExp.get(j).registerID = Now_RID;
                }
                cur_Bblock.InstrList.add(i1);
                //zext指令
                Instruction i2 = new Instruction();{
                    i2.parentBB = cur_Bblock;
                    i2.instrType = Instruction.InstrType.ZEXT;
                    i2.result = "%" + (Now_RID++);
                    i2.operation1 = "%" + (Now_RID-2);
                }
                cur_Bblock.InstrList.add(i2);
            }
        }
        eqExp.registerID = Now_RID-1;
    }
    public void visit(LAndExp lAndExp){
        //LAndExp → EqExp | LAndExp '&&' EqExp
        //改写：EqExp { '&&' EqExp }
        for(int j=0;j<lAndExp.arr_EqExp.size();j++){
            visit(lAndExp.arr_EqExp.get(j));
//            if(lAndExp.arr_EqExp.get(j).arr_RelExp.size()==1 && flag_continue_cond){
            if(j!=lAndExp.arr_EqExp.size()-1){
                //icmp指令
                Instruction i = new Instruction();
                {
                    i.parentBB = cur_Bblock;
                    i.instrType = Instruction.InstrType.ICMP_NE;
                    if(flag_gth){
                        i.instrType = Instruction.InstrType.ICMP_EQ;
                        flag_gth = false;
                    }
                    i.result = "%" + (Now_RID++);lAndExp.arr_EqExp.get(j).registerID = Now_RID -1;
                    i.operation1 = "0";
                    if (cur_Bblock.BBid == Now_RID - 2 && lAndExp.arr_EqExp.get(j).isIntCon()) {
                        i.operation2 = "" + lAndExp.arr_EqExp.get(j).getInt();
                    } else {
                        i.operation2 = "%" + (Now_RID - 2);
                    }
                }
                cur_Bblock.InstrList.add(i);
                //br i1 指令
                Instruction i2 = new Instruction();
                {
                    i2.parentBB = cur_Bblock;
                    i2.instrType = Instruction.InstrType.BRI1;
                    i2.result = "%" + (Now_RID - 1);
                    i2.operation1 = "% nextandLabel";
                    i2.operation2 = "% nextStmt1";
                }
                cur_Bblock.InstrList.add(i2);
                //定义基本块
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                lAndExp.needs.add(cur_Bblock);//加入回填列表
                BasicBlock b = new BasicBlock();
                {
                    b.fromFunc = cur_func;
                    b.BBid = Now_RID++;
                }
                cur_Bblock = b;
            }
        }
        lAndExp.registerID = Now_RID-1;
    }
    public void visit(LOrExp lOrExp){
        //LOrExp → LAndExp | LOrExp '||' LAndExp
        //改写：LAndExp { '||' LAndExp }
        for(int j=0;j<lOrExp.arr_LAndExp.size();j++){
            visit(lOrExp.arr_LAndExp.get(j));
            if(true){
                //icmp指令
                Instruction i = new Instruction();
                {
                    i.parentBB = cur_Bblock;
                    i.instrType = Instruction.InstrType.ICMP_NE;
                    if(flag_gth){
                        i.instrType = Instruction.InstrType.ICMP_EQ;
                        flag_gth = false;
                    }
                    i.result = "%" + (Now_RID++);
                    i.operation1 = "0";
                    if (cur_Bblock.BBid == Now_RID - 2 && lOrExp.arr_LAndExp.get(j).arr_EqExp.get(lOrExp.arr_LAndExp.get(j).arr_EqExp.size()-1).isIntCon()) {
                        i.operation2 = "" + lOrExp.arr_LAndExp.get(j).arr_EqExp.get(lOrExp.arr_LAndExp.get(j).arr_EqExp.size()-1).getInt();
                    } else {
                        i.operation2 = "%" + (Now_RID - 2);
                    }
                }
                cur_Bblock.InstrList.add(i);
                lOrExp.arr_LAndExp.get(j).registerID = Now_RID - 1;
                lOrExp.arr_LAndExp.get(j).arr_EqExp.get(0).registerID = Now_RID-1;
                lOrExp.arr_LAndExp.get(j).arr_EqExp.get(0).arr_RelExp.get(0).registerID = Now_RID - 1;
                //br i1 指令
                Instruction i2 = new Instruction();
                {
                    i2.parentBB = cur_Bblock;
                    i2.instrType = Instruction.InstrType.BRI1;
                    i2.result = "%" + (Now_RID - 1);
                    i2.operation1 = "% nextStmt1";
                    i2.operation2 = "% nextorLabel";
                }
                cur_Bblock.InstrList.add(i2);
                lOrExp.needs.add(cur_Bblock);//加入回填列表
                //定义基本块
                cur_func.BblockList.add(cur_Bblock);//保存先前基本块
                BasicBlock b = new BasicBlock();
                {
                    b.fromFunc = cur_func;
                    b.BBid = Now_RID++;
                }
                cur_Bblock = b;


            }
        }
        lOrExp.registerID = Now_RID-1;
    }
    public void visit(ConstExp constExp){
        //ConstExp → AddExp
        visit(constExp.para_AddExp);
        constExp.RegisterID = Now_RID-1;
    }
}
