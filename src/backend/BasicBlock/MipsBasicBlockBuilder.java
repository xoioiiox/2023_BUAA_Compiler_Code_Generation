package backend.BasicBlock;

import backend.Insturction.MipsInstruction;
import backend.Insturction.MipsInstructionBuilder;
import backend.MipsRegManager;
import backend.MipsSymbolTable;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.instruction.IrInstruction;

import java.util.ArrayList;

public class MipsBasicBlockBuilder {
    private IrBasicBlock basicBlock;
    private MipsSymbolTable symbolTable;
    private boolean isMainFunc;
    private MipsRegManager mipsRegManager;

    public MipsBasicBlockBuilder(IrBasicBlock basicBlock, MipsSymbolTable symbolTable,
                                 boolean isMainFunc, MipsRegManager mipsRegManager) {
        this.basicBlock = basicBlock;
        this.symbolTable = symbolTable;
        this.isMainFunc = isMainFunc;
        this.mipsRegManager = mipsRegManager;
    }

    public MipsBasicBlock genMipsBasicBlock() {
        ArrayList<MipsInstruction> mipsInstructions = new ArrayList<>();
        for (IrInstruction instruction : this.basicBlock.getInstructions()) {
            MipsInstructionBuilder mipsInstructionBuilder
                    = new MipsInstructionBuilder(instruction, symbolTable, isMainFunc, mipsRegManager);
            mipsInstructions.addAll(mipsInstructionBuilder.genMipsInstruction());
        }
        MipsBasicBlock mipsBasicBlock = new MipsBasicBlock(this.basicBlock.getName(), mipsInstructions);
        return mipsBasicBlock;
    }
}
