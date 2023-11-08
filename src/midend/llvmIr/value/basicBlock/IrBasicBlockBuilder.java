package midend.llvmIr.value.basicBlock;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.expression.*;
import frontend.parser.statement.*;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrFunctionType;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.function.NameCnt;
import midend.llvmIr.value.instruction.IrInstruction;
import midend.llvmIr.value.instruction.IrInstructionBuilder;
import midend.llvmIr.value.instruction.binary.IrBinaryInst;
import midend.llvmIr.value.instruction.binary.IrBinaryType;
import midend.llvmIr.value.instruction.cond.IrBr;
import midend.llvmIr.value.instruction.cond.IrIcmp;
import midend.llvmIr.value.instruction.cond.IrIcmpType;
import midend.llvmIr.value.instruction.cond.IrZext;
import midend.symbol.SymbolTable;

import java.util.ArrayList;

public class IrBasicBlockBuilder {
    private SymbolTable symbolTable;
    private Block block;
    private NameCnt nameCnt;
    private ArrayList<IrBasicBlock> basicBlocks;
    private ArrayList<IrBr> breaks;
    private ArrayList<IrBr> continues;
    private String funcName;

    public IrBasicBlockBuilder(String funcName, SymbolTable symbolTable, Block block, NameCnt nameCnt) {
        this.funcName = funcName;
        this.symbolTable = symbolTable;
        this.block = block;
        this.nameCnt = nameCnt;
        this.basicBlocks = new ArrayList<>();
        this.breaks = new ArrayList<>();
        this.continues = new ArrayList<>();
    }

    public ArrayList<IrBr> getBreaks() {
        return breaks;
    }

