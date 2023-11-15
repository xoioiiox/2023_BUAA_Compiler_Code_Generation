package backend.BasicBlock;

import backend.Insturction.MipsInstruction;

import java.util.ArrayList;

public class MipsBasicBlock {
    private String blockName;
    private ArrayList<MipsInstruction> instructions;

    public MipsBasicBlock(String blockName, ArrayList<MipsInstruction> instructions) {
        this.blockName = blockName;
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.blockName);
        sb.append(":\n");
        for (MipsInstruction instruction : instructions) {
            sb.append(instruction.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
