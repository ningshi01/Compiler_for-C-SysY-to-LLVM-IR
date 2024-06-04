import Semantics.Semantics;
import Token.*;
import LLVM.*;
import java.util.ArrayList;

import static java.lang.Character.*;

public class Deal {
    public static boolean flag_run = true;
    public static void analyse_1(StringBuilder theData, ArrayList<Token> arr_token, ArrayList<TokenMap> arr_ListMap) {
        char[] tmp = theData.toString().toCharArray();
        String Tdata="";
        Init.tokenType Ttype;
        int lineNum = 1;
        for(int i=0;i<tmp.length;i++){
            char c = tmp[i];
            if(c == '+'){
                Tdata += c;
                Ttype = Init.tokenType.PLUS;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == '-'){
                Tdata += c;
                Ttype = Init.tokenType.MINU;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == '*'){
                Tdata += c;
                Ttype = Init.tokenType.MULT;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == '%'){
                Tdata += c;
                Ttype = Init.tokenType.MOD;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == ';'){
                Tdata += c;
                Ttype = Init.tokenType.SEMICN;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == ','){
                Tdata += c;
                Ttype = Init.tokenType.COMMA;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == '('){
                Tdata += c;
                Ttype = Init.tokenType.LPARENT;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == ')'){
                Tdata += c;
                Ttype = Init.tokenType.RPARENT;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == '['){
                Tdata += c;
                Ttype = Init.tokenType.LBRACK;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == ']'){
                Tdata += c;
                Ttype = Init.tokenType.RBRACK;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == '{'){
                Tdata += c;
                Ttype = Init.tokenType.LBRACE;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if(c == '}'){
                Tdata += c;
                Ttype = Init.tokenType.RBRACE;
                arr_token.add(new Token(Tdata,Ttype,lineNum));
                Tdata = "";
            }else if (isLetter(c) || c=='_') {
                for(;i<tmp.length;i++){
                    c=tmp[i];
                    if(isLetter(c) || isDigit(c) || c=='_'){
                        Tdata += c;
                    }else{
                        Ttype = Compiler.findType(arr_ListMap,Tdata);
                        if(Ttype==null)
                            Ttype = Init.tokenType.IDENFR;
                        arr_token.add(new Token(Tdata,Ttype,lineNum));
                        Tdata= "";
                        i-=1;
                        break;
                    }
                }
            }else if(isDigit(c)){
                //十进制
                for(;i<tmp.length;i++){
                    c=tmp[i];
                    if(isDigit(c)){
                        Tdata += c;
                    }else{
                        Ttype = Init.tokenType.INTCON;
                        arr_token.add(new Token(Tdata,Ttype,lineNum));
                        Tdata= "";
                        i-=1;
                        break;
                    }
                }
            }else if(c=='"'){
                Tdata+=c;
                int formatNum=0;
                for(i=i+1;i<tmp.length;i++){
                    c=tmp[i];
                    if(c!='"'){
                        if(c=='%' && tmp[i+1]=='d'){
                            formatNum++;
                        }
                        if(c=='%' && tmp[i+1]!='d') {
                            //Error-a
                            Errors._a(Compiler._Error(), lineNum);
                        }
                        if(c==92 && tmp[i+1]!='n'){
                            //Error-a
                            Errors._a(Compiler._Error(), lineNum);
                        }
                        if((c<40 || c>126 )&& c!='%'){
                            if(c!=32 && c!=33){
                                //Error-a
                                Errors._a(Compiler._Error(), lineNum);
                            }
                        }
                        Tdata += c;
                    }else{
                        Tdata += c;
                        Ttype = Init.tokenType.STRCON;
                        Token token = new Token(Tdata,Ttype,lineNum);
                        token.FormatNum = formatNum;
                        arr_token.add(token);
                        Tdata= "";
                        break;
                    }
                }
            }else if(c=='!'){
                Tdata+=c;
                i+=1;
                if(i< tmp.length && tmp[i]=='=') {//!=
                    c=tmp[i];
                    Tdata += c;
                    Ttype = Init.tokenType.NEQ;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                }else{//!
                    Ttype = Init.tokenType.NOT;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                    i-=1;
                }
            }else if(c=='<'){
                Tdata+=c;
                i+=1;
                if(i< tmp.length && tmp[i]=='=') {//<=
                    c=tmp[i];
                    Tdata += c;
                    Ttype = Init.tokenType.LEQ;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                }else{//<
                    Ttype = Init.tokenType.LSS;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                    i-=1;
                }
            }else if(c=='>'){
                Tdata+=c;
                i+=1;
                if(i< tmp.length && tmp[i]=='=') {//>=
                    c=tmp[i];
                    Tdata += c;
                    Ttype = Init.tokenType.GEQ;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                }else{//>
                    Ttype = Init.tokenType.GRE;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                    i-=1;
                }
            }else if(c=='='){
                Tdata+=c;
                i+=1;
                if(i< tmp.length && tmp[i]=='=') {//==
                    c=tmp[i];
                    Tdata += c;
                    Ttype = Init.tokenType.EQL;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                }else{//=
                    Ttype = Init.tokenType.ASSIGN;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                    i-=1;
                }
            }else if(c=='&'){
                Tdata+=c;
                i+=1;
                if(i< tmp.length && tmp[i]=='&') {//&&
                    c=tmp[i];
                    Tdata += c;
                    Ttype = Init.tokenType.AND;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                }else{
                    //Error

                    Tdata= "";
                    i-=1;
                }
            }else if(c=='|'){
                Tdata+=c;
                i+=1;
                if(i< tmp.length && tmp[i]=='|') {//||
                    c=tmp[i];
                    Tdata += c;
                    Ttype = Init.tokenType.OR;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata= "";
                }else{
                    //Error

                    Tdata= "";
                    i-=1;
                }
            }else if(c=='/'){
                i+=1;
                if(i< tmp.length && tmp[i]=='/') {// 单行注释
                    for(;i<tmp.length;i++){
                        c=tmp[i];
                        if(c=='\n'){
                            i-=1;
                            break;
                        }
                    }
                }else if(i< tmp.length && tmp[i]=='*'){// 多行注释
                    for(;i<tmp.length;i++){
                        c=tmp[i];
                        if(c=='*' && i+1< tmp.length && tmp[i+1]=='/'){
                            i+=1;
                            break;
                        }
                    }
                }else{// 除号
                    Tdata += c;
                    Ttype = Init.tokenType.DIV;
                    arr_token.add(new Token(Tdata,Ttype,lineNum));
                    Tdata = "";
                    i-=1;
                }
            } else if (c == '\n') {
                lineNum++;
            } else if(isWhitespace(c)){
                continue;
            }else{
                System.out.println("???");
            }
        }
        if(arr_token.size()==0)
            flag_run = false;
    }
    public static void analyse_2(ArrayList<Token> arr_token,ArrayList<String> arr_Parser){
        if(flag_run == false)
            return;
        int index = 0;
        Parser parser = new Parser();
        parser.analyse(index,arr_token,arr_Parser);
    }
    public static void analyse_3(ArrayList<Errors> arr_Error,boolean error_flag,boolean flag){
        error_flag = flag;
        if(arr_Error.size()>0){
            flag_run = false;
            Init.flag_run = false;
        }
    }
    public static void analyse_4(){
        if(flag_run == false)
            return;
//        //打印Semantics符号表
//        Semantics semantics = Semantics.getInstance();
//        Semantics.getRoot().print();

        //遍历AST
        ForIR llvm = new ForIR();
        llvm.visit(Parser.getInstance());
        //打印SymbolManager符号表
        SymbolManager symbolManager = SymbolManager.getInstance();
        symbolManager.getRoot().print();
        //生成LLVM IR
        IRbuilder.ToIR(Compiler._LLVM());
    }
    public static void analyse_5(){
        ForMIPS forMIPS = ForMIPS.getInstance();
        forMIPS.genMips();
    }
}
