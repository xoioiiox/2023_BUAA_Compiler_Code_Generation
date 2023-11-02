package frontend.parser.expression;

import frontend.lexer.Token;

import java.util.ArrayList;

public class EqExp {
    ArrayList<RelExp> relExps;
    ArrayList<Token> signs;

    public EqExp(ArrayList<RelExp> relExps, ArrayList<Token> signs) {
        this.relExps = relExps;
        this.signs = signs;
    }
}
