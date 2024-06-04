package LLVM;

import Symbol.Symbol;

import java.util.ArrayList;

public class Function extends Value{
    public String name;
    public ArrayList<Symbol> paraList = new ArrayList<>();
    public ArrayList<BasicBlock> BblockList = new ArrayList<>();
    public ReturnType retType;
    public boolean hasReturn = false;//用来标记是否拥有return语句
    public boolean IsBrLast(int indexId){
        if(this.findBblock(indexId).InstrList.size()==0)
            return false;
        if(this.findBblock(indexId).InstrList.get(this.findBblock(indexId).InstrList.size()-1).instrType!= Instruction.InstrType.BR)
            return false;
        return true;
    }
    public BasicBlock findBeforeBblock(int indexId){
        for(int i=0;i<BblockList.size();i++){
            if(BblockList.get(i).BBid == indexId){
                return BblockList.get(i-1);
            }
        }
        return null;
    }
    public BasicBlock findBblock(int indexId){
        for(int i=0;i<BblockList.size();i++){
            if(BblockList.get(i).BBid == indexId){
                return BblockList.get(i);
            }
        }
        return null;
    }
    public BasicBlock findNextBblock(int indexId){
        for(int i=0;i<BblockList.size();i++){
            if(BblockList.get(i).BBid == indexId){
                return BblockList.get(i+1);
            }
        }
        return null;
    }
    public void findAndToBr(int indexId,Instruction br){
        for(BasicBlock b:BblockList){
            if(b.BBid == indexId){
                b.InstrList.add(br);
            }
        }
    }
    @Override
    public String toString() {
        String ret_Type;
        switch (retType) {
            case INT :
                ret_Type = "i32";
                break;
            default:
                ret_Type = "void";
                break;
        }
        //Para_list
        String para_list="";
        for(int i=0;i<paraList.size();i++){
            switch (paraList.get(i).selfType){
                case PARA:
                    para_list+="i32 ";
                    break;
                case PARA_ARRAY:
                    para_list+=paraList.get(i).get_array_type()+" ";
                    break;
            }
            para_list+="%"+paraList.get(i).registerID;
            if(i!=paraList.size()-1){
                para_list+=", ";
            }
        }
        //BasicBlock
        String Bblock_list="";
        //假如是void函数且无return语句(检查最后一条)
        if(retType==ReturnType.VOID){
            if(BblockList.get(BblockList.size()-1).InstrList.size()==0){//最后一个块为空块
                Instruction in = new Instruction();
                {
                    in.parentBB = BblockList.get(BblockList.size() - 1);
                    in.instrType = Instruction.InstrType.RET;
                    in.result_Rid = -1;
                }
                BblockList.get(BblockList.size() - 1).InstrList.add(in);
            }
            else if(BblockList.get(BblockList.size()-1).InstrList.get(BblockList.get(BblockList.size()-1).InstrList.size()-1).instrType == Instruction.InstrType.RET){}
            else {//最后一个块的最后没有ret
                Instruction in = new Instruction();
                {
                    in.parentBB = BblockList.get(BblockList.size() - 1);
                    in.instrType = Instruction.InstrType.RET;
                    in.result_Rid = -1;
                }
                BblockList.get(BblockList.size() - 1).InstrList.add(in);
            }
        }
        for(BasicBlock Bblock : BblockList){
            Bblock_list+=Bblock.BBid+":";
            for (Instruction i : Bblock.InstrList){
                Bblock_list+="\n"+"  "+i.toString();
                if(i.instrType== Instruction.InstrType.RET)
                    break;
            }Bblock_list+="\n";
        }
        return "define dso_local "+ret_Type+" @"+name+"("+para_list+") {\n" +
                    Bblock_list+
                "}\n";
    }
}
