package backend.BasicBlock;

import backend.Insturction.MipsInstruction;
import backend.Insturction.MipsInstructionBuilder;
import backend.MipsSymbolTable;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.instruction.IrInstruction;

import java.util.ArrayList;

public class MipsBasicBlockBuilder {
    private IrBasicBlock basicBlock;
    private MipsSymbolTable symbolTable;
    private boolean isMainFunc;

    public MipsBasicBlockBuilder(IrBasicBlock basicBlock, MipsSymbolTable symbolTable, boolean isMainFunc) {
        this.basicBlock = basicBlock;
        this.symbolTable = symbolTable;
        this.isMainFunc = isMainFunc;
    }

    public MipsBasicBlock genMipsBasicBlock() {
        ArrayList<MipsInstruction> mipsInstructions = new ArrayList<>();
        for (IrInstruction instruction : this.basicBlock.getInstructions()) {
            MipsInstructionBuilder mipsInstructionBuilder
                    = new MipsInstructionBuilder(instruction, symbolTable, isMainFunc);
            mipsInstructions.addAll(mipsInstructionBuilder.genMipsInstruction());
        }
        MipsBasicBlock mipsBasicBlock = new MipsBasicBlock(this.basicBlock.getName(), mipsInstructions);
        return mipsBasicBlock;
    }
}
