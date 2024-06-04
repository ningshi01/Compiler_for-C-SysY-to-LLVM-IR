package LLVM;

import MIPS.UniqueID;

import java.util.ArrayList;

public class BasicBlock extends Value{
    public ArrayList<Instruction> InstrList = new ArrayList<>();
    public Function fromFunc;
    public int BBid;
    public boolean flag_break = false;
    public boolean flag_continue = false;
    public String mips_name;

    public BasicBlock() {
        mips_name = UniqueID.getInstance().getUniqueID();
        UniqueID.getInstance().Count();
    }

    public String getName(){
        return "Label_"+mips_name;
    }
}
