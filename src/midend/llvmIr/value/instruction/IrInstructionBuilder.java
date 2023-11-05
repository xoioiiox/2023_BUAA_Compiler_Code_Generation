package midend.llvmIr.value.instruction;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.declaration.ConstDecl;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.VarDecl;
import frontend.parser.declaration.VarDef;
import frontend.parser.expression.*;
import frontend.parser.statement.*;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.type.IrVoidType;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.function.NameCnt;
import midend.llvmIr.value.instruction.binary.IrBinaryInst;
import midend.llvmIr.value.instruction.binary.IrBinaryType;
import midend.llvmIr.value.instruction.memory.IrAlloca;
import midend.llvmIr.value.instruction.memory.IrLoad;
import midend.llvmIr.value.instruction.memory.IrStore;
import midend.llvmIr.value.instruction.terminator.IrCall;
import midend.llvmIr.value.instruction.terminator.IrRet;
import midend.symbol.Symbol;
import midend.symbol.SymbolFunc;
import midend.symbol.SymbolTable;
import midend.symbol.SymbolVar;

import java.util.ArrayList;

public class IrInstructionBuilder {
    private SymbolTable symbolTable;
    private BlockItem blockItem;
    private ArrayList<IrInstruction> irInstructions;
    private NameCnt nameCnt;
    private IrValue addExpRet;

    public IrInstructionBuilder(SymbolTable symbolTable, BlockItem blockItem, NameCnt nameCnt) {
        this.symbolTable = symbolTable;
        this.blockItem = blockItem;
        this.nameCnt = nameCnt;
        this.irInstructions = new ArrayList<>();
    }

    // StmtIf
    public IrInstructionBuilder(SymbolTable symbolTable, NameCnt nameCnt) {
        this.symbolTable = symbolTable;
        this.nameCnt = nameCnt;
        this.irInstructions = new ArrayList<>();
    }

    public ArrayList<IrInstruction> getIrInstructions() {
        return irInstructions;
    }

    public ArrayList<IrInstruction> genIrInstruction() {
        if (this.blockItem.getStmt() != null) {
            Stmt stmt = this.blockItem.getStmt();
            if (stmt instanceof StmtAssign) {
                genStmtAssign((StmtAssign) stmt);
            }
            else if (stmt instanceof StmtBreak) {
                genStmtBreak((StmtBreak) stmt);
            }
            else if (stmt instanceof StmtContinue) {
                genStmtContinue((StmtContinue) stmt);
            }
            else if (stmt instanceof StmtExp) {
                genExp(((StmtExp) stmt).getExp());
            }
            else if (stmt instanceof StmtGetInt) {
                genStmtGetInt((StmtGetInt) stmt);
            }
            else if (stmt instanceof StmtPrintf) {
                genStmtPrintf((StmtPrintf) stmt);
            }
            else if (stmt instanceof StmtReturn) {
                genStmtReturn((StmtReturn) stmt);
            }
        }
        else if (this.blockItem.getDecl() != null) {
            Decl decl = this.blockItem.getDecl();
            if (decl instanceof ConstDecl) {

            }
            else if (decl instanceof VarDecl) {
                genVarDecl((VarDecl) decl);
            }
        }
        return irInstructions;
    }

    public void genVarDecl(VarDecl varDecl) {
        for (VarDef varDef : varDecl.getVarDefs()) {
            if (varDef.getConstExps().size() == 0) {
                /*---生成IrValue---*/
                String name = "%" + this.nameCnt.getCnt();
                IrValueType valueType = new IrIntType(32);
                IrValue value = new IrValue(name, valueType);
                /*---内存分配指令---*/
                IrAlloca irAlloca = new IrAlloca(valueType, value);
                this.irInstructions.add(irAlloca);
                if (varDef.getInitVal() != null) {
                    IrValue value1 = genExp(varDef.getInitVal().getExp());
                    IrStore irStore = new IrStore(value, value1); //todo right?
                    this.irInstructions.add(irStore);
                }
                /*---填入符号表---*/
                Symbol symbol = new SymbolVar(varDef.getIdent().getVal(), varDef.getDimension(), value);
                this.symbolTable.addSymbol(symbol);
            }
            else {
                //todo
            }
        }
    }

