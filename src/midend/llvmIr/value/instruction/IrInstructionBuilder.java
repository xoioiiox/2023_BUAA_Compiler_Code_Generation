package midend.llvmIr.value.instruction;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.declaration.*;
import frontend.parser.expression.*;
import frontend.parser.statement.*;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.*;
import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.function.NameCnt;
import midend.llvmIr.value.instruction.binary.IrBinaryInst;
import midend.llvmIr.value.instruction.binary.IrBinaryType;
import midend.llvmIr.value.instruction.cond.IrBr;
import midend.llvmIr.value.instruction.cond.IrIcmp;
import midend.llvmIr.value.instruction.cond.IrIcmpType;
import midend.llvmIr.value.instruction.memory.IrAlloca;
import midend.llvmIr.value.instruction.memory.IrGetElementPtr;
import midend.llvmIr.value.instruction.memory.IrLoad;
import midend.llvmIr.value.instruction.memory.IrStore;
import midend.llvmIr.value.instruction.terminator.IrCall;
import midend.llvmIr.value.instruction.terminator.IrRet;
import midend.symbol.*;

import java.util.ArrayList;

public class IrInstructionBuilder {
    private SymbolTable symbolTable;
    private BlockItem blockItem;
    private ArrayList<IrInstruction> irInstructions;
    private ArrayList<IrBr> breaks;
    private ArrayList<IrBr> continues;
    private NameCnt nameCnt;
    private IrValue addExpRet;

