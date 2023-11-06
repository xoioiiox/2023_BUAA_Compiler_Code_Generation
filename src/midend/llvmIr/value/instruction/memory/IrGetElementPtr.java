package midend.llvmIr.value.instruction.memory;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

import java.util.ArrayList;

public class IrGetElementPtr extends IrInstruction {
    private IrValueType baseType;
    private IrValue ptrVal;
    private IrValueType indexType;
    private IrValue index;
    private IrValue index1;
    private IrValue index2;
    private boolean isFParam;

    public IrGetElementPtr(IrValueType irValueType, String name,
                           IrValueType baseType, IrValue ptrVal,
                           IrValueType indexType, IrValue index, boolean isFParam) {
        super(irValueType);
        this.setName(name);
        this.baseType = baseType;
        this.ptrVal = ptrVal;
        this.indexType = indexType;
        this.index = index;
        this.isFParam = isFParam;
    }

    public IrGetElementPtr(IrValueType irValueType, String name,
                           IrValueType baseType, IrValue ptrVal,
                           IrValueType indexType, IrValue index1, IrValue index2, boolean isFParam) {
        super(irValueType);
        this.setName(name);
        this.baseType = baseType;
        this.ptrVal = ptrVal;
        this.indexType = indexType;
        this.index1 = index1;
        this.index2 = index2;
        this.isFParam = isFParam;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName());
        sb.append(" = getelementptr ");
        if (!isFParam) {
            sb.append(this.baseType.toString());
            sb.append(", ");
            sb.append(this.baseType.toString());
            sb.append("* ");
            sb.append(this.ptrVal.getName());
            sb.append(", i32 0");
        }
        else {
            sb.append(this.baseType.toString());
            sb.deleteCharAt(sb.length() - 1);
            sb.append(", ");
            sb.append(this.baseType.toString());
            sb.append(" ");
            sb.append(this.ptrVal.getName());
        }
        if (this.index != null) {
            sb.append(", ");
            sb.append(indexType.toString());
            sb.append(" ");
            sb.append(index.getName());
        } else {
            sb.append(", ");
            sb.append(indexType.toString());
            sb.append(" ");
            sb.append(index1.getName());
            sb.append(", ");
            sb.append(indexType.toString());
            sb.append(" ");
            sb.append(index2.getName());
        }
        return sb.toString();
    }
}
