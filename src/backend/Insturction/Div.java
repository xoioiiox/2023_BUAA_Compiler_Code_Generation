package backend.Insturction;

import backend.MipsReg;

// div rs, rt
public class Div extends MipsInstruction {
    private MipsReg rs;
    private MipsReg rt;

    public Div(MipsReg rs, MipsReg rt) {
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString () {
        return "div " + rs.toString() + ", " + rt.toString();
    }
}
