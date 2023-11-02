package frontend.parser.function;

import frontend.lexer.LexType;
import frontend.lexer.Token;

public class FuncType {
    private Token type;

    public FuncType(Token type) {
        this.type = type;
    }

    public int getType() {
        if (type.getLexType() == LexType.VOIDTK) {
            return -1;
        }
        else if (type.getLexType() == LexType.INTTK) {
            return 0;
        }
        return -2; //ERROR
    }
}
