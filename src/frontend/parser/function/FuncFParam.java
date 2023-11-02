package frontend.parser.function;

import frontend.lexer.Token;
import frontend.parser.declaration.Btype;
import frontend.parser.expression.ConstExp;

public class FuncFParam {
    private Btype btype;
    private Token Ident;
    private ConstExp constExp;
    private int dimension;

    public FuncFParam(Btype btype, Token Ident, ConstExp constExp, int dimension) {
        this.btype = btype;
        this.Ident = Ident;
        this.constExp = constExp;
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public Token getIdent() {
        return Ident;
    }
}
