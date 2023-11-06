package frontend.parser.declaration;

import frontend.parser.expression.Exp;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class InitVal {
    private Exp exp;
    private ArrayList<InitVal> initVals;

    public InitVal() {
    }

    public InitVal(Exp exp) {
        this.exp = exp;
    }

    public InitVal(ArrayList<InitVal> initVals) {
        this.initVals = initVals;
    }

    public Exp getExp() {
        return exp;
    }

    public ArrayList<InitVal> getInitVals() {
        return initVals;
    }

    public int calculate(SymbolTable symbolTable) {
        return this.exp.calculate(symbolTable);
    }
}
