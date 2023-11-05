package frontend.parser.expression;

import java.util.ArrayList;

public class LOrExp {
    private ArrayList<LAndExp> lAndExps;

    public LOrExp(ArrayList<LAndExp> lAndExps) {
        this.lAndExps = lAndExps;
    }

    public ArrayList<LAndExp> getlAndExps() {
        return lAndExps;
    }
}
