package backend.Insturction;

import backend.MipsReg;

public class La extends MipsInstruction {
    private MipsReg desReg;
    private String addr;

    public La(MipsReg desReg, String addr) {
        this.desReg = desReg;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "la " + desReg.toString() + ", " + addr;
    }
}
