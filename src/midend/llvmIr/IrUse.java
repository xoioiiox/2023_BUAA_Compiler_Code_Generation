package midend.llvmIr;

public class IrUse {
    private IrValue value;
    private IrUser user;
    //pos表示该value在该user的operandList中的位置
    private int pos;

    public IrUse(IrValue value, IrUser user, int pos) {
        this.value = value;
        this.user = user;
        this.pos = pos;
    }

    public IrValue getValue() {
        return value;
    }
}
