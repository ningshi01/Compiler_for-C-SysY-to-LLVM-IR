package Token;

public class Token {
    public String TData;
    public Init.tokenType type;
    public int lineNum;
    public int FormatNum;//printf格式字符个数
    public Token(String TData, Init.tokenType type, int lineNum) {
        this.TData = TData;
        this.type = type;
        this.lineNum = lineNum;
    }

    public Token() {

    }
}
