package midend.symbol;

import frontend.parser.declaration.ConstInitVal;

public class SymbolCon extends Symbol{
    private int dimension;
    private int initVal;

    public SymbolCon(String name, int lineNum, int dimension) {
        super(name, lineNum);
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public void setInitVal(int initVal) {
        this.initVal = initVal;
    }

    public int getInitVal() {
        return initVal;
    }
}
