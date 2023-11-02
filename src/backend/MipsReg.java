package backend;

public class MipsReg {
    private int regNum;

    public MipsReg(int regNum) {
        this.regNum = regNum;
    }

    @Override
    public String toString() {
        String s = null;
        if (regNum == 2 || regNum == 3) {
            s = "$v" + (regNum - 2);
        }
        else if (4 <= regNum && regNum <= 7) {
            s = "$a" + (regNum - 4);
        }
        else if (8 <= regNum && regNum <= 15) {
            s = "$t" + (regNum - 8);
        }
        else if (16 <= regNum && regNum <= 23) {
            s = "$s" + (regNum - 16);
        }
        else if (26 <= regNum && regNum <= 27) {
            s = "$k" + (regNum - 26);
        }
        else if (regNum == 28) {
            s = "$gp";
        }
        else if (regNum == 29) {
            s = "$sp";
        }
        else if (regNum == 30) {
            s = "$fp";
        }
        else if (regNum == 31) {
            s = "$ra";
        }
        return s;
    }
}
