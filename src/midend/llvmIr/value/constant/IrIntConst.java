package midend.llvmIr.value.constant;

import midend.llvmIr.type.IrValueType;

public class IrIntConst extends IrConstant{
    private int val;

    public IrIntConst(String name, IrValueType irValueType, int val) {
        super(name, irValueType);
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    @Override
    public String toString() {
        return this.getValueType().toString() + " " + this.val;
    }
}
