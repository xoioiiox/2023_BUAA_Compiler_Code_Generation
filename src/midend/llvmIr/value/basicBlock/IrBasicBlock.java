package midend.llvmIr.value.basicBlock;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

import java.util.ArrayList;

public class IrBasicBlock extends IrValue {
    private ArrayList<IrInstruction> instructions;

    public IrBasicBlock(String name) {
        super(name);
        this.instructions = new ArrayList<>();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    public ArrayList<IrInstruction> getInstructions() {
        return instructions;
    }

    public void addInstruction(IrInstruction instruction) {
        this.instructions.add(instruction);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(this.getName()); //todo
        sb.append(":");
        sb.append("\n");
        for (IrInstruction instruction : instructions) {
            sb.append("\t");
            sb.append(instruction.toString());
            sb.append("\n");
        }
        if (!instructions.isEmpty()) { //todo
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }
}
