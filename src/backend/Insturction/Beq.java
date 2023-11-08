package backend.Insturction;

import backend.MipsReg;

public class Beq extends MipsInstruction {
    private MipsReg reg;
    private int cmpNum;
    private String label;

    public Beq (MipsReg reg, int cmpNum, String label) {
        this.reg = reg;
        this.cmpNum = cmpNum;
        this.label = label;
    }

    @Override
    public String toString() {
        return "beq " + reg.toString() + ", " + cmpNum + ", " + label;
    }
}
