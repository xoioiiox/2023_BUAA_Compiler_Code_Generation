package midend.llvmIr.value.constant;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;

public class IrConstant extends IrValue {

    public IrConstant(String name, IrValueType valueType) {
        super(name, valueType);
    }

    @Override
    public String toString() {
        return "#";
    }
}
