package backend.Insturction;

import backend.MipsReg;

public class Binary extends MipsInstruction {
    private BinaryType binaryType;
    private MipsReg rd; //des reg
    private MipsReg rs; //src reg1
    private MipsReg rt; //src reg2

    public Binary(BinaryType binaryType, MipsReg rd, MipsReg rs, MipsReg rt) {
        this.binaryType = binaryType;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return this.binaryType.toString() + " " + rd.toString() + ", " + rs.toString() + ", " + rt.toString();
    }
}
