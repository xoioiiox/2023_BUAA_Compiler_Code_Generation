package midend.llvmIr.value.instruction.memory;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrAlloca extends IrInstruction {
    private IrValue value;

    public IrAlloca(IrValueType irValueType, IrValue value) {
        super(irValueType);
        this.setName(value.getName());
        this.value = value;
    }

    public IrValue getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value.getName() + " = alloca " + this.value.getValueType();
    }
}
