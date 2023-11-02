package frontend.parser.statement;

import frontend.parser.expression.Exp;
import frontend.parser.expression.LVal;

public class ForStmt {
    LVal lVal;
    Exp exp;

    public  ForStmt(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }
}
