package frontend.parser.expression;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class AddExp {
    private ArrayList<Token> signs;
    private ArrayList<MulExp> mulExps;

    public AddExp(ArrayList<Token> signs, ArrayList<MulExp> mulExps) {
        this.signs = signs;
        this.mulExps = mulExps;
    }

    public ArrayList<MulExp> getMulExps() {
        return mulExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    public int getDimension() {
        return mulExps.get(0).getDimension();
    }

    public int calculate(SymbolTable symbolTable) {
        int sum = mulExps.get(0).calculate(symbolTable);
        for (int i = 0; i < signs.size(); i++) {
            if (signs.get(i).getLexType() == LexType.PLUS) {
                sum += mulExps.get(i + 1).calculate(symbolTable);
            }
            else if (signs.get(i).getLexType() == LexType.MINU) {
                sum -= mulExps.get(i + 1).calculate(symbolTable);
            }
        }
        return sum;
    }
}
