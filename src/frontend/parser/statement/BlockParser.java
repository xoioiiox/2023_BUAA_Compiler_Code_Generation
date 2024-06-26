package frontend.parser.statement;

import midend.symbol.SymbolTable;
import frontend.io.Output;
import frontend.io.ParserOutput;
import frontend.lexer.LexType;
import frontend.lexer.LexerIterator;
import frontend.parser.declaration.DeclParser;

import java.util.ArrayList;

public class BlockParser {
    private LexerIterator iterator;
    private SymbolTable curSymbolTable;
    private int inFor;

    public BlockParser(LexerIterator iterator, SymbolTable curSymbolTable, int inFor) {
        this.iterator = iterator;
        this.curSymbolTable = curSymbolTable;
        this.inFor = inFor;
    }

    public Block parseBlock() {
        /*进入block需要新建符号表*/
        curSymbolTable = new SymbolTable(curSymbolTable);
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        iterator.read(); // {
        while (iterator.preRead(1).getLexType() != LexType.RBRACE) {
            blockItems.add(parseBlockItem(false));
        }
        iterator.read(); // }
        Output output = new Output("<Block>");
        ParserOutput.addOutput(output);
        return new Block(blockItems);
    }

    public BlockItem parseBlockItem(boolean checkVoidReturn) {
        BlockItem blockItem;
        if (iterator.preRead(1).getLexType() == LexType.CONSTTK
                || iterator.preRead(1).getLexType() == LexType.INTTK) {
            DeclParser declParser = new DeclParser(iterator, curSymbolTable);
            blockItem = new BlockItem(declParser.parseDecl());
        }
        else {
            StmtParser stmtParser = new StmtParser(iterator, curSymbolTable, checkVoidReturn, inFor);
            blockItem = new BlockItem(stmtParser.parseStmt());
        }
        return blockItem;
    }

}
