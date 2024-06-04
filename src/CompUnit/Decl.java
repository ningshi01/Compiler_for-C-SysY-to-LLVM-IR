package CompUnit;

public class Decl {
    public ConstDecl para_ConstDecl = new ConstDecl();
    public VarDecl para_VarDecl = new VarDecl();

    public Decl(ConstDecl para_ConstDecl, VarDecl para_VarDecl) {
        this.para_ConstDecl = para_ConstDecl;
        this.para_VarDecl = para_VarDecl;
    }

    public Decl() {
    }
}
