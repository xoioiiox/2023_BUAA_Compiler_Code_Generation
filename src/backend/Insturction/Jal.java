package backend.Insturction;

public class Jal extends MipsInstruction {
    private String label;

    public Jal(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "jal " + this.label;
    }
}
