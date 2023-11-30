package backend.Function;

import backend.BasicBlock.MipsBasicBlock;
import backend.BasicBlock.MipsBasicBlockBuilder;
import backend.MipsRegManager;
import backend.MipsSymbolTable;
import midend.llvmIr.type.IrFunctionType;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.function.IrFunction;

import java.util.ArrayList;

public class MipsFunctionBuilder {
    private IrFunction function;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;
    private MipsSymbolTable symbolTable;
    private MipsRegManager mipsRegManager;

    public MipsFunctionBuilder(IrFunction function) {
        this.function = function;
        this.mipsBasicBlocks = new ArrayList<>();
        this.symbolTable = new MipsSymbolTable(); // 对于每个函数都建一个符号表记录内存位置
        this.mipsRegManager = new MipsRegManager(this.symbolTable);
    }

    public MipsFunction genMipsFunction() {
        int paramNum = ((IrFunctionType)this.function.getValueType()).getParamTypes().size();
        for (int i = 0; i < paramNum; i++) {
            String paramName = ((IrFunctionType) this.function.getValueType()).getParamNames().get(i);
            this.symbolTable.getSymbolMap().put(paramName, this.symbolTable.getOffset(null));
            this.symbolTable.addLocalVal(paramName);
        }
        for (IrBasicBlock basicBlock : function.getBasicBlocks()) {
            MipsBasicBlockBuilder mipsBasicBlockBuilder
                    = new MipsBasicBlockBuilder(basicBlock, symbolTable, this.function.isMainFunc(), mipsRegManager);
            this.mipsBasicBlocks.add(mipsBasicBlockBuilder.genMipsBasicBlock());
        }
        return new MipsFunction(this.function.getName().substring(1), this.function.isMainFunc(), this.mipsBasicBlocks);
    }
}
