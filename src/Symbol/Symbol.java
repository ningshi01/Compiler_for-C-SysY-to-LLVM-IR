package Symbol;

import LLVM.Value;

import java.util.ArrayList;

public class Symbol extends Value {//符号基类
    public boolean hasDone=false;

    public String Name;//名字

    /*种类 Type
    0-常量
    1-变量
    2-数组
    3-函数/过程
    4-参数
     */
    public int Type;

    /*类型 Pattern
    0-int
    1-void
     */
    public int Pattern;

    public int RegisterID=-1;//局部变量寄存器号
    public Symbol(String name){
        Name = name;
    }
    public Symbol(String name, int type, int pattern) {
        Name = name;
        Type = type;
        Pattern = pattern;
        hasDone = true;
    }
    public Symbol(String name, Value value){
        this.Name = name;
        this.registerID = value.registerID;
        this.returnValue = value.returnValue;
        this.selfLevels = value.selfLevels;
        this.returnNums = value.returnNums;
        this.returnType = value.returnType;
        this.selfType = value.selfType;
        this.Array_Const = value.Array_Const;
        this.hasDone = true;
    }

    public void print(){
        String print_name=Name,print_type="null",print_return="null",print_value="null",print_RID="null";
        switch(selfType){
            case CONST:
                print_type = "常量";
                print_value=""+returnValue;
                break;
            case VAR:
                print_type = "变量";
                print_value=""+returnValue;
                break;
            case ARRAY:
                print_type = "数组";
                for(int i=0;i<selfLevels.size();i++){
                    print_name+="["+selfLevels.get(i)+"]";
                }
                if(returnNums.size()!=0){
                    print_value = "[" + returnNums.get(0);
                    for (int i = 1; i < returnNums.size(); i++) {
                        print_value += "," + returnNums.get(i);
                        if (i > 19) {//太多了处理一下下
                            print_value += "......";
                            break;
                        }
                    }
                    print_value += "]";
                }else{
                    print_value = "[null]";
                }
                break;
            case FUNC:
                print_type = "函数";
                break;
            case PARA:
                print_type = "参数";
                break;
            case PARA_ARRAY:
                print_type = "参数-数组";
                print_name += "[]";
                for(int i=1;i<selfLevels.size();i++){
                    print_name+="["+selfLevels.get(i)+"]";
                }
                print_value = "["+"形参"+"]";
                break;
        }
        switch (returnType){
            case INT:
                print_return = "int";
                break;
            case VOID:
                print_return = "void";
                break;
        }
        switch (registerID){
            case -1:
                print_RID = "@"+Name;
                break;
            default:
                print_RID="%"+registerID;
        }
        System.out.println(
                   "<"+"Name:"+print_name+
                "    "+"Type:"+print_type+
                "    "+"Return:"+print_return+
                "    "+"Value:"+print_value+
                "    "+"Register:"+print_RID+
                   ">");
    }
    /*
    public void print(){
        String print_type="null",print_pattern="null",print_value="null",print_RID="%"+RegisterID;
        if(RegisterID==-1){
            print_RID = "@"+Name;
        }
        switch (Type){
            case 0:
                print_type = "常量";
                VarSymbol tmp1 = (VarSymbol) this;
                print_value=""+tmp1.var_num;
                break;
            case 1:
                print_type = "变量";
                VarSymbol tmp2 = (VarSymbol) this;
                print_value=""+tmp2.var_num;
                break;
            case 2:
                print_type = "数组";
                break;
            case 3:
                print_type = "函数";
                break;
            case 4:
                print_type = "参数";
                break;
        }
        switch (Pattern){
            case 0:
                print_pattern = "int";
                break;
            case 1:
                print_pattern = "void";
                break;
        }
        System.out.println("<"
                      +"Name:"+Name+
                "    "+"Type:"+print_type+
                "    "+"Pattern:"+print_pattern+
                "    "+"Value:"+print_value+
                "    "+"Register:"+print_RID+
                ">");
    }
    */
    public String get_array_type_all(){
        ArrayList<String> array_types = new ArrayList<>();
        for(Integer i:selfLevels){//初始化出一样的空字符串个数
            array_types.add("");
        }array_types.add("i32");
        for(int i=selfLevels.size()-1;i>=0;i--){
            if(i==selfLevels.size()-1){
                array_types.set(i,"["+selfLevels.get(i)+" x "+"i32]");
            }else{
                array_types.set(i,"["+selfLevels.get(i)+" x "+array_types.get(i+1)+"]");
            }
        }
        return array_types.get(0);
    }
    public String get_array_type(){
        ArrayList<String> array_types = new ArrayList<>();
        for(Integer i:selfLevels){//初始化出一样的空字符串个数
            array_types.add("");
        }array_types.add("i32");
        for(int i=selfLevels.size()-1;i>=0;i--){
            if(i==selfLevels.size()-1){
                array_types.set(i,"["+selfLevels.get(i)+" x "+"i32]");
            }else{
                array_types.set(i,"["+selfLevels.get(i)+" x "+array_types.get(i+1)+"]");
            }
        }
        String array_type="";
        array_type = array_types.get(1)+"*";
        return array_type;
    }

