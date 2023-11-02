package backend.Function;

import backend.BasicBlock.MipsBasicBlock;
import backend.BasicBlock.MipsBasicBlockBuilder;
import backend.MipsSymbolTable;
import midend.llvmIr.type.IrFunctionType;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.function.IrFunction;

import java.util.ArrayList;

public class MipsFunctionBuilder {
    private IrFunction function;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;
    private MipsSymbolTable symbolTable;

    public MipsFunctionBuilder(IrFunction function, MipsSymbolTable symbolTable) {
        this.function = function;
        this.mipsBasicBlocks = new ArrayList<>();
        this.symbolTable = symbolTable;
    }

    public MipsFunction genMipsFunction() {
        //todo head 加入符号表
        int paramNum = ((IrFunctionType)this.function.getValueType()).getParamTypes().size();
        for (int i = 0; i < paramNum; i++) {
            this.symbolTable.getSymbolMap().put(
                    ((IrFunctionType)this.function.getValueType()).getParamNames().get(i),
                    this.symbolTable.getOffset(null));
        }
        for (IrBasicBlock basicBlock : function.getBasicBlocks()) {
            MipsBasicBlockBuilder mipsBasicBlockBuilder
                    = new MipsBasicBlockBuilder(basicBlock, symbolTable, this.function.isMainFunc());
            this.mipsBasicBlocks.add(mipsBasicBlockBuilder.genMipsBasicBlock());
        }
        MipsFunction mipsFunction = new MipsFunction(
                this.function.getName().substring(1),
                this.function.isMainFunc(), this.mipsBasicBlocks);
        return mipsFunction;
    }
}
