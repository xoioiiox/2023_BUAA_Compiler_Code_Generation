package midend.llvmIr.value.globalVariable;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;

public class IrGlobalVariable extends IrValue {
    private IrValue irConstant;
    private boolean isConst;

    public IrGlobalVariable(String name, IrValue irConstant, boolean isConst, IrValueType type) {
        super(name, type);
        this.irConstant = irConstant;
        this.isConst = isConst;
    }

    public IrValue getIrConstant() {
        return irConstant;
    }

    /**
     * int a = 5;
     * /@a  = dso_local global i32 5
     */
    @Override
    public String toString() {
        return this.getName() + " = dso_local "
                + ((isConst)? "constant": "global") + " "
                + this.getValueType().toString() + " "
                + this.irConstant.toString();
    }
}
