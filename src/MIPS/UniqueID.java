package MIPS;

public class UniqueID {
    private static UniqueID instance = new UniqueID();
    public static UniqueID getInstance(){
        return instance;
    }
    public int id=1;
    public String getUniqueID(){
        return "unique_"+id;
    }
    public void Count(){
        id++;
    }
}
