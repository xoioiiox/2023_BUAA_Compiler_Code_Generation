package frontend.parser.expression;

import midend.symbol.SymbolTable;

public class PrimaryExp {
    private Exp exp;
    private LVal lVal;
    private Number number;

    public PrimaryExp(Exp exp) {
        this.exp = exp;
    }

    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
    }

    public PrimaryExp(Number number) {
        this.number = number;
    }

    public Exp getExp() {
        return exp;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Number getNumber() {
        return number;
    }

    public int getDimension() {
        if (exp != null) {
            return exp.getDimension();
        }
        else if (lVal != null) {
            return lVal.getDimension();
        }
        else {
            return 0; //int
        }
    }

    public int calculate(SymbolTable symbolTable) {
        if (this.exp != null) {
            return this.exp.calculate(symbolTable);
        }
        else if (this.lVal != null) {
            return this.lVal.calculate(symbolTable);
        }
        else if (this.number != null) {
            return Integer.parseInt(this.number.getIntConst().getVal());
        }
        return -1;
    }
}
