package midend.llvmIr.value.globalVariable;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;

public class IrGlobalVariable extends IrValue {
    private IrValue value;
    private boolean isConst;

    public IrGlobalVariable(String name, IrValue value, boolean isConst, IrValueType type) {
        super(name, type);
        this.value = value;
        this.isConst = isConst;
    }

    public IrValue getValue() {
        return value;
    }

    /**
     * int a = 5;
     * /@a  = dso_local global i32 5
     */
    @Override
    public String toString() {
        return this.getName() + " = dso_local "
                + ((isConst)? "constant": "global") + " "
                //+ this.getValueType().toString() + " "
                + this.value.toString();
    }
}
