package frontend.parser.expression;

import midend.symbol.SymbolTable;

public class Exp {
    private AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public int getDimension() {
        return this.addExp.getDimension();
    }

    public int calculate(SymbolTable symbolTable) {
        return this.addExp.calculate(symbolTable);
    }
}
