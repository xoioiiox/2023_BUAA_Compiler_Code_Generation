package midend.llvmIr.type;

public class IrArrayType extends IrValueType {
    private IrValueType elementType; //元素的type
    private int elementNum;

    public IrArrayType(IrValueType irValueType, int elementNum) {
        this.elementType = irValueType;
        this.elementNum = elementNum;
    }

    @Override
    public String toString() {
        return "[" + elementNum + "x" + elementType.toString() + "]";
    }

}
