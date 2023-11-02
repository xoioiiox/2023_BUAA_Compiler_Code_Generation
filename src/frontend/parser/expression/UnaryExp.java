package frontend.parser.expression;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import midend.symbol.Symbol;
import midend.symbol.SymbolFunc;
import midend.symbol.SymbolTable;

public class UnaryExp {
    private Token Ident;
    private FuncRParams funcRParams;
    private PrimaryExp primaryExp;
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;
    private SymbolTable curSymbolTable;

    public UnaryExp(UnaryOp unaryOp, UnaryExp unaryExp, SymbolTable curSymbolTable) {
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
        this.curSymbolTable = curSymbolTable;
    }

    public UnaryExp(Token Ident, FuncRParams funcRParams, SymbolTable curSymbolTable) {
        this.Ident = Ident;
        this.funcRParams = funcRParams;
        this.curSymbolTable = curSymbolTable;
    }

    public UnaryExp(PrimaryExp primaryExp, SymbolTable curSymbolTable) {
        this.primaryExp = primaryExp;
        this.curSymbolTable = curSymbolTable;
    }

    public Token getIdent() {
        return Ident;
    }

    public FuncRParams getFuncRParams() {
        return funcRParams;
    }

    public PrimaryExp getPrimaryExp() {
        return primaryExp;
    }

    public UnaryExp getUnaryExp() {
        return unaryExp;
    }

    public UnaryOp getUnaryOp() {
        return unaryOp;
    }

    public int getDimension() {
        if (primaryExp != null) {
            return primaryExp.getDimension();
        }
        else if (unaryExp != null) {
            return unaryExp.getDimension();
        }
        else {
            Symbol symbol = curSymbolTable.getSymbol(Ident.getVal()); //TODO 若之前定义函数时重名怎么办？
            if (symbol instanceof SymbolFunc) {
                return ((SymbolFunc) symbol).getReType();
            }
            else {
                return -2;
            }
        }
    }

    public int calculate(SymbolTable symbolTable) {
        if (this.primaryExp != null) {
            return this.primaryExp.calculate(symbolTable);
        }
        else if (this.Ident != null) {
            //todo can be func ret?
        }
        else if (this.unaryExp != null) {
            if (this.unaryOp.getOp().getLexType() == LexType.PLUS) {
                return this.unaryExp.calculate(symbolTable);
            }
            else if (this.unaryOp.getOp().getLexType() == LexType.MINU) {
                return -1 * this.unaryExp.calculate(symbolTable);
            }
        }
        return -1;
    }
}
