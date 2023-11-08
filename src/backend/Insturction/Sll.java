package backend.Insturction;

import backend.MipsReg;

public class Sll extends MipsInstruction {
    private MipsReg rd;
    private MipsReg rs;
    private int num;

    public Sll(MipsReg rd, MipsReg rs, int num) {
        this.rd = rd;
        this.rs = rs;
        this.num = num;
    }

    @Override
    public String toString() {
        return "sll " + rd.toString() + ", " + rs.toString() +", " + num;
    }
}
