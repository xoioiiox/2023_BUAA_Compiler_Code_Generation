package backend.Insturction;

import backend.MipsReg;

public class Sw extends MipsInstruction {
    private MipsReg srcReg;
    private MipsReg baseReg;
    private int offset;

    public Sw(MipsReg srcReg, MipsReg baseReg, int offset) {
        this.srcReg = srcReg;
        this.baseReg = baseReg;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "sw " + srcReg.toString() + ", " + offset + "(" + baseReg.toString() + ")";
    }
}
