package frontend.parser.statement;

import frontend.parser.expression.Exp;
import frontend.parser.expression.LVal;

public class StmtAssign extends Stmt {
    private LVal lVal;
    private Exp exp;

    public StmtAssign(LVal lVal, Exp exp) {
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
