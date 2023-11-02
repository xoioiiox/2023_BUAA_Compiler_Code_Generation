package frontend.parser.expression;

import midend.symbol.Symbol;
import midend.symbol.SymbolCon;
import midend.symbol.SymbolTable;
import midend.symbol.SymbolVar;
import frontend.lexer.Token;

import java.util.ArrayList;

public class LVal {
    private Token Ident;
    private ArrayList<Exp> exps;
    private SymbolTable curSymbolTable;

    public LVal(Token Ident, ArrayList<Exp> exps, SymbolTable curSymbolTable) {
        this.Ident = Ident;
        this.exps = exps;
        this.curSymbolTable = curSymbolTable;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public Token getIdent() {
        return Ident;
    }

    public int getDimension() {
        Symbol symbol = curSymbolTable.getSymbol(Ident.getVal());
        if (!(symbol instanceof SymbolVar) && !(symbol instanceof SymbolCon)) {
            return -2; //未定义
        }
        int dim1, dim2; //dim1为原定义维度
        if (symbol instanceof SymbolVar) {
            dim1 = ((SymbolVar) symbol).getDimension();
        }
        else {
            dim1 = ((SymbolCon) symbol).getDimension();
        }
        dim2 = exps.size();
        return dim1 - dim2;
    }

    public int calculate(SymbolTable symbolTable) { //symbolTable here is created during generation
        if (this.exps.size() == 0) {
            Symbol symbol = symbolTable.getSymbol(this.Ident.getVal());
            if (symbol instanceof SymbolCon) {
                return ((SymbolCon) symbol).getInitVal();
            }
            else if (symbol instanceof SymbolVar) {
                return ((SymbolVar) symbol).getInitVal();
            }
        }
        else if (this.exps.size() == 1) {
            //todo
        }
        else if (this.exps.size() == 2) {
            //todo
        }
        return -1;
    }
}
