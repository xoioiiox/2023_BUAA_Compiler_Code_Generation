package backend.Insturction;

import backend.MipsReg;

public class Jr extends MipsInstruction {
    private MipsReg reg;

    public Jr(MipsReg reg) {
        this.reg = reg;
    }

    @Override
    public String toString() {
        return "jr" + " " + this.reg.toString();
    }
}
