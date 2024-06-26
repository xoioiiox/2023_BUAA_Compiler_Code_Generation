package midend.llvmIr.type;

public class IrPointerType extends IrValueType {
    private IrValueType innerType;

    public IrPointerType(IrValueType innerType) {
        this.innerType = innerType;
    }

    public IrValueType getInnerType() {
        return innerType;
    }

    @Override
    public String toString() {
        return innerType.toString() + "*";
    }
}
