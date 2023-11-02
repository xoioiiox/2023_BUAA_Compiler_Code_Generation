package frontend.parser.declaration;

import frontend.lexer.Token;
import frontend.parser.expression.ConstExp;

import java.util.ArrayList;

public class ConstDef {
    private Token Ident;
    private ArrayList<ConstExp> constExps;
    private ConstInitVal constInitVal;

    public ConstDef(Token Ident, ArrayList<ConstExp> constExps, ConstInitVal constInitVal) {
        this.Ident = Ident;
        this.constExps = constExps;
        this.constInitVal = constInitVal;
    }

    public int getDimension() {
        return this.constExps.size();
    }

    public Token getIdent() {
        return Ident;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }
}
