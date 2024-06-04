package CompUnit;

public class BlockItem {
    //BlockItem → Decl | Stmt
    public Decl para_Decl;
    public Stmt para_Stmt;

    public BlockItem(Decl para_Decl, Stmt para_Stmt) {
        this.para_Decl = para_Decl;
        this.para_Stmt = para_Stmt;
    }

    public BlockItem() {
    }
}
