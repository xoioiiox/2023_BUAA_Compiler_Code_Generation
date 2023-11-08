package backend.Insturction;

public class J extends MipsInstruction {
    private String label;

    public J (String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "j " + label;
    }
}
