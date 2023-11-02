package frontend.parser.statement;

import frontend.parser.expression.LVal;

public class StmtGetInt extends Stmt {
    private LVal lVal;

    public StmtGetInt(LVal lVal) {
        this.lVal = lVal;
    }

    public LVal getlVal() {
        return lVal;
    }
}
