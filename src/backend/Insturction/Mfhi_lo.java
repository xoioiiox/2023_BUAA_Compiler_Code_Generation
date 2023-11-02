package backend.Insturction;

import backend.MipsReg;

public class Mfhi_lo extends MipsInstruction {
    private MipsReg rd;
    private boolean isHi;

    public Mfhi_lo(MipsReg rd, boolean isHi) {
        this.rd = rd;
        this.isHi = isHi;
    }

    @Override
    public String toString() {
        return ((isHi)? "mfhi " : "mflo ") + rd.toString();
    }
}
