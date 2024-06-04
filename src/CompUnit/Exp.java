package CompUnit;

import Symbol.Symbol;

public class Exp {
    //Exp → AddExp
    public AddExp para_AddExp;

    public Symbol Rpara;//标识是否是常量0/一维1/二维2
    public int RegisterID;
    public Exp(AddExp para_AddExp,Symbol Rpara) {
        this.para_AddExp = para_AddExp;
        this.Rpara = Rpara;
        this.RegisterID = para_AddExp.RegisterID;
    }

    public Exp() {
    }
    public String getInt_h(){
        if(isIntCon())
            return ""+para_AddExp.getInt();
        else
            return "%"+RegisterID;
    }
    public int getInt(){
        return para_AddExp.getInt();
    }
    public boolean isIntCon(){
        return para_AddExp.isIntCon();
    }
    public String getRparaType(){
        return para_AddExp.getRparaType();
    }
}
