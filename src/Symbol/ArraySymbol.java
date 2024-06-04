package Symbol;

import LLVM.Value;

import java.util.ArrayList;

public class ArraySymbol extends Symbol{//派生类-数组
    //维数
    public int w_num;
    //数组元素类型
    public int theType;
    //数组每维元素个数
    public int[] nums;
    //数组每维元素的值
    public int[] num_vals;
    /*flag-const
    0-const
    1-int
     */
    public int IsConst;
    public ArraySymbol(String name, int w_num, int theType, int[] nums, int IsConst) {
        super(name, 2, 0);
        this.w_num = w_num;
        this.theType = theType;
        this.nums = nums;
        this.IsConst = IsConst;
    }

    public ArraySymbol(String name, Value value) {
        super(name, value);
    }
}
