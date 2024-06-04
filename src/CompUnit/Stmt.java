package CompUnit;

import LLVM.BasicBlock;
import Token.Token;

import java.util.ArrayList;

public class Stmt {
    public int type;
    // Stmt →
    //1 LVal '=' Exp ';' // 每种类型的语句都要覆盖
    public LVal para_LVal;
    public Token para_ASSIGN;
    public Exp para_Exp;
    public Token para_SEMICN;

    public Stmt(int type, LVal para_LVal, Token para_ASSIGN, Exp para_Exp, Token para_SEMICN) {
        this.type = type;
        this.para_LVal = para_LVal;
        this.para_ASSIGN = para_ASSIGN;
        this.para_Exp = para_Exp;
        this.para_SEMICN = para_SEMICN;
    }
    //2 [Exp] ';' //有无Exp两种情况

    public Stmt(int type, Exp para_Exp, Token para_SEMICN) {
        this.type = type;
        this.para_Exp = para_Exp;
        this.para_SEMICN = para_SEMICN;
    }

    //3 Block
    public Block para_Block;

    public Stmt(int type, Block para_Block) {
        this.type = type;
        this.para_Block = para_Block;
    }
    //4 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    public Token para_IFTK;
    public Token para_LPARENT;
    public Cond para_Cond;
    public Token para_RPARENT;
    public Stmt para_Stmt1;
    public Token para_ELSETK;
    public Stmt para_Stmt2;

    public Stmt(int type, Token para_IFTK, Token para_LPARENT, Cond para_Cond, Token para_RPARENT, Stmt para_Stmt1, Token para_ELSETK, Stmt para_Stmt2) {
        this.type = type;
        this.para_IFTK = para_IFTK;
        this.para_LPARENT = para_LPARENT;
        this.para_Cond = para_Cond;
        this.para_RPARENT = para_RPARENT;
        this.para_Stmt1 = para_Stmt1;
        this.para_ELSETK = para_ELSETK;
        this.para_Stmt2 = para_Stmt2;
    }
    //5 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    public Token para_FORTK;
    public ForStmt para_ForStmt1;
    public Token para_SEMICN1;
    public Token para_SEMICN2;
    public ForStmt para_ForStmt2;
    public Stmt para_Stmt;

    public Stmt(int type, Token para_FORTK, Token para_LPARENT, ForStmt para_ForStmt1, Token para_SEMICN1, Cond para_Cond, Token para_SEMICN2, ForStmt para_ForStmt2, Token para_RPARENT, Stmt para_Stmt) {
        this.type = type;
        this.para_FORTK = para_FORTK;
        this.para_LPARENT = para_LPARENT;
        this.para_ForStmt1 = para_ForStmt1;
        this.para_SEMICN1 = para_SEMICN1;
        this.para_Cond = para_Cond;
        this.para_SEMICN2 = para_SEMICN2;
        this.para_ForStmt2 = para_ForStmt2;
        this.para_RPARENT = para_RPARENT;
        this.para_Stmt = para_Stmt;
    }
    //6 'break' ';'
    public Token para_BREAKTK;

    public Stmt(int type, Token para_BREAKTK, Token para_SEMICN) {
        this.type = type;
        if(type == 6)
            this.para_BREAKTK = para_BREAKTK;
        else if(type == 7)
            this.para_CONTINUETK = para_BREAKTK;
        this.para_SEMICN = para_SEMICN;
    }

    //7 'continue' ';'
    //构造同6
    public Token para_CONTINUETK;


    //8 'return' [Exp] ';' // 1.有Exp 2.无Exp
    public Token para_RETURNTK;

    public Stmt(int type, Token para_RETURNTK, Exp para_Exp, Token para_SEMICN) {
        this.type = type;
        this.para_RETURNTK = para_RETURNTK;
        this.para_Exp = para_Exp;
        this.para_SEMICN = para_SEMICN;
    }
    //9 LVal '=' 'getint''('')'';'
    public Token para_GETINTTK;

    public Stmt(int type, LVal para_LVal, Token para_ASSIGN, Token para_GETINTTK, Token para_LPARENT, Token para_RPARENT, Token para_SEMICN) {
        this.type = type;
        this.para_LVal = para_LVal;
        this.para_ASSIGN = para_ASSIGN;
        this.para_GETINTTK = para_GETINTTK;
        this.para_LPARENT = para_LPARENT;
        this.para_RPARENT = para_RPARENT;
        this.para_SEMICN = para_SEMICN;
    }

    //10 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public Token para_PRINTFTK;
    public Token para_FormatString;
    public ArrayList<Token> arr_COMMA=new ArrayList<>();
    public ArrayList<Exp> arr_Exp = new ArrayList<>();

    public Stmt(int type, Token para_PRINTFTK, Token para_LPARENT, Token para_FormatString, ArrayList<Token> arr_COMMA, ArrayList<Exp> arr_Exp, Token para_RPARENT, Token para_SEMICN) {
        this.type = type;
        this.para_PRINTFTK = para_PRINTFTK;
        this.para_LPARENT = para_LPARENT;
        this.para_FormatString = para_FormatString;
        this.arr_COMMA = arr_COMMA;
        this.arr_Exp = arr_Exp;
        this.para_RPARENT = para_RPARENT;
        this.para_SEMICN = para_SEMICN;
    }

    public Stmt() {
        this.type = -1;
    }


}
