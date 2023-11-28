package backend.Insturction;

import backend.MipsReg;

public class Addiu extends MipsInstruction {
    private MipsReg rd;
    private MipsReg rt;
    private int immediate;

    public Addiu(MipsReg rd, MipsReg rt, int immediate) {
        this.rd = rd;
        this.rt = rt;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return "addiu " + rd.toString() + ", " + rt.toString() + ", " + immediate;
    }
}
