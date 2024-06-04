package LLVM;

import java.util.ArrayList;

public class Value {
    public int registerID = -1;//寄存器ID
    public int returnValue;//返回值
    public ArrayList<Integer> selfLevels = new ArrayList<>();//数组维数
    public ArrayList<Integer> returnNums = new ArrayList<>();//返回数组值
    public boolean boolValue;//条件式的布尔返回值
    public boolean Array_Const = false;
    public int ToVar_level;//参数存储时的层数，仅在层数大于它时才可以被认定为已存储
    public int ToVar_level_id;//相同层数时的分配id
    public int In_ToVar_registerID;//参数存储的寄存器号
    public boolean flag_ToVar = false;//指参数被存储到了一个寄存器中
    public ReturnType returnType;//返回类型
    public enum ReturnType {
        VOID,
        INT
    }
    public SelfType selfType;//自身类型
    public enum SelfType {
        CONST,//常量
        VAR,//变量
        ARRAY,//数组
        FUNC,//函数
        PARA,//参数
        PARA_ARRAY//参数-数组
    }
}
