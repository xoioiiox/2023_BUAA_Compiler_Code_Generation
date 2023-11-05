package frontend.parser.statement;

import frontend.parser.expression.Cond;

public class StmtIf extends Stmt{
    private Cond cond;
    private Stmt stmtIf;
    private Stmt stmtElse;

    public StmtIf(Cond cond, Stmt stmtIf) {
        this.cond = cond;
        this.stmtIf = stmtIf;
        this.stmtElse = null;
    }

    public StmtIf(Cond cond, Stmt stmtIf, Stmt stmtElse) {
        this.cond = cond;
        this.stmtIf = stmtIf;
        this.stmtElse = stmtElse;
    }

    public Cond getCond() {
        return cond;
    }

    public Stmt getStmtIf() {
        return stmtIf;
    }

    public Stmt getStmtElse() {
        return stmtElse;
    }
}
