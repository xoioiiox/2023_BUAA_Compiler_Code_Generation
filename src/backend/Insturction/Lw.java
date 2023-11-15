package backend.Insturction;

import backend.MipsReg;

public class Lw extends MipsInstruction {
    private MipsReg desReg;
    private MipsReg baseReg;
    private int offset;
    private String label;

    public Lw(MipsReg desReg, MipsReg baseReg, int offset) {
        this.desReg = desReg;
        this.baseReg = baseReg;
        this.offset = offset;
    }

    public Lw(MipsReg des, String label) {
        this.desReg = des;
        this.label = label;
    }

    @Override
    public String toString() {
        if (label != null) {
            return "lw " + desReg.toString() + ", " + label;
        }
        else {
            return "lw " + desReg.toString() + ", " + offset + "(" + baseReg.toString() + ")";
        }
    }
}
