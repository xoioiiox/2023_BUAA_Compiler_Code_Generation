package frontend.parser.statement;

import frontend.parser.expression.Exp;
import frontend.parser.expression.LVal;

public class ForStmt {
    private LVal lVal;
    private Exp exp;

    public  ForStmt(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

    public LVal getlVal() {
        return lVal;
    }
}
