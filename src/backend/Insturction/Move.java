package backend.Insturction;

import backend.MipsReg;

public class Move extends MipsInstruction {
    private MipsReg desReg;
    private MipsReg srcReg;

    public Move(MipsReg desReg, MipsReg srcReg) {
        this.srcReg = srcReg;
        this.desReg = desReg;
    }

    @Override
    public String toString() {
        return "move " + desReg.toString() + ", " + srcReg.toString();
    }
}
