package Token;

import java.util.ArrayList;

public class Errors {
    public int LineHeight;
    public String type;

    @Override
    public String toString() {
        return LineHeight +type;
    }

    public Errors(int lineHeight, String type) {
        this.LineHeight = lineHeight;
        this.type = type;
    }

    public static void _a(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," a"));
    }
    public static void _b(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," b"));
    }
    public static void _c(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," c"));
    }
    public static void _d(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," d"));
    }
    public static void _e(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," e"));
    }
    public static void _f(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," f"));
    }
    public static void _g(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," g"));
    }
    public static void _h(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," h"));
    }
    public static void _i(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," i"));
    }
    public static void _j(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," j"));
    }
    public static void _k(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," k"));
    }
    public static void _l(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," l"));
    }
    public static void _m(ArrayList<Errors> arr_Errors, int LineHeight){
        arr_Errors.add(new Errors(LineHeight," m"));
    }
}
