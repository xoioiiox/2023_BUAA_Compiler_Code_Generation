package midend.symbol;

import midend.llvmIr.IrValue;

public class Symbol {
    private String name;
    private int lineNum;
    private IrValue value;
    private boolean isUsed;

    //语法分析
    public Symbol(String name, int lineNum) {
        this.name = name;
        this.lineNum = lineNum;
    }

    //中间代码生成
    public Symbol(String name, IrValue value) {
        this.name = name;
        this.value = value;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public IrValue getValue() {
        return value;
    }

    public void setValue(IrValue value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getLineNum() {
        return lineNum;
    }
}
