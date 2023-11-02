package frontend.parser.statement;

import frontend.parser.expression.Exp;

public class StmtExp extends Stmt {
    private Exp exp;

    public StmtExp(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }
}
