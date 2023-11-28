import backend.MipsBuilder;
import backend.MipsModule;
import midend.llvmIr.IrModule;
import midend.symbol.SymbolTable;
import frontend.lexer.Lexer;
import frontend.lexer.LexerIterator;
import frontend.parser.CompUnit;
import midend.llvmIr.IrBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;

public class Compiler {
    public static void main(String[] args) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream("testfile.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        /*-------------词法分析--------------*/
        Lexer lexer = new Lexer(inputStream);
        LexerIterator iterator = new LexerIterator(lexer.getTokens());
        /*-------------建符号表--------------*/
        SymbolTable symbolTable = new SymbolTable(null);
        /*-------------语法分析--------------*/
        CompUnit compUnit = new CompUnit(iterator, symbolTable);
        compUnit.parseCompUnit();
        /*try {
            PrintStream printStream = new PrintStream("output.txt");
            System.setOut(printStream); //将sout重定向到文件输出
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }*/
        //ParserOutput.Print();
        /*-------------错误处理--------------*/
        /*try {
            PrintStream printStream = new PrintStream("error.txt");
            System.setOut(printStream); //将sout重定向到文件输出
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ErrorTable.outPut();
        if (!ErrorTable.isEmpty()) { // 若程序错误则提前结束编译
            return;
        }*/
        /*-------------中间代码--------------*/
        try {
            PrintStream printStream = new PrintStream("llvm_ir.txt");
            System.setOut(printStream); //将sout重定向到文件输出
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        IrBuilder irBuilder = new IrBuilder(compUnit);
        IrModule irModule = irBuilder.genIrModule();
        irModule.IrPrint();
        /*-------------目标代码--------------*/
        try {
            PrintStream printStream = new PrintStream("mips.txt");
            System.setOut(printStream); //将sout重定向到文件输出
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        MipsBuilder mipsBuilder = new MipsBuilder(irModule);
        MipsModule mipsModule = mipsBuilder.genMipsModule();
        mipsModule.printMips();
    }
}