package backend.Insturction;

import backend.MipsReg;

public class Addi extends MipsInstruction {
    private MipsReg rd;
    private MipsReg rt;
    private int immediate;

    public Addi(MipsReg rd, MipsReg rt, int immediate) {
        this.rd = rd;
        this.rt = rt;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return "addi " + rd.toString() + ", " + rt.toString() + ", " + immediate;
    }
}
