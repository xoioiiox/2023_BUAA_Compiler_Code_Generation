package midend.llvmIr;

import midend.llvmIr.type.IrValueType;

import java.util.ArrayList;

public class IrUser extends IrValue {
    private ArrayList<IrUse> operands;

    public IrUser(IrValueType irValueType) {
        super(irValueType);
        this.operands = new ArrayList<>();
    }

    public void setOperand(int pos, IrValue operand) {
        IrUse irUse = new IrUse(operand, this, pos);
        //不可以用set todo deal pos
        operands.add(irUse);
    }

    public IrValue getOperand(int pos) {
        if (pos < this.operands.size()) {
            return operands.get(pos).getValue();
        }
        else {
            return null;
        }
    }
}
