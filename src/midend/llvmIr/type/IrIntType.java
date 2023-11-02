package midend.llvmIr.type;

public class IrIntType extends IrValueType {
    private int bits;

    public IrIntType(int bits) {
        this.bits = bits;
    }

    @Override
    public String toString() {
        return "i" + bits;
    }
}