    public IrInstructionBuilder(SymbolTable symbolTable, BlockItem blockItem, NameCnt nameCnt) {
        this.symbolTable = symbolTable;
        this.blockItem = blockItem;
        this.nameCnt = nameCnt;
        this.irInstructions = new ArrayList<>();
        this.breaks = new ArrayList<>();
        this.continues = new ArrayList<>();
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

    public ArrayList<IrBr> getBreaks() {
        return breaks;
    }

    public ArrayList<IrBr> getContinues() {
        return continues;
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
                genConstDecl((ConstDecl) decl);
            }
            else if (decl instanceof VarDecl) {
                genVarDecl((VarDecl) decl);
            }
        }
        return irInstructions;
    }

    public void genConstDecl(ConstDecl constDecl) {
        for (ConstDef constDef : constDecl.getConstDefs()) {
            String name = "%" + this.nameCnt.getCnt();
            int dim = constDef.getConstExps().size();
            if (dim == 0) {
                /*---生成IrValue---*/
                IrValueType valueType = new IrIntType(32);
                IrValue value = new IrValue(name, valueType);
                /*---内存分配指令---*/
                IrAlloca irAlloca = new IrAlloca(valueType, value);
                this.irInstructions.add(irAlloca);
                if (constDef.getConstInitVal() != null) {
                    Exp exp = new Exp(constDef.getConstInitVal().getConstExp().getAddExp());
                    IrValue value1 = genExp(exp);
                    IrStore irStore = new IrStore(value, value1); //todo right?
                    this.irInstructions.add(irStore);
                }
                /*---填入符号表---*/
                Symbol symbol = new SymbolVar(constDef.getIdent().getVal(), constDef.getDimension(), value);
                this.symbolTable.addSymbol(symbol);
            }
            else if (dim == 1) {
                /*---定义类型和值，并分配内存---*/
                int eleNum = constDef.getConstExps().get(0).calculate(symbolTable);
                IrValueType valueType = new IrArrayType(new IrIntType(32), eleNum);
                IrValue value = new IrValue(name, valueType);
                IrAlloca irAlloca = new IrAlloca(valueType, value);
                this.irInstructions.add(irAlloca);
                /*---赋值前先获取全部初始值---*/
                ArrayList<IrValue> initVal = new ArrayList<>();
                if (constDef.getConstInitVal() != null) {
                    for (ConstInitVal constInitVal : constDef.getConstInitVal().getConstInitVals()) {
                        Exp exp = new Exp(constInitVal.getConstExp().getAddExp());
                        IrValue value1 = genExp(exp);
                        IrStore irStore = new IrStore(value, value1);
                        this.irInstructions.add(irStore);
                        initVal.add(value1);
                    }
                }
                /*---获取数组地址，赋值---*/
                for (int i = 0; i < eleNum && i < initVal.size(); i++) {
                    String name1 = "%" + this.nameCnt.getCnt();
                    IrGetElementPtr getElementPtr = new IrGetElementPtr
                            (valueType, name1, valueType, value, new IrIntType(32), new IrValue(String.valueOf(i)), false);
                    this.irInstructions.add(getElementPtr);
                    IrStore irStore = new IrStore(getElementPtr, initVal.get(i));
                    this.irInstructions.add(irStore);

                }
                /*---填入符号表---*/
                Symbol symbol = new SymbolCon(constDef.getIdent().getVal(), constDef.getDimension(), value);
                this.symbolTable.addSymbol(symbol);
            }
            else {
                /*---定义类型和值，并分配内存---*/
                int eleNum1 = constDef.getConstExps().get(0).calculate(symbolTable);
                int eleNum2 = constDef.getConstExps().get(1).calculate(symbolTable);
                IrArrayType irArrayType = new IrArrayType(new IrIntType(32), eleNum2);
                IrValueType valueType = new IrArrayType(irArrayType, eleNum1, eleNum2);
                IrValue value = new IrValue(name, valueType);
                IrAlloca irAlloca = new IrAlloca(valueType, value);
                this.irInstructions.add(irAlloca);
                /*---赋值前先获取全部初始值---*/
                ArrayList<ArrayList<IrValue>> initVal = new ArrayList<>();
                if (constDef.getConstInitVal() != null) {
                    for (ConstInitVal constInitVal : constDef.getConstInitVal().getConstInitVals()) {
                        ArrayList<IrValue> initVal_ = new ArrayList<>();
                        for (ConstInitVal constInitVal2 : constInitVal.getConstInitVals()) {
                            Exp exp = new Exp(constInitVal2.getConstExp().getAddExp());
                            IrValue value1 = genExp(exp);
                            IrStore irStore = new IrStore(value, value1);
                            this.irInstructions.add(irStore);
                            initVal_.add(value1);
                        }
                        initVal.add(initVal_);
                    }
                }
                /*---获取数组地址，赋值---*/
                for (int i = 0; i < eleNum1 && i < initVal.size(); i++) {
                    for (int j = 0; j < eleNum2 && j < initVal.get(i).size(); j++) {
                        String name1 = "%" + this.nameCnt.getCnt();
                        IrGetElementPtr getElementPtr = new IrGetElementPtr
                                (valueType, name1, valueType, value, new IrIntType(32),
                                        new IrValue(String.valueOf(i)), new IrValue(String.valueOf(j)), false);
                        this.irInstructions.add(getElementPtr);
                        IrStore irStore = new IrStore(getElementPtr, initVal.get(i).get(j));
                        this.irInstructions.add(irStore);
                    }
                }
                /*---填入符号表---*/
                Symbol symbol = new SymbolCon(constDef.getIdent().getVal(), constDef.getDimension(), value);
                this.symbolTable.addSymbol(symbol);
            }
        }
    }

    public void genVarDecl(VarDecl varDecl) {
        for (VarDef varDef : varDecl.getVarDefs()) {
            String name = "%" + this.nameCnt.getCnt();
            int dim = varDef.getConstExps().size();
            if (dim == 0) {
                /*---生成IrValue---*/
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
            else if (dim == 1) {
                /*---定义类型和值，并分配内存---*/
                int eleNum = varDef.getConstExps().get(0).calculate(symbolTable);
                IrValueType valueType = new IrArrayType(new IrIntType(32), eleNum);
                IrValue value = new IrValue(name, valueType);
                IrAlloca irAlloca = new IrAlloca(valueType, value);
                this.irInstructions.add(irAlloca);
                /*---赋值前先获取全部初始值---*/
                ArrayList<IrValue> initVal = new ArrayList<>();
                if (varDef.getInitVal() != null) {
                    for (InitVal initVal1 : varDef.getInitVal().getInitVals()) {
                        Exp exp = new Exp(initVal1.getExp().getAddExp());
                        IrValue value1 = genExp(exp);
                        IrStore irStore = new IrStore(value, value1);
                        this.irInstructions.add(irStore);
                        initVal.add(value1);
                    }
                }
                /*---获取数组地址，赋值---*/
                for (int i = 0; i < eleNum && i < initVal.size(); i++) {
                    String name1 = "%" + this.nameCnt.getCnt();
                    IrGetElementPtr getElementPtr = new IrGetElementPtr
                            (valueType, name1, valueType, value, new IrIntType(32), new IrValue(String.valueOf(i)), false);
                    this.irInstructions.add(getElementPtr);
                    IrStore irStore = new IrStore(getElementPtr, initVal.get(i));
                    this.irInstructions.add(irStore);

                }
                /*---填入符号表---*/
                Symbol symbol = new SymbolVar(varDef.getIdent().getVal(), varDef.getDimension(), value);
                this.symbolTable.addSymbol(symbol);
            }
            else {
                /*---定义类型和值，并分配内存---*/
                int eleNum1 = varDef.getConstExps().get(0).calculate(symbolTable);
                int eleNum2 = varDef.getConstExps().get(1).calculate(symbolTable);
                IrArrayType irArrayType = new IrArrayType(new IrIntType(32), eleNum2);
                IrValueType valueType = new IrArrayType(irArrayType, eleNum1, eleNum2);
                IrValue value = new IrValue(name, valueType);
                IrAlloca irAlloca = new IrAlloca(valueType, value);
                this.irInstructions.add(irAlloca);
                /*---赋值前先获取全部初始值---*/
                ArrayList<ArrayList<IrValue>> initVal = new ArrayList<>();
                if (varDef.getInitVal() != null) {
                    for (InitVal initVal1 : varDef.getInitVal().getInitVals()) {
                        ArrayList<IrValue> initVal_ = new ArrayList<>();
                        for (InitVal initVal2 : initVal1.getInitVals()) {
                            Exp exp = new Exp(initVal2.getExp().getAddExp());
                            IrValue value1 = genExp(exp);
                            IrStore irStore = new IrStore(value, value1);
                            this.irInstructions.add(irStore);
                            initVal_.add(value1);
                        }
                        initVal.add(initVal_);
                    }
                }
                /*---获取数组地址，赋值---*/
                for (int i = 0; i < eleNum1 && i < initVal.size(); i++) {
                    for (int j = 0; j < eleNum2 && j < initVal.get(i).size(); j++) {
                        String name1 = "%" + this.nameCnt.getCnt();
                        IrGetElementPtr getElementPtr = new IrGetElementPtr
                                (valueType, name1, valueType, value, new IrIntType(32),
                                        new IrValue(String.valueOf(i)), new IrValue(String.valueOf(j)), false);
                        this.irInstructions.add(getElementPtr);
                        IrStore irStore = new IrStore(getElementPtr, initVal.get(i).get(j));
                        this.irInstructions.add(irStore);
                    }
                }
                /*---填入符号表---*/
                Symbol symbol = new SymbolVar(varDef.getIdent().getVal(), varDef.getDimension(), value);
                this.symbolTable.addSymbol(symbol);
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
        IrBr irBr = new IrBr("#");
        this.irInstructions.add(irBr);
        this.breaks.add(irBr);
    }

    public void genStmtContinue(StmtContinue stmtContinue) {
        IrBr irBr = new IrBr("#");
        this.irInstructions.add(irBr);
        this.continues.add(irBr);
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
        // todo a[2][3] 但是引用a[1]
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getVal());
        IrValue value_ = symbol.getValue();
        IrValueType type = value_.getValueType();
        if (lVal.getExps().size() == 0) {
            if (isLeftOp) {
                return value_;
            } else {
                if (!value_.getName().contains("@")
                        && !value_.getName().contains("%")) { //number
                    return value_;
                }
                else {
                    if (type instanceof IrArrayType) {
                        type = new IrPointerType(((IrArrayType) type).getElementType());
                    }
                    IrLoad irLoad = new IrLoad(type, value_);
                    irLoad.setName("%" + nameCnt.getCnt());
                    this.irInstructions.add(irLoad);
                    return irLoad;
                }
            }
        }
        else if (lVal.getExps().size() == 1) {
            IrValue value1 = genExp(lVal.getExps().get(0)); // 存储lVal的index对应的IrValue
            IrGetElementPtr getElementPtr;
            if (type instanceof IrArrayType) { //todo 类型？或许可以统一吗？
                String name = "%" + this.nameCnt.getCnt();
                getElementPtr = new IrGetElementPtr(((IrArrayType) type).getElementType(), name,
                        type, value_, new IrIntType(32), value1, false);
            }
            else {
                // 先load降维
                IrLoad irLoad = new IrLoad(type, value_);
                irLoad.setName("%" + this.nameCnt.getCnt());
                this.irInstructions.add(irLoad);
                String name = "%" + this.nameCnt.getCnt();
                getElementPtr = new IrGetElementPtr(((IrPointerType)type).getInnerType(), name,
                        irLoad.getValueType(), irLoad, new IrIntType(32), value1, true);
            }
            this.irInstructions.add(getElementPtr);
            if (isLeftOp) {
                return getElementPtr;
            }
            else {
                IrValueType valueType = getElementPtr.getValueType();
                if (valueType instanceof IrArrayType) {
                    valueType = new IrPointerType(((IrArrayType) valueType).getElementType());
                }
                IrLoad irLoad = new IrLoad(valueType, getElementPtr);
                irLoad.setName("%" + nameCnt.getCnt());
                this.irInstructions.add(irLoad); //todo 需要load吗？
                return irLoad;
            }
        }
        else if (lVal.getExps().size() == 2) {
            IrValue value1 = genExp(lVal.getExps().get(0)); //[]中的
            IrValue value2 = genExp(lVal.getExps().get(1));
            IrGetElementPtr getElementPtr;
            if (value_.getValueType() instanceof IrArrayType) { //todo 类型？或许可以统一吗？
                String name = "%" + this.nameCnt.getCnt(); // 获取元素指针存到此处
                getElementPtr = new IrGetElementPtr( //一定是int32 todo
                        new IrIntType(32), name, value_.getValueType(), value_
                                , new IrIntType(32), value1, value2, false);
            }
            else {
                // 先load降维
                IrLoad irLoad = new IrLoad(value_.getValueType(), value_);
                irLoad.setName("%" + this.nameCnt.getCnt());
                this.irInstructions.add(irLoad);
                String name = "%" + this.nameCnt.getCnt(); // 获取元素指针存到此处
                getElementPtr = new IrGetElementPtr(((IrPointerType)type).getInnerType(), name,
                        irLoad.getValueType(), irLoad, new IrIntType(32), value1, true);
                this.irInstructions.add(getElementPtr);
                getElementPtr = new IrGetElementPtr(new IrIntType(32), "%" + this.nameCnt.getCnt(),
                                value_.getValueType(), getElementPtr, new IrIntType(32),
                                new IrValue("0"), value2,true);
            }
            this.irInstructions.add(getElementPtr);
            if (isLeftOp) {
                return getElementPtr;
            }
            else {
                IrValueType valueType = getElementPtr.getValueType();
                if (valueType instanceof IrArrayType) {
                    valueType = new IrPointerType(((IrArrayType) valueType).getElementType());
                }
                IrLoad irLoad = new IrLoad(valueType, getElementPtr);
                irLoad.setName("%" + nameCnt.getCnt());
                this.irInstructions.add(irLoad); //todo 需要load吗？
                return irLoad;
            }
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
                irBinaryInst = new IrBinaryInst(name, valueType, IrBinaryType.srem, op1, op2);
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
        else if (unaryExp.getUnaryOp().getOp().getLexType() == LexType.NOT) {
            IrValue value = genUnaryExp(unaryExp.getUnaryExp());
            IrValue value1 = new IrValue("0", new IrIntType(32));
            IrIcmp icmp = new IrIcmp("%" + this.nameCnt.getCnt(), IrIcmpType.eq, new IrIntType(32), value, value1);
            this.irInstructions.add(icmp);
            return icmp;
        }
        return null;
    }
}
