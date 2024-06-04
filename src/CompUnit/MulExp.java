package CompUnit;

import Token.Init;
import Token.Token;

import java.util.ArrayList;

public class MulExp {
    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    //改写：UnaryExp { ('*' | '/' | '%') UnaryExp }
    public ArrayList<Token> arr_MDM;
    public ArrayList<UnaryExp> arr_UnaryExp;
    public int RegisterID;

    public boolean flag_Intcon=true;
    public MulExp(ArrayList<Token> arr_MDM, ArrayList<UnaryExp> arr_UnaryExp,int RegisterID) {
        this.arr_MDM = arr_MDM;
        this.arr_UnaryExp = arr_UnaryExp;
    }

    public MulExp() {
    }
    public int getInt() {
        int tmp=arr_UnaryExp.get(0).getInt();
        if(arr_UnaryExp.size()>1){
            for(int i=1;i<arr_UnaryExp.size();i++){
                if(arr_MDM.get(i-1).type == Init.tokenType.MULT){// *
                    tmp*=arr_UnaryExp.get(i).getInt();
                }else if(arr_MDM.get(i-1).type == Init.tokenType.DIV){// /
                    tmp/=arr_UnaryExp.get(i).getInt();
                }else{// %
                    tmp%=arr_UnaryExp.get(i).getInt();
                }
            }
        }
        return tmp;
    }
    public boolean isIntCon() {
        if(flag_Intcon==false)
            return false;
        if(arr_UnaryExp.size()>1)
            return false;
        if(arr_UnaryExp.get(0).para_UnaryOp.para_PMN!=null)
            return false;
        return arr_UnaryExp.get(arr_UnaryExp.size()-1).isIntCon();
    }
    public String getRparaType(){
        return arr_UnaryExp.get(0).getRparaType();
    }
}
