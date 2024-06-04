package CompUnit;

public class ConstExp {
    //ConstExp → AddExp
    public AddExp para_AddExp;

    public int RegisterID;
    public ConstExp(AddExp para_AddExp) {
        this.para_AddExp = para_AddExp;
        this.RegisterID = para_AddExp.RegisterID;
    }

    public ConstExp() {
    }
    public int getInt(){
        return para_AddExp.getInt();
    }
    public boolean isIntCon(){
        return para_AddExp.isIntCon();
    }
}
