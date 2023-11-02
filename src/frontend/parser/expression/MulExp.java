package frontend.parser.expression;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class MulExp {
    private ArrayList<Token> signs;
    private ArrayList<UnaryExp> unaryExps;

    public MulExp(ArrayList<Token> signs, ArrayList<UnaryExp> unaryExps) {
        this.signs = signs;
        this.unaryExps = unaryExps;
    }

    public ArrayList<Token> getSigns() {
        return signs;
    }

    public ArrayList<UnaryExp> getUnaryExps() {
        return unaryExps;
    }

    public int getDimension() {
        return unaryExps.get(0).getDimension();
    }

    public int calculate(SymbolTable symbolTable) {
        int sum = unaryExps.get(0).calculate(symbolTable);
        for (int i = 0; i < signs.size(); i++) {
            if (signs.get(i).getLexType() == LexType.MULT) {
                sum *= unaryExps.get(i + 1).calculate(symbolTable);
            }
            else if (signs.get(i).getLexType() == LexType.DIV) {
                sum /= unaryExps.get(i + 1).calculate(symbolTable);
            }
            else if (signs.get(i).getLexType() == LexType.MOD) { //todo how to cal?
                sum = sum % unaryExps.get(i + 1).calculate(symbolTable);
            }
        }
        return sum;
    }
}
