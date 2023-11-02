package frontend.parser;
import frontend.parser.function.FuncFParam;
import frontend.parser.function.FuncType;
import midend.error.ErrorTable;
import midend.error.ErrorType;
import midend.symbol.SymbolTable;
import midend.error.Error;
import frontend.io.Output;
import frontend.io.ParserOutput;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.lexer.LexerIterator;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.DeclParser;
import frontend.parser.function.FuncDef;
import frontend.parser.function.FuncDefParser;
import frontend.parser.statement.Block;
import frontend.parser.statement.BlockParser;
import frontend.parser.statement.Stmt;
import frontend.parser.statement.StmtReturn;

import java.util.ArrayList;

public class CompUnit {
    private LexerIterator iterator;
    private ArrayList<Decl> decls;
    private ArrayList<FuncDef> funcDefs;
    private FuncDef mainFunc;
    private Block block;
    private SymbolTable curSymbolTable;

    public CompUnit (LexerIterator iterator, SymbolTable curSymbolTable) {
        this.iterator = iterator;
        this.decls = new ArrayList<>();
        this.funcDefs = new ArrayList<>();
        this.block = null;
        this.curSymbolTable = curSymbolTable;
    }

    public void parseCompUnit() {
        parseDecls();
        parseFuncDefs();
        parseMainFuncDef();
        Output output = new Output("<CompUnit>");
        ParserOutput.addOutput(output);
    }

    public void parseDecls() {
        while (iterator.hasNext()) {
            Token preToken3 = iterator.preRead(3); //null?
            if (preToken3.getLexType() == LexType.LPARENT) { //FuncDef
                return;
            }
            DeclParser declParser = new DeclParser(iterator, curSymbolTable);
            decls.add(declParser.parseDecl());
        }
    }

    public void parseFuncDefs() {
        while (iterator.hasNext()) {
            Token preToken2 = iterator.preRead(2);
            if (preToken2.getLexType() == LexType.MAINTK) {
                return;
            }
            FuncDefParser funcDefParser = new FuncDefParser(iterator, curSymbolTable);
            funcDefs.add(funcDefParser.parseFuncDef());
        }
    }

    public void parseMainFuncDef() {
        Token type = iterator.read(); // int
        Token Ident =iterator.read(); // main
        iterator.read(); // (
        //iterator.read(); // )
        checkErrorJ();
        BlockParser blockParser = new BlockParser(iterator, curSymbolTable, 0);
        block = blockParser.parseBlock();
        if (block.getBlockItems().size() == 0) {
            Error error = new Error(iterator.readLast().getLineNum(), ErrorType.g);
            ErrorTable.addError(error);
        }
        else {
            Stmt stmt = block.getBlockItems().get(block.getBlockItems().size() - 1).getStmt();
            if (!(stmt instanceof StmtReturn)) {
                Error error = new Error(iterator.readLast().getLineNum(), ErrorType.g);
                ErrorTable.addError(error);
            }
        }
        ArrayList<FuncFParam> params = new ArrayList<>();
        FuncType funcType = new FuncType(type);
        this.mainFunc = new FuncDef(funcType, Ident, params, block);
        Output output = new Output("<MainFuncDef>");
        ParserOutput.addOutput(output);
    }

    public void checkErrorJ() {
        if (iterator.preRead(1).getLexType() == LexType.RPARENT) {
            iterator.read();
        }
        else {
            Error error = new Error(iterator.readLast().getLineNum(), ErrorType.j);
            ErrorTable.addError(error);
        }
    }

    public ArrayList<Decl> getDecls() {
        return decls;
    }

    public ArrayList<FuncDef> getFuncDefs() {
        return funcDefs;
    }

    public FuncDef getMainFunc() {
        return mainFunc;
    }
}
