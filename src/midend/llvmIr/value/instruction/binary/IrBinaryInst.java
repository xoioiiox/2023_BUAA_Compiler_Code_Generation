package midend.llvmIr.value.instruction.binary;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrBinaryInst extends IrInstruction {
    private IrBinaryType type;

    public IrBinaryInst(String name, IrValueType irValueType, IrBinaryType type, IrValue op1, IrValue op2) {
        super(irValueType);
        this.setOperand(0, op1);
        this.setOperand(1, op2);
        this.setName(name);
        this.type = type;
    }

    public IrBinaryType getType() {
        return type;
    }

    //%2 = add i32 1, 2
    @Override
    public String toString() {
        return this.getName() + " = " + this.type.toString() + " "
                + this.getValueType() + " "
                + this.getOperand(0).getName() + ", "
                + this.getOperand(1).getName();
    }
}
