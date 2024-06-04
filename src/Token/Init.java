package Token;

import Token.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class Init {
    public static boolean flag_run = true;
    //读取文件内容
    public static void read(StringBuilder theData, String path) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(path));
        Scanner sc = new Scanner(in);
        //把文件内容按行读入到theData中
        while(sc.hasNextLine()){
            theData.append(sc.nextLine());
            theData.append("\n");
        }
        sc.close();
        in.close();
    }
    //输出文件内容
    public static void write1(ArrayList<Token> arr_token, String path) throws IOException{
        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        String data;
        for(int i=0;i<arr_token.size();i++){
            data = arr_token.get(i).type+" "+arr_token.get(i).TData;
            out.write(data.getBytes());
            out.write('\n');
        }
        out.close();
    }
    public static void write2(ArrayList<String> arr_Parser, String path) throws IOException{
        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        String data;
        for(int i=0;i<arr_Parser.size();i++){
            data = arr_Parser.get(i);
            out.write(data.getBytes());
            out.write('\n');
        }
        out.close();
    }
    public static void write3(ArrayList<Errors> arr_Error, String path) throws IOException{
        if(flag_run){
            return;
        }
        Collections.sort(arr_Error,new Comparator<Errors>() {
            @Override
            public int compare(Errors error1, Errors error2) {
                return error1.LineHeight-error2.LineHeight;
            }
        });
        OutputStream e = new BufferedOutputStream(new FileOutputStream(path));
        String data;
        for(int i=0;i<arr_Error.size();i++){
            data = arr_Error.get(i).toString();
            e.write(data.getBytes());
            e.write('\n');
        }
        e.close();
    }
    public static void write4(ArrayList<String> arr_llvm, String path)throws IOException{
        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        String data;
        for(int i=0;i<arr_llvm.size();i++){
            data = arr_llvm.get(i);
            out.write(data.getBytes());
        }
        out.close();
    }
    public static void write5(ArrayList<String> arr_mips, String path)throws IOException{
        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        String data;
        for(int i=0;i<arr_mips.size();i++){
            data = arr_mips.get(i);
            out.write(data.getBytes());
        }
        out.close();
    }
    //枚举类别码
    public enum tokenType{
        IDENFR,
        INTCON,
        STRCON,
        MAINTK,
        CONSTTK,
        INTTK,
        BREAKTK,
        CONTINUETK,
        IFTK,
        ELSETK,
        NOT,
        AND,
        OR,
        FORTK,
        GETINTTK,
        PRINTFTK,
        RETURNTK,
        PLUS,
        MINU,
        VOIDTK,
        MULT,
        DIV,
        MOD,
        LSS,
        LEQ,
        GRE,
        GEQ,
        EQL,
        NEQ,
        ASSIGN,
        SEMICN,
        COMMA,
        LPARENT,
        RPARENT,
        LBRACK,
        RBRACK,
        LBRACE,
        RBRACE
    };
    //建立映射关系
    public static void initMap(ArrayList<TokenMap> arr_ListMap){
        arr_ListMap.add(new TokenMap("Ident",tokenType.IDENFR));
        arr_ListMap.add(new TokenMap("IntConst",tokenType.INTCON));
        arr_ListMap.add(new TokenMap("FormatString",tokenType.STRCON));
        arr_ListMap.add(new TokenMap("main",tokenType.MAINTK));
        arr_ListMap.add(new TokenMap("const",tokenType.CONSTTK));
        arr_ListMap.add(new TokenMap("int",tokenType.INTTK));
        arr_ListMap.add(new TokenMap("break",tokenType.BREAKTK));
        arr_ListMap.add(new TokenMap("continue",tokenType.CONTINUETK));
        arr_ListMap.add(new TokenMap("if",tokenType.IFTK));
        arr_ListMap.add(new TokenMap("else",tokenType.ELSETK));
        arr_ListMap.add(new TokenMap("!",tokenType.NOT));
        arr_ListMap.add(new TokenMap("&&",tokenType.AND));
        arr_ListMap.add(new TokenMap("||",tokenType.OR));
        arr_ListMap.add(new TokenMap("for",tokenType.FORTK));
        arr_ListMap.add(new TokenMap("getint",tokenType.GETINTTK));
        arr_ListMap.add(new TokenMap("printf",tokenType.PRINTFTK));
        arr_ListMap.add(new TokenMap("return",tokenType.RETURNTK	));
        arr_ListMap.add(new TokenMap("+",tokenType.PLUS));
        arr_ListMap.add(new TokenMap("-",tokenType.MINU));
        arr_ListMap.add(new TokenMap("void",tokenType.VOIDTK));
        arr_ListMap.add(new TokenMap("*",tokenType.MULT));
        arr_ListMap.add(new TokenMap("/",tokenType.DIV));
        arr_ListMap.add(new TokenMap("%",tokenType.MOD));
        arr_ListMap.add(new TokenMap("<",tokenType.LSS));
        arr_ListMap.add(new TokenMap("<=",tokenType.LEQ));
        arr_ListMap.add(new TokenMap(">",tokenType.GRE));
        arr_ListMap.add(new TokenMap(">=",tokenType.GEQ));
        arr_ListMap.add(new TokenMap("==",tokenType.EQL));
        arr_ListMap.add(new TokenMap("!=",tokenType.NEQ));
        arr_ListMap.add(new TokenMap("=",tokenType.ASSIGN));
        arr_ListMap.add(new TokenMap(";",tokenType.SEMICN));
        arr_ListMap.add(new TokenMap(",",tokenType.COMMA));
        arr_ListMap.add(new TokenMap("(",tokenType.LPARENT));
        arr_ListMap.add(new TokenMap(")",tokenType.RPARENT));
        arr_ListMap.add(new TokenMap("[",tokenType.LBRACK));
        arr_ListMap.add(new TokenMap("]",tokenType.RBRACK));
        arr_ListMap.add(new TokenMap("{",tokenType.LBRACE));
        arr_ListMap.add(new TokenMap("}",tokenType.RBRACE));
    }

}
