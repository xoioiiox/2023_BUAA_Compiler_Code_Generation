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

    public MipsReg getDesReg() {
        return desReg;
    }

    public MipsReg getBaseReg() {
        return baseReg;
    }

    public int getOffset() {
        return offset;
    }

    public String getLabel() {
        return label;
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
