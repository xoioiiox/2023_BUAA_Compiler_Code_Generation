package midend.llvmIr.value.instruction.cond;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.value.instruction.IrInstruction;

public class IrBr extends IrInstruction {
    private IrValue cond;
    private String trueLabel;
    private String falseLabel;
    private String label;

    public IrBr(IrValue cond, String trueLabel, String falseLabel) {
        super(new IrIntType(1)); //todo
        this.cond = cond;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    public IrBr(String label) {
        super(new IrIntType(1)); //todo
        this.label = label;
    }

    public void setFalseLabel(String falseLabel) {
        this.falseLabel = falseLabel;
    }

    public void setTrueLabel(String trueLabel) {
        this.trueLabel = trueLabel;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    //br i1 %10, label %11, label %13
    @Override
    public String toString() {
        if (label != null) {
            return "br label %" + label;
        }
        else {
            return "br " + this.getValueType() + " "
                    + this.cond.getName() + ", label %"
                    + trueLabel + ", label %"
                    + falseLabel;
        }
    }
}
