package midend.llvmIr.value.instruction.cond;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrIcmp extends IrInstruction {
    private IrIcmpType type;

    public IrIcmp(String name, IrIcmpType type, IrValueType valueType, IrValue op1, IrValue op2) {
        super(valueType);
        this.setName(name);
        this.setOperand(0, op1);
        this.setOperand(1, op2);
        this.type = type;
    }

    public IrIcmpType getType() {
        return type;
    }

    @Override
    public String toString() {
        return this.getName() + " = icmp "
                + this.type.toString() + " "
                + this.getValueType().toString() + " "
                + this.getOperand(0).getName() + ", "
                + this.getOperand(1).getName();
    }
}
