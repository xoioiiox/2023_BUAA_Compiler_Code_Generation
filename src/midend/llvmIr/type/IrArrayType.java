package midend.llvmIr.type;

public class IrArrayType extends IrValueType {
    private IrValueType elementType; //元素的type 涉及递归？
    private int elementNum;
    private int elementNum1;
    private int elementNum2;
    private int dim;

    public IrArrayType(IrValueType irValueType, int elementNum) {
        this.elementType = irValueType;
        this.elementNum = elementNum;
        this.dim = 1;
    }

    public IrArrayType(IrValueType irValueType, int elementNum1, int elementNum2) {
        this.elementType = irValueType;
        this.elementNum1 = elementNum1;
        this.elementNum2 = elementNum2;
        this.dim = 2;
    }

    public IrValueType getElementType() {
        return elementType;
    }

    public int getDim() {
        return dim;
    }

    public int getElementNum() {
        return elementNum;
    }

    public int getElementNum1() {
        return elementNum1;
    }

    public int getElementNum2() {
        return elementNum2;
    }

    @Override
    public String toString() {
        if (this.dim == 1) {
            return "[" + elementNum + " x " + elementType.toString() + "]";
        }
        else if (this.dim == 2) {
            return "[" + elementNum1 + " x " + elementType.toString() + "]";
        }
        return "#";
    }

}
