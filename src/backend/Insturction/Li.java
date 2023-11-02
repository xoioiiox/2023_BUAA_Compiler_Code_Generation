package backend.Insturction;

import backend.MipsReg;

public class Li extends MipsInstruction {
    private MipsReg reg;
    private int immediate;

    public Li(MipsReg reg, int immediate) {
        this.reg = reg;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return "li " + reg.toString() + ", " + immediate;
    }
}
