package midend.llvmIr.value.instruction.memory;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.type.IrVoidType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrStore extends IrInstruction {
    private IrValue leftOp;
    private IrValue rightOp;

    public IrStore(IrValue leftOp, IrValue rightOp) {
        super(new IrIntType(32)); //todo
        this.leftOp = leftOp;
        this.rightOp = rightOp;
    }

    public IrValue getLeftOp() {
        return leftOp;
    }

    public IrValue getRightOp() {
        return rightOp;
    }

    /**
     * %1 = %2
     * store i32 %2, i32* %1
     */
    @Override
    public String toString() {
        return "store "
                + rightOp.getValueType().toString() + " " + rightOp.getName() + ", "
                + leftOp.getValueType().toString() + "* " + leftOp.getName();
    }
}
