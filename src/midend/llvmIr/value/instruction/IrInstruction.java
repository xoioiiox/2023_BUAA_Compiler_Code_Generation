package midend.llvmIr.value.instruction;

import midend.llvmIr.IrUser;
import midend.llvmIr.type.IrValueType;

public class IrInstruction extends IrUser {

    public IrInstruction(IrValueType irValueType) {
        super(irValueType);
    }
    //todo name?
}
