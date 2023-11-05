package midend.llvmIr.value.instruction.cond;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrZext extends IrInstruction {
    private IrValue src;

    public IrZext(String name, IrValue src, IrValueType desType) {
        super(desType);
        this.setName(name);
        this.src = src;
    }

    //%32 = zext i1 %31 to i32
    @Override
    public String toString() {
        return this.getName() + " = zext i1 " + this.src.getName() + " to " + "i32";
    }
}
