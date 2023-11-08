package backend.Insturction;

import backend.MipsReg;

public class Sw extends MipsInstruction {
    private MipsReg srcReg;
    private MipsReg baseReg;
    private int offset;
    private String label;

    public Sw(MipsReg srcReg, MipsReg baseReg, int offset) {
        this.srcReg = srcReg;
        this.baseReg = baseReg;
        this.offset = offset;
    }

    public Sw(MipsReg srcReg, String label) {
        this.srcReg = srcReg;
        this.label = label;
    }

    @Override
    public String toString() {
        if (label == null) {
            return "sw " + srcReg.toString() + ", " + offset + "(" + baseReg.toString() + ")";
        }
        else {
            return "sw " + srcReg.toString() + ", " + label;
        }
    }
}
