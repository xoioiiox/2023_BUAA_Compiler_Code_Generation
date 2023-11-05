package frontend.parser.expression;

import frontend.lexer.Token;

import java.util.ArrayList;

public class EqExp {
    private ArrayList<RelExp> relExps;
    private ArrayList<Token> signs;

    public EqExp(ArrayList<RelExp> relExps, ArrayList<Token> signs) {
        this.relExps = relExps;
        this.signs = signs;
    }

    public ArrayList<RelExp> getRelExps() {
        return relExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }
}
