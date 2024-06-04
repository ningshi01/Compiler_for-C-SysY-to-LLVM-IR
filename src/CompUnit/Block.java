package CompUnit;

import Token.Token;

import java.util.ArrayList;

public class Block {
    //Block → '{' { BlockItem } '}'
    Token para_LBRACE;
    public ArrayList<BlockItem> arr_BlockItem = new ArrayList<>();
    Token para_RBRACE;

    public Block(Token para_LBRACE, ArrayList<BlockItem> arr_BlockItem, Token para_RBRACE) {
        this.para_LBRACE = para_LBRACE;
        this.arr_BlockItem = arr_BlockItem;
        this.para_RBRACE = para_RBRACE;
    }

    public Block() {
    }
}
