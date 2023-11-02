package frontend.parser.expression;

import frontend.lexer.Token;

public class UnaryOp {
    Token op;
    public UnaryOp(Token op) {
        this.op = op;
    }

    public Token getOp() {
        return op;
    }
}
