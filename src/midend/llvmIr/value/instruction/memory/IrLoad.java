package midend.llvmIr.value.instruction.memory;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrLoad extends IrInstruction {
    private IrValue value;

    public IrLoad(IrValueType irValueType, IrValue value) {
        super(irValueType);
        this.value = value;
    }

    public IrValue getValue() {
        return value;
    }

    //%7 = load i32, i32* %1
    @Override
    public String toString() {
        return this.getName() + " = load "
                + this.getValueType().toString() + ", "
                + this.getValueType().toString() + "* "
                + this.value.getName();
    }
}