    public ArrayList<IrBr> getContinues() {
        return continues;
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
                            = new IrBasicBlockBuilder(this.funcName, symbolTable1, (Block) stmt, this.nameCnt);
                    this.basicBlocks.addAll(basicBlockBuilder.genIrBasicBlock());
                    this.breaks.addAll(basicBlockBuilder.getBreaks());
                    this.continues.addAll(basicBlockBuilder.getContinues());
                }
                else if (stmt instanceof StmtIf) {
                    //this.basicBlocks.addAll(genIrStmtIf((StmtIf) stmt));
                    genIrStmtIf((StmtIf) stmt);
                }
                else if (stmt instanceof StmtFor) {
                    genIrStmtFor((StmtFor) stmt);
                }
                i++; //!!!
            }
            //普通语句
            else {
                IrBasicBlock basicBlock = new IrBasicBlock(funcName + this.nameCnt.getCnt());
                for (; i < block.getBlockItems().size(); i++) {
                    BlockItem blockItem1 = block.getBlockItems().get(i);
                    Stmt stmt1 = blockItem1.getStmt();
                    if (stmt1 instanceof Block || stmt1 instanceof StmtIf || stmt1 instanceof StmtFor) {
                        break;
                    }
                    IrInstructionBuilder instructionBuilder
                            = new IrInstructionBuilder(this.symbolTable, blockItem1, this.nameCnt);
                    ArrayList<IrInstruction> instructions = instructionBuilder.genIrInstruction();
                    basicBlock.getInstructions().addAll(instructions);
                    this.breaks.addAll(instructionBuilder.getBreaks());
                    this.continues.addAll(instructionBuilder.getContinues());
                }
                this.basicBlocks.add(basicBlock);
            }
        }
        return basicBlocks;
    }

    public void genIrStmtFor(StmtFor stmtFor) {
        // 处理for头-执行forStmt1初始化语句 todo 可能有空缺
        ForStmt forStmt1 = stmtFor.getForStmt1();
        if (forStmt1 != null) {
            IrBasicBlock curBasicBlock = this.basicBlocks.get(this.basicBlocks.size() - 1); //当前基本块
            StmtAssign stmtAssign1 = new StmtAssign(forStmt1.getlVal(), forStmt1.getExp());
            BlockItem blockItem = new BlockItem(stmtAssign1);
            IrInstructionBuilder instructionBuilder
                    = new IrInstructionBuilder(this.symbolTable, blockItem, this.nameCnt);
            curBasicBlock.getInstructions().addAll(instructionBuilder.genIrInstruction());
        }
        // 处理cond
        String condLabel = String.valueOf(this.nameCnt.getCntOnly());
        ArrayList<ArrayList<IrBasicBlock>> blocks = new ArrayList<>();
        Cond cond = stmtFor.getCond();
        if (cond != null) {
            blocks = genCond(cond);
        }
        //gen for主体stmt
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        blockItems.add(new BlockItem(stmtFor.getStmt()));
        Block block1 = new Block(blockItems);
        IrBasicBlockBuilder irBasicBlockBuilder = new IrBasicBlockBuilder(this.funcName, this.symbolTable, block1, this.nameCnt);
        ArrayList<IrBasicBlock> irBasicBlocks = irBasicBlockBuilder.genIrBasicBlock(); //过程中会自动新建基本块
        this.basicBlocks.addAll(irBasicBlocks);
        ArrayList<IrBr> breaks = irBasicBlockBuilder.getBreaks();
        ArrayList<IrBr> continues = irBasicBlockBuilder.getContinues();
        // forStmt2增量语句
        String forStmt2Begin = String.valueOf(this.nameCnt.getCntOnly());
        IrBasicBlock forStmt2Block = new IrBasicBlock(funcName + this.nameCnt.getCnt());
        this.basicBlocks.add(forStmt2Block);
        ForStmt forStmt2 = stmtFor.getForStmt2();
        if (forStmt2 != null) {
            StmtAssign stmtAssign2 = new StmtAssign(forStmt2.getlVal(), forStmt2.getExp());
            BlockItem blockItem = new BlockItem(stmtAssign2);
            IrInstructionBuilder instructionBuilder
                    = new IrInstructionBuilder(this.symbolTable, blockItem, this.nameCnt);
            forStmt2Block.getInstructions().addAll(instructionBuilder.genIrInstruction());
        }
        // 末尾追加跳转到For头语句
        IrBr brIfEnd = new IrBr(funcName + condLabel);
        forStmt2Block.addInstruction(brIfEnd);
        String forEndLabel = String.valueOf(this.nameCnt.getCntOnly());
        if (cond != null) {
            // 对于整个cond的最后一条语句，若不为真，则要跳转到for语句后接的基本块
            ArrayList<IrBasicBlock> basicBlocks1 = blocks.get(blocks.size() - 1);
            IrBasicBlock basicBlock1 = basicBlocks1.get(basicBlocks1.size() - 1);
            IrInstruction instruction = basicBlock1.getInstructions().get(basicBlock1.getInstructions().size() - 1);
            IrBr irBr = (IrBr) instruction;
            irBr.setFalseLabel(funcName + forEndLabel);
        }
        for (IrBr breakBr : breaks) {
            breakBr.setLabel(funcName + forEndLabel);
        }
        for (IrBr continueBr : continues) {
            continueBr.setLabel(funcName + forStmt2Begin);
        }
        //IrBasicBlock irBasicBlock = new IrBasicBlock(funcName + this.nameCnt.getCnt());
        //this.basicBlocks.add(irBasicBlock); //防止之后没有block了 todo
    }

    public void genIrStmtIf(StmtIf stmtIf) {
        Cond cond = stmtIf.getCond();
        // cond分析
        ArrayList<ArrayList<IrBasicBlock>> blocks = genCond(cond);
        // 分析if-stmt
        Stmt ifStmt = stmtIf.getStmtIf();
        Stmt elseStmt = stmtIf.getStmtElse();
        //gen
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        blockItems.add(new BlockItem(ifStmt));
        Block block1 = new Block(blockItems);
        IrBasicBlockBuilder irBasicBlockBuilder = new IrBasicBlockBuilder(this.funcName, this.symbolTable, block1, this.nameCnt);
        ArrayList<IrBasicBlock> irBasicBlocks = irBasicBlockBuilder.genIrBasicBlock();
        this.basicBlocks.addAll(irBasicBlocks);
        this.breaks.addAll(irBasicBlockBuilder.getBreaks());
        this.continues.addAll(irBasicBlockBuilder.getContinues());
        IrBr brIfEnd = null;
        if (irBasicBlocks.size() != 0) { //todo 需要else吗？
            IrBasicBlock lastBlock = irBasicBlocks.get(irBasicBlocks.size() - 1);
            brIfEnd = new IrBr("#");
            lastBlock.addInstruction(brIfEnd);
        }
        // 分析else-stmt
        int elseBeginLabel = this.nameCnt.getCntOnly();
        if (elseStmt != null) {
            ArrayList<BlockItem> blockItems1 = new ArrayList<>();
            blockItems1.add(new BlockItem(elseStmt));
            Block block11 = new Block(blockItems1);
            IrBasicBlockBuilder irBasicBlockBuilder1 = new IrBasicBlockBuilder(this.funcName, this.symbolTable, block11, this.nameCnt);
            ArrayList<IrBasicBlock> irBasicBlocks1 = irBasicBlockBuilder1.genIrBasicBlock();
            this.basicBlocks.addAll(irBasicBlocks1);
            this.breaks.addAll(irBasicBlockBuilder1.getBreaks());
            this.continues.addAll(irBasicBlockBuilder1.getContinues());
        }
        // 对于整个cond的最后一条语句，若不为真，则要跳转到else
        ArrayList<IrBasicBlock> basicBlocks1 = blocks.get(blocks.size() - 1);
        IrBasicBlock basicBlock1 = basicBlocks1.get(basicBlocks1.size() - 1);
        IrInstruction instruction = basicBlock1.getInstructions().get(basicBlock1.getInstructions().size() - 1);
        IrBr irBr = (IrBr) instruction;
        irBr.setFalseLabel(funcName + elseBeginLabel);
        // 跳转到接下来的基本块
        if (brIfEnd != null) {
            brIfEnd.setLabel(funcName + this.nameCnt.getCntOnly());
        }
    }

    public ArrayList<ArrayList<IrBasicBlock>> genCond(Cond cond) {
        ArrayList<ArrayList<IrBasicBlock>> lAndBlocks = new ArrayList<>();
        ArrayList<LAndExp> lAndExps = cond.getlOrExp().getlAndExps();
        for (LAndExp lAndExp : lAndExps) {
            lAndBlocks.add(genLAndExp(lAndExp));
        }
        int num = this.nameCnt.getCntOnly(); // stmt1对应序号
        String LorExpEnd = String.valueOf(num);
        for (ArrayList<IrBasicBlock> blocks: lAndBlocks) {
            // 对于或语句中每一个与语句的最后一条eq语句，若true则证明整个与语句为真，可直接跳转到stmt1（即或语句末尾
            IrBasicBlock basicBlock = blocks.get(blocks.size() - 1);
            IrBr irBr = (IrBr) basicBlock.getInstructions().get(basicBlock.getInstructions().size() - 1);
            irBr.setTrueLabel(funcName + LorExpEnd);
        }
        return lAndBlocks;
    }

    public ArrayList<IrBasicBlock> genLAndExp(LAndExp lAndExp) {
        ArrayList<IrBasicBlock> eqExpBlocks = new ArrayList<>();
        ArrayList<EqExp> eqExps = lAndExp.getEqExps();
        for (EqExp eqExp : eqExps) {
            eqExpBlocks.add(genEqExp(eqExp));
        }
        int num = this.nameCnt.getCntOnly();
        String lAndEnd = String.valueOf(num);
        for (IrBasicBlock basicBlock : eqExpBlocks) {
            IrInstruction instruction = basicBlock.getInstructions().get(basicBlock.getInstructions().size() - 1); // 应该是br语句
            IrBr irBr = (IrBr) instruction;
            irBr.setFalseLabel(funcName + lAndEnd);
        }
        return eqExpBlocks;
    }

    public IrBasicBlock genEqExp(EqExp eqExp) {
        // 生成基本块
        IrBasicBlock curBasicBlock = new IrBasicBlock(funcName + this.nameCnt.getCnt());
        // 解析EqExp语句
        ArrayList<RelExp> relExps = eqExp.getRelExps();
        ArrayList<Token> signs = eqExp.getSigns();
        IrValue op1 = genRelExp(relExps.get(0), curBasicBlock);
        if (signs.size() == 0 && eqExp.getRelExps().get(0).getAddExps().size() == 1) {
            String name = "%" + this.nameCnt.getCnt();
            IrValue value = new IrValue("0");
            IrIcmp icmp = new IrIcmp(name, IrIcmpType.ne, new IrIntType(32), value, op1);
            curBasicBlock.addInstruction(icmp);
            IrBr irBr = new IrBr(icmp, funcName + this.nameCnt.getCntOnly(), "#");
            curBasicBlock.addInstruction(irBr);
            this.basicBlocks.add(curBasicBlock);
            return curBasicBlock;
        }
        for (int i = 0; i < signs.size(); i++) {
            IrValue op2 = genRelExp(relExps.get(i + 1), curBasicBlock);
            Token sign = signs.get(i);
            IrIcmp icmp = null;
            String name = "%" + this.nameCnt.getCnt();
            if (sign.getLexType() == LexType.EQL) {
                icmp = new IrIcmp(name, IrIcmpType.eq, new IrIntType(32), op1, op2);
            }
            else if (sign.getLexType() == LexType.NEQ) {
                icmp = new IrIcmp(name, IrIcmpType.ne, new IrIntType(32), op1, op2);
            }
            curBasicBlock.addInstruction(icmp);
            op1 = icmp;
        }
        // 生成Br语句，加入当前基本块
        // 正确则跳转下一与语句，否则跳到下一或语句
        IrBr irBr = new IrBr(op1, funcName + this.nameCnt.getCntOnly(), "#");
        curBasicBlock.addInstruction(irBr);
        this.basicBlocks.add(curBasicBlock);
        return curBasicBlock;
    }

    public IrValue genRelExp(RelExp relExp, IrBasicBlock curBasicBlock) {
        ArrayList<AddExp> addExps = relExp.getAddExps();
        ArrayList<Token> signs = relExp.getSigns();
        IrValue op1;
        IrValue op2;
        AddExp addExp = addExps.get(0);
        IrInstructionBuilder instructionBuilder = new IrInstructionBuilder(this.symbolTable, this.nameCnt);
        instructionBuilder.genAddExp(addExp);
        curBasicBlock.getInstructions().addAll(instructionBuilder.getIrInstructions()); // 过程中产生的语句加入基本块
        op1 = instructionBuilder.getAddExpRet();
        for (int i = 1; i < addExps.size(); i++) {
            addExp = addExps.get(i);
            instructionBuilder = new IrInstructionBuilder(this.symbolTable, this.nameCnt);
            instructionBuilder.genAddExp(addExp);
            curBasicBlock.getInstructions().addAll(instructionBuilder.getIrInstructions());
            op2 = instructionBuilder.getAddExpRet();
            String name = "%" + this.nameCnt.getCnt();
            IrValueType valueType = new IrIntType(32);
            IrIcmp icmp = null;
            if (signs.get(i - 1).getLexType() == LexType.LSS) { // <
                icmp = new IrIcmp(name, IrIcmpType.slt, valueType, op1, op2);
            }
            else if (signs.get(i - 1).getLexType() == LexType.LEQ) { // <=
                icmp = new IrIcmp(name, IrIcmpType.sle, valueType, op1, op2);
            }
            else if (signs.get(i - 1).getLexType() == LexType.GRE) { // >
                icmp = new IrIcmp(name, IrIcmpType.sgt, valueType, op1, op2);
            }
            else if (signs.get(i - 1).getLexType() == LexType.GEQ) { // >=
                icmp = new IrIcmp(name, IrIcmpType.sge, valueType, op1, op2);
            }
            curBasicBlock.addInstruction(icmp);
            if (i != addExps.size() - 1) {
                String name1 = "%" + this.nameCnt.getCnt();
                IrZext irZext = new IrZext(name1, icmp, new IrIntType(32));
                op1 = irZext;
                curBasicBlock.addInstruction(irZext);
            }
            else {
                op1 = icmp;
            }
        }
        return op1;
    }

}
