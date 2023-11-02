package frontend.parser.expression;

import frontend.lexer.Token;

public class Number {
    private Token intConst;

    public Number(Token intConst) {
        this.intConst = intConst;
    }

    public Token getIntConst() {
        return intConst;
    }
}
