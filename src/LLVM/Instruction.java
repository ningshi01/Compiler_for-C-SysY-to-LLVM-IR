package LLVM;

public class Instruction extends Value{
    public enum InstrType {
        HELP,
        RET,//ret <type> <value> ,ret void
        CALL,//<result> = call [ret attrs] <ty> <fnptrval>(<function args>)
        PUTCH,
        PUTINT,
        ADD,//<result> = add <ty> <op1>, <op2>
        SUB,//<result> = sub <ty> <op1>, <op2>
        MUL,//<result> = mul <ty> <op1>, <op2>
        SDIV,//<result> = sdiv <ty> <op1>, <op2>
        SREM,
        ALLOCA,//<result> = alloca <type>
        LOAD,//	<result> = load <ty>, <ty>* <pointer>
        STORE,//store <ty> <value>, <ty>* <pointer>
        BR,//br label <dest>
        BRI1,//br i1 <cond>, label <iftrue>, label <iffalse>
        /*<result> = icmp <cond> <ty> <op1>, <op2>
        *
            eq: ==
            ne: !=
            sgt: >
            sge: >=
            slt: <
            sle: <=
        *
        */
        ICMP_EQ,
        ICMP_NE,
        ICMP_SGT,
        ICMP_SGE,
        ICMP_SLT,
        ICMP_SLE,
        ZEXT,
        GETELEMENTPTR,
    }
    public int result_Rid;
    public String result;
    public InstrType instrType;
    public String operation1;
    public String operation2;
    public String operation3;
    public String operation4;
    public String operation5=null;
    public BasicBlock parentBB;

    @Override
    public String toString() {
        String instr = "";
        switch (instrType){
            case HELP:
                instr = ";help-->" + result;
                break;
            case RET:
                if(result_Rid==-1)//代表return void
                    instr = "ret void";
                else
                    instr = "ret i32 "+result;
                break;
            case PUTCH:
                instr = "call void @putch(i32 "+result+")";
                break;
            case PUTINT:
                instr = "call void @putint(i32 "+result+")";
                break;
            case CALL:
                instr = result + operation1 + operation2;
                break;
            case ADD:
                instr = result +" = add i32 " +operation1+ ", "+operation2;
                break;
            case SUB:
                instr = result +" = sub i32 " +operation1+ ", "+operation2;
                break;
            case MUL:
                instr = result +" = mul i32 " +operation1+ ", "+operation2;
                break;
            case SDIV:
                instr = result +" = sdiv i32 " +operation1+ ", "+operation2;
                break;
            case SREM:
                instr = result +" = srem i32 " +operation1+ ", "+operation2;
                break;
            case LOAD:
                if(operation3==null)
                    instr = result +" = load i32, i32* "+operation2;
                else
                    instr = result +" = load "+operation3+", "+operation4+" "+operation2;
                break;
            case ALLOCA:
                instr = result +" = alloca "+operation1;
                break;
            case STORE:
                if(operation3==null)
                    instr = "store i32 "+operation1+", i32* "+operation2;
                else
                    instr = "store "+operation3+" "+operation1+", "+operation4+" "+operation2;
                break;
            case BR:
                instr = "br label "+result;
                break;
            case BRI1:
                instr = "br i1 "+result+", label "+operation1+", label "+operation2;
                break;
            case ICMP_EQ:
                instr = result + " = icmp eq i32 "+operation1+", "+operation2;
                break;
            case ICMP_NE:
                instr = result + " = icmp ne i32 "+operation1+", "+operation2;
                break;
            case ICMP_SGT:
                instr = result + " = icmp sgt i32 "+operation1+", "+operation2;
                break;
            case ICMP_SGE:
                instr = result + " = icmp sge i32 "+operation1+", "+operation2;
                break;
            case ICMP_SLT:
                instr = result + " = icmp slt i32 "+operation1+", "+operation2;
                break;
            case ICMP_SLE:
                instr = result + " = icmp sle i32 "+operation1+", "+operation2;
                break;
            case ZEXT:
                instr = result+" = zext i1 "+operation1+" to i32";
                break;
            case GETELEMENTPTR:
                if(operation4!=null && operation5==null)
                    instr = result+" = getelementptr "+operation1+", "+operation1+"* "+operation2+", i32 0, i32 "+operation3+", i32 "+operation4;
                else if(operation4!=null && operation5!=null)
                    instr = result+" = getelementptr "+operation1+", "+operation1+"* "+operation2+", i32 "+operation3+", i32 "+operation4;
                else if(operation4==null && operation5==null)
                    instr = result+" = getelementptr "+operation1+", "+operation1+"* "+operation2+", i32 0, i32 "+operation3;
                else if(operation4==null && operation5!=null)
                    instr = result+" = getelementptr "+operation1+", "+operation1+"* "+operation2+", i32 "+operation3;
                break;
        }
        return instr;
    }
}
