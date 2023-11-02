package frontend.parser.expression;

import midend.symbol.SymbolTable;

public class ConstExp {
    private AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public int calculate(SymbolTable symbolTable) {
        return this.addExp.calculate(symbolTable);
    }
}