    public void genStmtAssign(StmtAssign stmtAssign) {
        IrValue leftOp = genLVal(stmtAssign.getlVal(), true);
        IrValue rightOp = genExp(stmtAssign.getExp());
        IrStore irStore = new IrStore(leftOp, rightOp);
        this.irInstructions.add(irStore);
    }

    public void genStmtBreak(StmtBreak stmtBreak) {

    }

    public void genStmtContinue(StmtContinue stmtContinue) {

    }

    public void genStmtReturn(StmtReturn stmtReturn) {
        IrRet irRet;
        if (stmtReturn.getExp() != null) { //有返回值
            IrValueType valueType = new IrIntType(32);
            IrValue value = genExp(stmtReturn.getExp());
            irRet = new IrRet(value.getName(), valueType, true);
        }
        else {
            IrValueType valueType = new IrVoidType();
            irRet = new IrRet(valueType, false);
        }
        this.irInstructions.add(irRet);
    }

    /**
     * a = getint()
     * %3 = call i32 @getint()
     * store i32 %3, i32* %1
     */
    public void genStmtGetInt(StmtGetInt stmtGetInt) {
        IrValue value = genLVal(stmtGetInt.getlVal(), true);
        String name = "%" + this.nameCnt.getCnt();
        IrCall irCall = new IrCall(name, new IrIntType(32), "@getint");
        this.irInstructions.add(irCall);
        IrStore irStore = new IrStore(value, irCall);
        this.irInstructions.add(irStore);
    }


    public void genStmtPrintf(StmtPrintf stmtPrintf) {
        String s = stmtPrintf.getFormatString().getVal();
        ArrayList<Exp> exps = stmtPrintf.getExps();
        int pos = 0;
        for (int i = 1; i < s.length() - 1; i++) {
            IrCall irCall;
            if (s.charAt(i) == '%') {
                IrValue value = genExp(exps.get(pos));
                pos++;
                i++;
                irCall = new IrCall(new IrVoidType(), "@putint", value);
            }
            else if (s.charAt(i) == '\\') {
                i++;
                irCall = new IrCall(new IrVoidType(), "@putch", '\n');
            }
            else {
                irCall = new IrCall(new IrVoidType(), "@putch", s.charAt(i));
            }
            this.irInstructions.add(irCall);
        }
    }


