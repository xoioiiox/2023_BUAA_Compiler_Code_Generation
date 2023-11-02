package frontend.parser.declaration;

import frontend.lexer.Token;
import frontend.parser.expression.ConstExp;

import java.util.ArrayList;

public class VarDef {
    private Token Ident;
    private ArrayList<ConstExp> constExps;
    private InitVal initVal;

    public VarDef(Token Ident, ArrayList<ConstExp> constExps, InitVal initVal) {
        this.Ident = Ident;
        this.constExps = constExps;
        this.initVal = initVal;
    }

    public Token getIdent() {
        return Ident;
    }

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    public InitVal getInitVal() {
        return initVal;
    }

    public int getDimension() {
        return this.constExps.size();
    }
}
