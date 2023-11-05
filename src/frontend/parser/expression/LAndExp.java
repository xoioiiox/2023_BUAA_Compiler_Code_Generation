package frontend.parser.expression;

import java.util.ArrayList;

public class LAndExp {
    private ArrayList<EqExp> eqExps;

    public LAndExp(ArrayList<EqExp> eqExps) {
        this.eqExps = eqExps;
    }

    public ArrayList<EqExp> getEqExps() {
        return eqExps;
    }
}
