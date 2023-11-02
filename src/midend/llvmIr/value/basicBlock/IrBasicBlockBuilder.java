package midend.llvmIr.value.basicBlock;

import frontend.parser.statement.*;
import midend.llvmIr.value.function.NameCnt;
import midend.llvmIr.value.instruction.IrInstruction;
import midend.llvmIr.value.instruction.IrInstructionBuilder;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class IrBasicBlockBuilder {
    private SymbolTable symbolTable;
    private Block block;
    private NameCnt nameCnt;
    private ArrayList<IrBasicBlock> basicBlocks;

    public IrBasicBlockBuilder(SymbolTable symbolTable, Block block, NameCnt nameCnt) {
        this.symbolTable = symbolTable;
        this.block = block;
        this.nameCnt = nameCnt;
        this.basicBlocks = new ArrayList<>();
    }

    public ArrayList<IrBasicBlock> genIrBasicBlock() { //todo 单独;
        for (int i = 0; i < block.getBlockItems().size();) {
            BlockItem blockItem = block.getBlockItems().get(i);
            Stmt stmt = blockItem.getStmt();
            //基本块
            if (stmt instanceof Block || stmt instanceof StmtIf || stmt instanceof StmtFor) {
                if (stmt instanceof Block) {
                    SymbolTable symbolTable1 = new SymbolTable(symbolTable);
                    IrBasicBlockBuilder basicBlockBuilder
                            = new IrBasicBlockBuilder(symbolTable1, (Block) stmt, this.nameCnt);
                    this.basicBlocks.addAll(basicBlockBuilder.genIrBasicBlock()); //todo check
                }
                else if (stmt instanceof StmtIf) {

                }
                else if (stmt instanceof StmtFor) {

                }
                i++; //!!!
            }
            //普通语句
            else {
                IrBasicBlock basicBlock = new IrBasicBlock("block_" + this.nameCnt.getBlockCnt()); //todo
                for (; i < block.getBlockItems().size(); i++) {
                    BlockItem blockItem1 = block.getBlockItems().get(i);
                    Stmt stmt1 = blockItem1.getStmt();
                    if (stmt1 instanceof Block || stmt1 instanceof StmtIf || stmt1 instanceof StmtFor) {
                        break;
                    }
                    IrInstructionBuilder instructionBuilder
                            = new IrInstructionBuilder(this.symbolTable, blockItem1, nameCnt);
                    ArrayList<IrInstruction> instructions = instructionBuilder.genIrInstruction();
                    basicBlock.getInstructions().addAll(instructions);
                }
                this.basicBlocks.add(basicBlock);
            }
        }
        return basicBlocks;
    }

}
