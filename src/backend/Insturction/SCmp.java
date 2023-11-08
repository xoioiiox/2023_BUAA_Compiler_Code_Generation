package backend.Insturction;

import backend.MipsReg;

public class SCmp extends MipsInstruction {
    private SCmpType type;
    private MipsReg rd;
    private MipsReg rs;
    private MipsReg rt;

    public SCmp (SCmpType type, MipsReg rd, MipsReg rs, MipsReg rt) {
        this.type = type;
        this.rd = rd;
        this.rt = rt;
        this.rs = rs;
    }

    @Override
    public String toString() {
        return type.toString() + " " + rd.toString() + ", " + rs.toString() + ", " + rt.toString();
    }
}
