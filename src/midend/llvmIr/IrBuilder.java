package midend.llvmIr;

import frontend.parser.CompUnit;
import frontend.parser.declaration.Decl;
import frontend.parser.function.FuncDef;
import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.function.IrFunctionBuilder;
import midend.llvmIr.value.globalVariable.IrGlobalVariable;
import midend.llvmIr.value.globalVariable.IrGlobalVariableBuilder;
import midend.symbol.SymbolFunc;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * LLVM IR中的一些基础类：
 * Value（值）：Value 类是LLVM IR的基础类，代表了IR中的各种数据和操作，包括常量、指令、全局变量、函数等。Value 是所有其他IR对象的基类。
 * Type（类型）：Type 类表示数据的类型，如整数、浮点数、指针等。不同类型的值在LLVM IR中由不同的Type 对象表示。
 * Constant（常量）：Constant 类表示不会发生更改的常量值，如整数常量、浮点常量和空指针等。这些常量在程序执行过程中是不可变的。
 * Instruction（指令）：Instruction 类表示LLVM IR中的各种操作指令，例如加法、减法、函数调用等。指令用于构建程序的控制流和数据流。
 * GlobalVariable（全局变量）：GlobalVariable 类表示全局变量，这些变量可以在整个程序中访问。全局变量通常用于表示全局数据。
 * Function（函数）：Function 类表示函数定义，包括参数、函数体和返回类型。函数是程序的主要组成部分，包含了程序的逻辑和控制流。
 * BasicBlock（基本块）：BasicBlock 类表示基本块，是LLVM IR中的基本控制流单位。基本块由一系列指令组成，通常以分支指令或跳转指令结束。
 * Module（模块）：Module 类代表整个LLVM模块，包括全局变量、函数定义和其他全局信息。一个模块可以包含整个程序的代码。
 */

public class IrBuilder {
    private CompUnit compUnit;
    private IrModule irModule;
    private SymbolTable symbolTable;

    public IrBuilder (CompUnit compUnit) {
        this.compUnit = compUnit;
        this.symbolTable = new SymbolTable(null);
    }

    public IrModule genIrModule() {
        //todo 何时新建符号表
        ArrayList<Decl> decls = this.compUnit.getDecls();
        ArrayList<FuncDef> funcDefs = this.compUnit.getFuncDefs();
        ArrayList<IrGlobalVariable> irGlobalVariables = new ArrayList<>();
        ArrayList<IrFunction> irFunctions = new ArrayList<>();
        /*---global Decl---*/
        for (Decl decl : decls) {
            IrGlobalVariableBuilder irGlobalVariableBuilder = new IrGlobalVariableBuilder(decl, symbolTable);
            irGlobalVariables.addAll(irGlobalVariableBuilder.genIrGlobalVariable());
        }
        for (FuncDef funcDef : funcDefs) {
            SymbolTable symbolTable1 = new SymbolTable(symbolTable);
            IrFunctionBuilder irFunctionBuilder = new IrFunctionBuilder(symbolTable1, funcDef);
            IrFunction irFunction = irFunctionBuilder.genIrFunction();
            // 函数也要加入根符号表
            SymbolFunc symbolFunc = new SymbolFunc(funcDef.getIdent().getVal(), 0, funcDef.getFuncType().getType());
            symbolFunc.setValue(irFunction);
            this.symbolTable.addSymbol(symbolFunc);
            irFunctions.add(irFunction);
        }
        SymbolTable symbolTable1 = new SymbolTable(symbolTable);
        IrFunctionBuilder irFunctionBuilder = new IrFunctionBuilder(symbolTable1, this.compUnit.getMainFunc());
        /*-----------mainFunc中的index从1开始-----------*/
        irFunctionBuilder.getNameCnt().getCnt();
        irFunctionBuilder.getNameCnt().getBlockCnt();
        irFunctions.add(irFunctionBuilder.genIrFunction());
        this.irModule = new IrModule(irGlobalVariables, irFunctions);
        return this.irModule;
    }
}
