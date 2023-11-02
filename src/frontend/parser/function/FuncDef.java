package frontend.parser.function;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.statement.Block;

import java.util.ArrayList;

public class FuncDef {
    private FuncType funcType;
    private Token Ident;
    private ArrayList<FuncFParam> funcFParams;
    private Block block;

    public FuncDef(FuncType funcType, Token Ident, ArrayList<FuncFParam> funcFParams, Block block) {
        this.funcType = funcType;
        this.Ident = Ident;
        this.funcFParams = funcFParams;
        this.block = block;
    }

    public boolean isMainFunc() {
        return this.Ident.getLexType() == LexType.MAINTK;
    }
    public Token getIdent() {
        return Ident;
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public Block getBlock() {
        return block;
    }

    public FuncType getFuncType() {
        return funcType;
    }
}
