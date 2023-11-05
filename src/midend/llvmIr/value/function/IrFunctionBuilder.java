package midend.llvmIr.value.function;

import frontend.parser.function.FuncDef;
import frontend.parser.function.FuncFParam;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrFunctionType;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.type.IrVoidType;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.basicBlock.IrBasicBlockBuilder;
import midend.llvmIr.value.instruction.IrInstruction;
import midend.symbol.Symbol;
import midend.symbol.SymbolFunc;
import midend.symbol.SymbolTable;
import midend.symbol.SymbolVar;

import java.util.ArrayList;

public class IrFunctionBuilder {
    private SymbolTable symbolTable;
    private FuncDef funcDef;
    private ArrayList<IrBasicBlock> irBasicBlocks;
    private NameCnt nameCnt;

    public IrFunctionBuilder(SymbolTable symbolTable, FuncDef funcDef) {
        this.symbolTable = symbolTable;
        this.funcDef = funcDef;
        this.irBasicBlocks = new ArrayList<>();
        this.nameCnt = new NameCnt();
    }

    public IrFunction genIrFunction() {
        /*------function head------*/
        IrValueType retType;
        if (funcDef.getFuncType().getType() == 0) {
            retType = new IrIntType(32);
        }
        else {
            retType = new IrVoidType();
        }
        ArrayList<IrValueType> paramTypes = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for (FuncFParam param : funcDef.getFuncFParams()) {
            //todo 参数加入符号表
            IrValueType paramType;
            if (param.getDimension() == 0) {
                String name = "%" + nameCnt.getCnt();
                names.add(name);
                paramType = new IrIntType(32);
                paramTypes.add(paramType);
                IrValue value = new IrValue(name, paramType);
                SymbolVar symbolVar = new SymbolVar(param.getIdent().getVal(), param.getDimension(), value);
                symbolTable.addSymbol(symbolVar);
            }
            else if (param.getDimension() == 1) {
                //todo
            }
            else if (param.getDimension() == 2) {
                //todo
            }
        }
        IrFunctionType functionType = new IrFunctionType(retType, paramTypes, names);
        /*------function block------*/
        IrBasicBlockBuilder basicBlockBuilder
                = new IrBasicBlockBuilder(funcDef.getIdent().getVal(), symbolTable, funcDef.getBlock(), nameCnt);
        ArrayList<IrBasicBlock> basicBlocks  = basicBlockBuilder.genIrBasicBlock();
        IrFunction function = new IrFunction("@" + this.funcDef.getIdent().getVal(),
                functionType, this.funcDef.isMainFunc(), basicBlocks);
        SymbolFunc symbolFunc = new SymbolFunc(funcDef.getIdent().getVal(), 0, funcDef.getFuncType().getType());
        this.symbolTable.addSymbol(symbolFunc);
        symbolFunc.setValue(function);
        return function;
    }

    public NameCnt getNameCnt() {
        return nameCnt;
    }
}