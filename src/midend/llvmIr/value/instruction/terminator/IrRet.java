package midend.llvmIr.value.instruction.terminator;

import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrRet extends IrInstruction {
    private boolean hasReturnExp;

    // 无返回值
    public IrRet(IrValueType irValueType, boolean hasReturnExp) {
        super(irValueType);
        this.hasReturnExp = hasReturnExp;
    }

    // 有返回值
    public IrRet(String name, IrValueType irValueType, boolean hasReturnExp) {
        super(irValueType);
        this.setName(name);
        this.hasReturnExp = hasReturnExp;
    }

    public boolean isHasReturnExp() {
        return hasReturnExp;
    }

    @Override
    public String toString() {
        return "ret " + this.getValueType() + (hasReturnExp? " " + this.getName() : "");
    }
}