    public IrValue genLVal(LVal lVal, boolean isLeftOp) {
        IrValue value = null;
        if (lVal.getExps().size() == 0) {
            Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getVal());
            if (isLeftOp) {
                return symbol.getValue();
            } else {
                if (!symbol.getValue().getName().contains("@")
                        && !symbol.getValue().getName().contains("%")) { //number
                    return symbol.getValue();
                } //todo param
                else {
                    IrValueType valueType = new IrIntType(32);
                    IrLoad irLoad = new IrLoad(valueType, symbol.getValue());
                    irLoad.setName("%" + nameCnt.getCnt());
                    this.irInstructions.add(irLoad);
                    return irLoad;
                }
            }
        }
        else {
            //todo
        }
        return value;
    }

    /**
     * 考虑是否需要将临时变量存入符号表
     */

    /*---返回表达式结果存储的Value---*/
    public IrValue genExp(Exp exp) {
        return genAddExp(exp.getAddExp());
    }

    public IrValue genAddExp(AddExp addExp) {
        ArrayList<Token> signs = addExp.getSigns();
        ArrayList<MulExp> mulExps = addExp.getMulExps();
        MulExp firstMulExp = mulExps.get(0);
        IrValue op1 = genMulExp(firstMulExp);
        IrValueType valueType = new IrIntType(32);
        for (int i = 0; i < signs.size(); i++) {
            IrValue op2 = genMulExp(mulExps.get(i + 1));
            String name = "%" + this.nameCnt.getCnt();
            IrBinaryInst irBinaryInst = null;
            if (signs.get(i).getLexType() == LexType.PLUS) {
                irBinaryInst = new IrBinaryInst(name, valueType, IrBinaryType.add, op1, op2);
            }
            else if (signs.get(i).getLexType() == LexType.MINU) {
                irBinaryInst = new IrBinaryInst(name, valueType, IrBinaryType.sub, op1, op2);
            }
            this.irInstructions.add(irBinaryInst);
            op1 = irBinaryInst;
        }
        this.addExpRet = op1;
        return op1;
    }

    public IrValue getAddExpRet() {
        return addExpRet;
    }

    public IrValue genMulExp(MulExp mulExp) {
        ArrayList<Token> signs = mulExp.getSigns();
        ArrayList<UnaryExp> unaryExps = mulExp.getUnaryExps();
        UnaryExp firstUnaryExp = unaryExps.get(0);
        IrValue op1 = genUnaryExp(firstUnaryExp);
        IrValueType valueType = new IrIntType(32);
        for (int i = 0; i < signs.size(); i++) {
            IrValue op2 = genUnaryExp(unaryExps.get(i + 1));
            String name = "%" + this.nameCnt.getCnt();
            IrBinaryInst irBinaryInst = null;
            if (signs.get(i).getLexType() == LexType.MULT) {
                irBinaryInst = new IrBinaryInst(name, valueType, IrBinaryType.mul, op1, op2);
            }
            else if (signs.get(i).getLexType() == LexType.DIV) {
                irBinaryInst = new IrBinaryInst(name, valueType, IrBinaryType.sdiv, op1, op2);
            }
            else if (signs.get(i).getLexType() == LexType.MOD) {
                irBinaryInst = new IrBinaryInst(name, valueType, IrBinaryType.mod, op1, op2);
            }
            this.irInstructions.add(irBinaryInst);
            op1 = irBinaryInst;
        }
        return op1;
    }

    public IrValue genUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.getPrimaryExp() != null) {
            return genPrimaryExp(unaryExp.getPrimaryExp());
        }
        else if (unaryExp.getIdent() != null) {
            return genUnaryExpFunc(unaryExp);
        }
        else if (unaryExp.getUnaryOp() != null) {
            return genUnaryExpOp(unaryExp);
        }
        return null;
    }

    public IrValue genPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.getExp() != null) {
            return genExp(primaryExp.getExp());
        }
        else if (primaryExp.getlVal() != null) {
            return genLVal(primaryExp.getlVal(), false);
        }
        else if (primaryExp.getNumber() != null) {
            IrValueType valueType = new IrIntType(32);
            return new IrValue(primaryExp.getNumber().getIntConst().getVal(), valueType);
        }
        return null;
    }

    /**
     * aaa(a, b)
     * %5 = load i32, i32* %2
     * %6 = load i32, i32* %1
     * %7 = call i32 @aaa(i32 %5, i32 %6)
     */
    public IrValue genUnaryExpFunc(UnaryExp unaryExp) {
        ArrayList<IrValue> params = new ArrayList<>();
        SymbolFunc symbol = (SymbolFunc) symbolTable.getSymbol(unaryExp.getIdent().getVal());
        IrFunction function = (IrFunction) symbol.getValue(); //todo
        if (unaryExp.getFuncRParams() != null) {
            for (Exp exp : unaryExp.getFuncRParams().getExps()) {
                params.add(genExp(exp));
            }
        }
        IrCall irCall;
        IrValueType valueType;
        if (symbol.getReType() == 0) {
            valueType = new IrIntType(32);
            irCall = new IrCall("%" + this.nameCnt.getCnt(), valueType, function, params);
        }
        else {
            valueType = new IrVoidType();
            irCall = new IrCall(valueType, function, params);
        }
        this.irInstructions.add(irCall);
        return irCall;
    }

    public IrValue genUnaryExpOp(UnaryExp unaryExp) {
        if (unaryExp.getUnaryOp().getOp().getLexType() == LexType.PLUS) {
            return genUnaryExp(unaryExp.getUnaryExp());
        }
        else if (unaryExp.getUnaryOp().getOp().getLexType() == LexType.MINU) {
            IrValueType valueType = new IrIntType(32);
            IrValue op1 = new IrValue("-1", valueType);
            IrValue op2 = genUnaryExp(unaryExp.getUnaryExp());
            IrBinaryInst irBinaryInst = new IrBinaryInst("%" + this.nameCnt.getCnt(),
                    valueType, IrBinaryType.mul, op1, op2);
            this.irInstructions.add(irBinaryInst);
            return irBinaryInst;
        }
        //todo
        return null;
    }
}
