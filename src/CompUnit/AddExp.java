package CompUnit;


import Token.Init;
import Token.Token;

import java.util.ArrayList;

public class AddExp {
    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    //改写：MulExp {('+' | '−') MulExp}
    public ArrayList<Token> arr_PLUSandMINU;
    public ArrayList<MulExp> arr_MulExp;
    public int RegisterID;
    public AddExp(ArrayList<Token> arr_PLUSandMINU, ArrayList<MulExp> arr_MulExp, int registerID) {
        this.arr_PLUSandMINU = arr_PLUSandMINU;
        this.arr_MulExp = arr_MulExp;
    }

    public AddExp() {
    }

    public int getInt() {
        int tmp=arr_MulExp.get(0).getInt();
        if(arr_MulExp.size()>1){
            for(int i=1;i<arr_MulExp.size();i++){
                if(arr_PLUSandMINU.get(i-1).type== Init.tokenType.PLUS){
                    tmp+=arr_MulExp.get(i).getInt();
                }else{
                    tmp-=arr_MulExp.get(i).getInt();
                }
            }
        }
        return tmp;
    }
    public boolean isIntCon() {
        if(arr_MulExp.size()>1)
            return false;
        return arr_MulExp.get(arr_MulExp.size()-1).isIntCon();
    }
    public String getRparaType(){
        return arr_MulExp.get(0).getRparaType();
    }
}
