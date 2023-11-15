package backend.BasicBlock;

import backend.Insturction.*;

import java.util.ArrayList;

public class MipsBasicBlock {
    private String blockName;
    private ArrayList<MipsInstruction> instructions;

    public MipsBasicBlock(String blockName, ArrayList<MipsInstruction> instructions) {
        this.blockName = blockName;
        this.instructions = instructions;
    }

    public ArrayList<MipsInstruction> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.blockName);
        sb.append(":\n");
        for (int i = 0; i < instructions.size(); i++) {
            MipsInstruction instruction = instructions.get(i);
            if (instruction instanceof Sw && i < instructions.size() - 1) { // sw lw
                MipsInstruction instruction1 = instructions.get(i + 1);
                if (instruction1 instanceof Lw) {
                    if (((Lw) instruction1).getLabel() == null && ((Sw) instruction).getLabel() == null) {
                        if (((Lw) instruction1).getBaseReg().getRegNum() == ((Sw) instruction).getBaseReg().getRegNum()
                                && ((Lw) instruction1).getDesReg().getRegNum() == ((Sw) instruction).getSrcReg().getRegNum()
                                && ((Lw) instruction1).getOffset() == ((Sw) instruction).getOffset()) {
                            i++;
                        }
                    }
                    else {
                        if (((Lw) instruction1).getLabel() == ((Sw) instruction).getLabel()
                                && ((Lw) instruction1).getDesReg().getRegNum() == ((Sw) instruction).getSrcReg().getRegNum()) {
                            i++;
                        }
                    }
                }
            }
            sb.append(instruction.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
