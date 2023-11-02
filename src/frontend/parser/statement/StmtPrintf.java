package frontend.parser.statement;

import frontend.lexer.Token;
import frontend.parser.expression.Exp;

import java.util.ArrayList;

public class StmtPrintf extends Stmt {
    private Token formatString;
    private ArrayList<Exp> exps;

    public StmtPrintf(Token formatString, ArrayList<Exp> exps) {
        this.formatString = formatString;
        this.exps = exps;
    }

    public Token getFormatString() {
        return formatString;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }
}
