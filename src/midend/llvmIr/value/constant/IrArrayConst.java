package midend.llvmIr.value.constant;

import midend.llvmIr.type.IrValueType;

import java.util.ArrayList;

public class IrArrayConst extends IrConstant{
    public IrArrayConst(String name, IrValueType valueType) {
        super(name, valueType);
    }
}
