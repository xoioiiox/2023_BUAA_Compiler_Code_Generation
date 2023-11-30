package midend.llvmIr.value.function;

import frontend.parser.function.FuncDef;
import frontend.parser.function.FuncFParam;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.*;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.basicBlock.IrBasicBlockBuilder;
import midend.llvmIr.value.instruction.IrInstruction;
import midend.llvmIr.value.instruction.memory.IrAlloca;
import midend.llvmIr.value.instruction.memory.IrStore;
import midend.llvmIr.value.instruction.terminator.IrRet;
import midend.symbol.Symbol;
import midend.symbol.SymbolFunc;
import midend.symbol.SymbolTable;
import midend.symbol.SymbolVar;

import java.util.ArrayList;

public class IrFunctionBuilder {
    private SymbolTable symbolTable;
    private FuncDef funcDef;
    private NameCnt nameCnt;

    public IrFunctionBuilder(SymbolTable symbolTable, FuncDef funcDef) {
        this.symbolTable = symbolTable;
        this.funcDef = funcDef;
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
        ArrayList<String> symNames = new ArrayList<>();
        ArrayList<IrValue> paramValues = new ArrayList<>();
        for (FuncFParam param : funcDef.getFuncFParams()) {
            symNames.add(param.getIdent().getVal());
            // 参数加入符号表
            IrValueType paramType = null;
            String name = "%" + nameCnt.getCnt();
            names.add(name);
            if (param.getDimension() == 0) {
                paramType = new IrIntType(32);
            }
            else if (param.getDimension() == 1) {
                paramType = new IrPointerType(new IrIntType(32));
            }
            else if (param.getDimension() == 2) {
                int v = param.getConstExp().calculate(symbolTable);
                paramType = new IrPointerType(new IrArrayType(new IrIntType(32), v));
            }
            paramTypes.add(paramType);
            IrValue value = new IrValue(name, paramType);
            paramValues.add(value);
            SymbolVar symbolVar = new SymbolVar(param.getIdent().getVal(), param.getDimension(), value);
            symbolTable.addSymbol(symbolVar);
        }
        IrFunctionType functionType = new IrFunctionType(retType, paramTypes, names);
        /*------加入参数Load语句------*/
        /*ArrayList<IrInstruction> paramInst = new ArrayList<>();
        for (int i = 0; i < paramTypes.size(); i++) {
            IrValue irValue = new IrValue("%" + this.nameCnt.getCnt(), paramTypes.get(i));
            IrAlloca irAlloca = new IrAlloca(paramTypes.get(i), irValue);
            IrStore irStore = new IrStore(irAlloca, paramValues.get(i));
            paramInst.add(irAlloca);
            paramInst.add(irStore);
            Symbol symbol = symbolTable.getSymbol(symNames.get(i));
            symbol.setValue(irAlloca);
        }*/
        /*------防止递归调用，需要先将函数符号加入符号表再进行block解析------*/
        SymbolFunc symbolFunc = new SymbolFunc(funcDef.getIdent().getVal(), 0, funcDef.getFuncType().getType());
        this.symbolTable.addSymbol(symbolFunc);
        ArrayList<IrBasicBlock> basicBlocks = new ArrayList<>();
        IrFunction function = new IrFunction("@" + this.funcDef.getIdent().getVal(),
                functionType, this.funcDef.isMainFunc(), basicBlocks);//, paramInst);
        symbolFunc.setValue(function);
        /*------function block------*/
        IrBasicBlockBuilder basicBlockBuilder
                = new IrBasicBlockBuilder(funcDef.getIdent().getVal() + "_", symbolTable, funcDef.getBlock(), nameCnt);
        ArrayList<IrBasicBlock> blocks = basicBlockBuilder.genIrBasicBlock();
        function.setBasicBlocks(blocks);
        if (funcDef.getFuncType().getType() == -1) { //void 一律在末尾加return todo 会有吗
            IrBasicBlock basicBlock = new IrBasicBlock(funcDef.getIdent().getVal() +"_" + this.nameCnt.getCnt());
            blocks.add(basicBlock);
            IrRet irRet = new IrRet(new IrVoidType(), false);
            basicBlock.addInstruction(irRet);
        }
        return function;
    }

    public NameCnt getNameCnt() {
        return nameCnt;
    }
}