    public String get_array_type_noStar(){
        ArrayList<String> array_types = new ArrayList<>();
        for(Integer i:selfLevels){//初始化出一样的空字符串个数
            array_types.add("");
        }array_types.add("i32");
        for(int i=selfLevels.size()-1;i>=0;i--){
            if(i==selfLevels.size()-1){
                array_types.set(i,"["+selfLevels.get(i)+" x "+"i32]");
            }else{
                array_types.set(i,"["+selfLevels.get(i)+" x "+array_types.get(i+1)+"]");
            }
        }
        String array_type="";
        array_type = array_types.get(1);
        return array_type;
    }
    public String get_array_type_former(){
        ArrayList<String> array_types = new ArrayList<>();
        for(Integer i:selfLevels){//初始化出一样的空字符串个数
            array_types.add("");
        }array_types.add("i32");
        for(int i=selfLevels.size()-1;i>=0;i--){
            if(i==selfLevels.size()-1){
                array_types.set(i,"["+selfLevels.get(i)+" x "+"i32]");
            }else{
                array_types.set(i,"["+selfLevels.get(i)+" x "+array_types.get(i+1)+"]");
            }
        }
        String array_type="";
        array_type = array_types.get(2)+"*";
        return array_type;
    }
    public String get_array_type_former_noStar(){
        ArrayList<String> array_types = new ArrayList<>();
        for(Integer i:selfLevels){//初始化出一样的空字符串个数
            array_types.add("");
        }array_types.add("i32");
        for(int i=selfLevels.size()-1;i>=0;i--){
            if(i==selfLevels.size()-1){
                array_types.set(i,"["+selfLevels.get(i)+" x "+"i32]");
            }else{
                array_types.set(i,"["+selfLevels.get(i)+" x "+array_types.get(i+1)+"]");
            }
        }
        String array_type="";
        array_type = array_types.get(2);
        return array_type;
    }
    int times=0;//遍历器
    public boolean IsToVar(int Now_level,int Now_levelId){
        if(flag_ToVar){
            if (Now_level>ToVar_level){
                return true;
            }
            if(Now_level == ToVar_level && Now_levelId == ToVar_level_id);{
                return true;
            }
        }
        return false;
    }
    public String help_Array(ArrayList<String> array_types,ArrayList<Integer> selfLevels,int i){
        String array_value="";
        if(i==array_types.size()-1){
            return array_types.get(i)+" "+returnNums.get(times++);
        } else {
            if(i==0)
                array_value+="[";
            if(i>0)
                array_value+=array_types.get(i)+" ";
            if(i+1==array_types.size()-1 && i!=0)
                array_value+="[";
            for(int j=0;j<selfLevels.get(i);j++){
                if(j>0)
                    array_value+=", ";
                array_value+=help_Array(array_types,selfLevels,i+1);
            }
            if(i+1==array_types.size()-1 && i!=0)
                array_value+="]";
            if(i==0)
                array_value+="]";
            return array_value;
        }
    }
    @Override
    public String toString() {
        String self_type;
        switch (selfType) {
            case VAR:
                self_type = "global";
                break;
            case CONST:
                self_type = "constant";
                break;
            case ARRAY:
                if(Array_Const)
                    self_type = "constant";
                else
                    self_type = "global";
                break;
            default:
                self_type = "null";
                break;
        }
        String return_type;
        switch (returnType){
            case INT :
                return_type = "i32";
                break;
            default:
                return_type = "null";
                break;
        }
        if(selfType == SelfType.ARRAY){
            ArrayList<String> array_types = new ArrayList<>();
            for(Integer i:selfLevels){//初始化出一样的空字符串个数
                array_types.add("");
            }array_types.add("i32");
            for(int i=selfLevels.size()-1;i>=0;i--){
                if(i==selfLevels.size()-1){
                    array_types.set(i,"["+selfLevels.get(i)+" x "+"i32]");
                }else{
                    array_types.set(i,"["+selfLevels.get(i)+" x "+array_types.get(i+1)+"]");
                }
            }
            String array_type,array_value;
            array_type = array_types.get(0);
            if(returnNums.size()!=0)
                array_value = help_Array(array_types,selfLevels,0);
            else
                array_value = "zeroinitializer";
            return "@"+Name+" = dso_local "+self_type+" "+array_type+" "+array_value+"\n";
        }
        return "@"+Name+" = dso_local "+self_type+" "+return_type+" "+returnValue+"\n";
    }
}
