package frontend.parser.declaration;

import frontend.parser.expression.ConstExp;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class ConstInitVal {
    private ConstExp constExp;
    private ArrayList<ConstInitVal> constInitVals;

    public ConstInitVal() {}

    public ConstInitVal(ConstExp constExp) {
        this.constExp = constExp;
    }

    public ConstInitVal(ArrayList<ConstInitVal> constInitVals) {
        this.constInitVals = constInitVals;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public ArrayList<ConstInitVal> getConstInitVals() {
        return constInitVals;
    }

    public int calculate(SymbolTable symbolTable) {
        return this.constExp.calculate(symbolTable);
    }
}
