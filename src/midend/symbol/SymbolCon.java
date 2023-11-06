package midend.symbol;

import midend.llvmIr.IrValue;
import midend.llvmIr.value.constant.IrConstant;

import java.util.ArrayList;

public class SymbolCon extends Symbol{
    private int dimension;
    private int initVal;
    private ArrayList<IrConstant> initValArray;

    public SymbolCon(String name, int lineNum, int dimension) {
        super(name, lineNum);
        this.dimension = dimension;
    }

    public SymbolCon(String name, int dimension, IrValue value) {
        super(name, value);
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public void setInitVal(int initVal) {
        this.initVal = initVal;
    }

    public void setInitValArray(ArrayList<IrConstant> initValArray) {
        this.initValArray = initValArray;
    }

    public int getInitVal() {
        return initVal;
    }
}
