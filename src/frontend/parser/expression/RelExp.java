package frontend.parser.expression;

import frontend.lexer.Token;

import java.util.ArrayList;

public class RelExp {
    private ArrayList<AddExp> addExps;
    private ArrayList<Token> signs;

    public RelExp(ArrayList<AddExp> addExps, ArrayList<Token> signs) {
        this.addExps = addExps;
        this.signs = signs;
    }

    public ArrayList<AddExp> getAddExps() {
        return addExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }
}
