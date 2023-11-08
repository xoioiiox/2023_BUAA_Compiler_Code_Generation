package backend.Insturction;

import backend.MipsReg;
import backend.MipsSymbolTable;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.*;
import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.instruction.IrInstruction;
import midend.llvmIr.value.instruction.binary.IrBinaryInst;
import midend.llvmIr.value.instruction.binary.IrBinaryType;
import midend.llvmIr.value.instruction.cond.IrBr;
import midend.llvmIr.value.instruction.cond.IrIcmp;
import midend.llvmIr.value.instruction.cond.IrIcmpType;
import midend.llvmIr.value.instruction.cond.IrZext;
import midend.llvmIr.value.instruction.memory.IrAlloca;
import midend.llvmIr.value.instruction.memory.IrGetElementPtr;
import midend.llvmIr.value.instruction.memory.IrLoad;
import midend.llvmIr.value.instruction.memory.IrStore;
import midend.llvmIr.value.instruction.terminator.IrCall;
import midend.llvmIr.value.instruction.terminator.IrRet;

import java.util.ArrayList;

public class MipsInstructionBuilder {
    private IrInstruction instruction;
    private ArrayList<MipsInstruction> mipsInstructions;
    private MipsSymbolTable symbolTable;
    private boolean isMainFunc;

    public MipsInstructionBuilder(IrInstruction instruction, MipsSymbolTable symbolTable, boolean isMainFunc) {
        this.instruction = instruction;
        this.symbolTable = symbolTable;
        this.mipsInstructions = new ArrayList<>();
        this.isMainFunc = isMainFunc;
    }


    public ArrayList<MipsInstruction> genMipsInstruction() {
        if (instruction instanceof IrAlloca) {
            genMipsAlloca();
        }
        else if (instruction instanceof IrBinaryInst) {
            genMipsBinaryInst();
        }
        else if (instruction instanceof IrBr) {
            genMipsBr();
        }
        else if (instruction instanceof IrCall) {
            genMipsCall();
        }
        else if (instruction instanceof IrGetElementPtr) {
            genMipsGetElementPtr();
        }
        else if (instruction instanceof IrIcmp) {
            genMipsIcmp();
        }
        else if (instruction instanceof IrLoad) {
            genMipsLoad();
        }
        else if (instruction instanceof IrRet) {
            genMipsRet();
        }
        else if (instruction instanceof IrStore) {
            genMipsStore();
        }
        else if (instruction instanceof IrZext) {
            //genMipsZext();
        }
        return this.mipsInstructions;
    }

    //%19 = icmp ne i32 0, %18
    public void genMipsIcmp() {
        IrIcmp icmp = (IrIcmp) this.instruction;
        IrValue value1 = icmp.getOperand(0);
        IrValue value2 = icmp.getOperand(1);
        if (isConst(value1.getName())) {
            Li li = new Li(new MipsReg(8), Integer.parseInt(value1.getName()));
            this.mipsInstructions.add(li);
        }
        else {
            int offset1 = this.symbolTable.getOffset(value1.getName());
            Lw lw = new Lw(new MipsReg(8), new MipsReg(30), offset1);
            this.mipsInstructions.add(lw);
        }
        if (isConst(value2.getName())) {
            Li li = new Li(new MipsReg(9), Integer.parseInt(value2.getName()));
            this.mipsInstructions.add(li);
        }
        else {
            int offset2 = this.symbolTable.getOffset(value2.getName());
            Lw lw = new Lw(new MipsReg(9), new MipsReg(30), offset2);
            this.mipsInstructions.add(lw);
        }
        SCmpType type = getType(icmp.getType());
        SCmp sCmp = new SCmp(type, new MipsReg(10), new MipsReg(8), new MipsReg(9));
        this.mipsInstructions.add(sCmp);
        int offset = this.symbolTable.getOffset(null);
        Sw sw = new Sw(new MipsReg(10), new MipsReg(30), offset);
        this.mipsInstructions.add(sw);
        this.symbolTable.getSymbolMap().put(icmp.getName(), offset);
    }

    public SCmpType getType(IrIcmpType irIcmpType) {
        SCmpType type = null;
        switch (irIcmpType) {
            case eq: type = SCmpType.seq; break;
            case ne: type = SCmpType.sne; break;
            case sge: type = SCmpType.sge; break;
            case sgt: type = SCmpType.sgt; break;
            case sle: type = SCmpType.sle; break;
            case slt: type = SCmpType.slt; break;
        }
        return type;
    }

    // b[][] 加载b[] todo
    public void genMipsGetElementPtr() {
        IrGetElementPtr getElementPtr = (IrGetElementPtr) this.instruction;
        String name = getElementPtr.getPtrVal().getName();
        /*------先获取数组首地址------*/
        if (isGlobal(name)) { //全局数组
            La la = new La(new MipsReg(8), name.substring(1));
            this.mipsInstructions.add(la);
        }
        else if (getElementPtr.isFParam() || getElementPtr.getPtrVal().getValueType() instanceof IrPointerType) { //形参
            int offset = symbolTable.getOffset(name);
            Lw lw = new Lw(new MipsReg(8), new MipsReg(30), offset);
            this.mipsInstructions.add(lw);
        }
        else { //局部数组 todo 出错！
            int offset = symbolTable.getOffset(name);
            Addi addi = new Addi(new MipsReg(8), new MipsReg(30), offset);
            this.mipsInstructions.add(addi);
        }
        /*------再计算总偏移量------*/
        if (getElementPtr.getIndex() != null) { //一维a[]->a 或二维b[][]->b[1]
            IrValue value = getElementPtr.getIndex();
            getIndex(value, new MipsReg(9)); // 存了index的寄存器
            IrValueType valueType = getElementPtr.getBaseType();
            IrValueType valueType1 = null;
            if (valueType instanceof IrArrayType) {
                valueType1 = ((IrArrayType) valueType).getElementType();
            }
            else if (valueType instanceof IrPointerType) {
                valueType1 = ((IrPointerType) valueType).getInnerType();
            }
            if (valueType1 instanceof IrArrayType) {
                int elementNum = ((IrArrayType) valueType1).getElementNum();
                Li li = new Li(new MipsReg(10), elementNum);
                this.mipsInstructions.add(li);
                Binary binary = new Binary(BinaryType.mul, new MipsReg(9), new MipsReg(9), new MipsReg(10));
                this.mipsInstructions.add(binary);
            }
        } else { //一定是二维
            int elementNum2 = 0;
            if (getElementPtr.getBaseType() instanceof IrArrayType) {
                IrArrayType irArrayType = (IrArrayType) getElementPtr.getBaseType();
                elementNum2 = irArrayType.getElementNum2();
            }
            else if (getElementPtr.getBaseType() instanceof IrPointerType) {
                IrPointerType irPointerType = (IrPointerType) getElementPtr.getBaseType();
                elementNum2 = ((IrArrayType)irPointerType.getInnerType()).getElementNum();
            }
            IrValue value1 = getElementPtr.getIndex1();
            IrValue value2 = getElementPtr.getIndex2();
            getIndex(value1, new MipsReg(9));
            getIndex(value2, new MipsReg(10));
            Li li = new Li(new MipsReg(11), elementNum2);
            this.mipsInstructions.add(li);
            Binary binary = new Binary(BinaryType.mul, new MipsReg(12), new MipsReg(9), new MipsReg(11));
            this.mipsInstructions.add(binary);
            Binary binary1 = new Binary(BinaryType.add, new MipsReg(9), new MipsReg(12), new MipsReg(10));
            this.mipsInstructions.add(binary1);
        }
        /*------计算绝对位置------*/
        Sll sll = new Sll(new MipsReg(9), new MipsReg(9), 2);
        this.mipsInstructions.add(sll);
        Binary binary = new Binary(BinaryType.add, new MipsReg(10), new MipsReg(8), new MipsReg(9));
        this.mipsInstructions.add(binary);
        int offset = this.symbolTable.getOffset(null);
        Sw sw = new Sw(new MipsReg(10), new MipsReg(30), offset);
        this.mipsInstructions.add(sw);
        //Lw lw = new Lw(new MipsReg(11), new MipsReg(10), 0);
        //this.mipsInstructions.add(lw);
        //Sw sw = new Sw(new MipsReg(11), new MipsReg(30), offset);
        //this.mipsInstructions.add(sw);
        symbolTable.getSymbolMap().put(getElementPtr.getName(), offset);
        symbolTable.addSpecialBase(getElementPtr.getName());
    }

    private void getIndex(IrValue value, MipsReg desReg) {
        if (isConst(value.getName())) {
            Li li = new Li(desReg, Integer.parseInt(value.getName()));
            this.mipsInstructions.add(li);
        }
        else {
            Lw lw = new Lw(desReg, new MipsReg(30),
                    symbolTable.getOffset(value.getName()));
            this.mipsInstructions.add(lw);
        }
    }

    public void genMipsAlloca() {
        IrAlloca irAlloca = (IrAlloca) this.instruction;
        IrValueType valueType = irAlloca.getValueType();
        int num = 0;
        if (valueType instanceof IrArrayType) {
            int dim = ((IrArrayType) valueType).getDim();
            if (dim == 1) {
                num = ((IrArrayType) valueType).getElementNum();
            }
            else if (dim == 2) {
                num = ((IrArrayType) valueType).getElementNum1() * ((IrArrayType) valueType).getElementNum2();
            }
        }
        // 加入符号表， 栈指针移动
        this.symbolTable.getSymbolMap().put(irAlloca.getValue().getName(), this.symbolTable.getOffset(null));
        this.symbolTable.changeOffset(4 * num);
    }

    public void genMipsBinaryInst() {
        IrBinaryInst irBinaryInst = (IrBinaryInst) this.instruction;
        /*------获取操作数(li lw)------*/
        String rdName = irBinaryInst.getName();
        String rsName = irBinaryInst.getOperand(0).getName();
        String rtName = irBinaryInst.getOperand(1).getName();
        if (isConst(rsName)) {
            Li li = new Li(new MipsReg(8), Integer.parseInt(rsName));
            this.mipsInstructions.add(li);
        }
        else {
            int offset = this.symbolTable.getOffset(rsName);
            Lw lw = new Lw(new MipsReg(8), new MipsReg(30), offset);
            this.mipsInstructions.add(lw);
        }
        if (isConst(rtName)) {
            Li li = new Li(new MipsReg(9), Integer.parseInt(rtName));
            this.mipsInstructions.add(li);
        }
        else {
            int offset = this.symbolTable.getOffset(rtName);
            Lw lw = new Lw(new MipsReg(9), new MipsReg(30), offset);
            this.mipsInstructions.add(lw);
        }
        /*------生成指令(add sub...)------*/
        BinaryType binaryType = getBinaryType(irBinaryInst.getType());
        if (binaryType == BinaryType.add || binaryType == BinaryType.sub || binaryType == BinaryType.mul) {
            Binary binary = new Binary(binaryType,
                    new MipsReg(10), new MipsReg(8), new MipsReg(9));
            this.mipsInstructions.add(binary);
        }
        else if (binaryType == BinaryType.div) {
            Div div = new Div(new MipsReg(8), new MipsReg(9));
            this.mipsInstructions.add(div);
            Mfhi_lo mflo = new Mfhi_lo(new MipsReg(10), false);
            this.mipsInstructions.add(mflo);
        }
        else if (binaryType == BinaryType.mod) {
            Div div = new Div(new MipsReg(8), new MipsReg(9));
            this.mipsInstructions.add(div);
            Mfhi_lo mfhi = new Mfhi_lo(new MipsReg(10), true);
            this.mipsInstructions.add(mfhi);
        }
        /*------存入结果(sw)------*/
        int rdOffset = this.symbolTable.getOffset(rdName);
        Sw sw = new Sw(new MipsReg(10), new MipsReg(30), rdOffset);
        this.mipsInstructions.add(sw);
        //todo 得到值加入符号表 (原本存在 & 原本不存在)
        this.symbolTable.getSymbolMap().put(rdName, rdOffset);
    }

    public BinaryType getBinaryType(IrBinaryType type) {
        BinaryType binaryType = null;
        switch(type) {
            case add: binaryType = BinaryType.add; break;
            case sub: binaryType = BinaryType.sub; break;
            case mul: binaryType = BinaryType.mul; break;
            case sdiv: binaryType = BinaryType.div; break;
            case srem: binaryType = BinaryType.mod; break;
            //todo
        }
        return binaryType;
    }

    //br i1 %16, label %20, label %17
    public void genMipsBr() {
        IrBr irBr = (IrBr) this.instruction;
        IrValue value = irBr.getCond();
        if (value != null) {
            int offset = this.symbolTable.getOffset(value.getName());
            Lw lw = new Lw(new MipsReg(8), new MipsReg(30), offset);
            this.mipsInstructions.add(lw);
            String trueLabel = irBr.getTrueLabel();
            String falseLabel = irBr.getFalseLabel();
            Beq beq = new Beq(new MipsReg(8), 1, trueLabel);
            this.mipsInstructions.add(beq);
            J j = new J(falseLabel);
            this.mipsInstructions.add(j);
        }
        else {
            J j = new J(irBr.getLabel());
            this.mipsInstructions.add(j);
        }
    }

    public void genMipsCall() {
        IrCall irCall = (IrCall) this.instruction;
        if (irCall.isPutCh()) {
            genMipsPutCh(irCall);
        }
        else if (irCall.isPutInt()) {
            genMipsPutInt(irCall);
        }
        else if (irCall.isGetInt()) {
            genMipsGetInt(irCall);
        }
        else {
            genMipsCallFunc(irCall);
        }
    }

    public void genMipsPutCh(IrCall irCall) {
        Li li = new Li(new MipsReg(2), 11);
        this.mipsInstructions.add(li);
        Li li1 = new Li(new MipsReg(4), irCall.getC());
        this.mipsInstructions.add(li1);
        Syscall syscall = new Syscall();
        this.mipsInstructions.add(syscall);
    }

    public void genMipsPutInt(IrCall irCall) {
        Li li = new Li(new MipsReg(2), 1);
        this.mipsInstructions.add(li);
        int offset = this.symbolTable.getOffset(irCall.getValue().getName());
        Lw lw = new Lw(new MipsReg(4), new MipsReg(30), offset);
        this.mipsInstructions.add(lw);
        Syscall syscall = new Syscall();
        this.mipsInstructions.add(syscall);
    }

    public void genMipsGetInt(IrCall irCall) {
        Li li = new Li(new MipsReg(2), 5);
        this.mipsInstructions.add(li);
        Syscall syscall = new Syscall();
        this.mipsInstructions.add(syscall);
        int offset_l = this.symbolTable.getOffset(null);
        this.symbolTable.getSymbolMap().put(irCall.getName(), offset_l);
        Sw sw = new Sw(new MipsReg(2), new MipsReg(30), offset_l);
        this.mipsInstructions.add(sw);
    }

    public void genMipsCallFunc(IrCall irCall) {
        // 保存$ra，防止覆盖
        Sw sw = new Sw(new MipsReg(31), new MipsReg(29), 0);
        this.mipsInstructions.add(sw);
        Addi addi = new Addi(new MipsReg(29), new MipsReg(29), -4);
        this.mipsInstructions.add(addi);
        // 获取fp指针当前偏移量
        int fpOffset = this.symbolTable.getSumOffset();
        // push函数参数
        IrFunction irFunction = (IrFunction) irCall.getOperand(0);
        int paramNum = ((IrFunctionType)irFunction.getValueType()).getParamTypes().size();
        for (int i = 0; i < paramNum; i++) {
            IrValue param = irCall.getOperand(i + 1);
            if (isConst(param.getName())) {
                Li li = new Li(new MipsReg(8), Integer.parseInt(param.getName()));
                this.mipsInstructions.add(li);
            }
            else {
                int offset = this.symbolTable.getOffset(param.getName());
                Lw lw = new Lw(new MipsReg(8), new MipsReg(30), offset);
                this.mipsInstructions.add(lw);
            }
            Sw sw1 = new Sw(new MipsReg(8), new MipsReg(30), this.symbolTable.getOffset(null));
            this.mipsInstructions.add(sw1);
        }
        // 调整fp指针位置
        Addi addi1 = new Addi(new MipsReg(30), new MipsReg(30), fpOffset);
        this.mipsInstructions.add(addi1);
        // 调用函数
        Jal jal = new Jal(irFunction.getName().substring(1));
        this.mipsInstructions.add(jal);
        // 结束调用，恢复现场
        Addi addi3 = new Addi(new MipsReg(30), new MipsReg(30), -1 * fpOffset);
        this.mipsInstructions.add(addi3);
        Addi addi2 = new Addi(new MipsReg(29), new MipsReg(29), 4);
        this.mipsInstructions.add(addi2);
        Lw lw = new Lw(new MipsReg(31), new MipsReg(29), 0);
        this.mipsInstructions.add(lw);
        if (irCall.isHasExpReturn()) {
            // 临时变量之前没加入过符号表
            int offset_l = this.symbolTable.getOffset(null);
            this.symbolTable.getSymbolMap().put(irCall.getName(), offset_l);
            Sw sw1 = new Sw(new MipsReg(2), new MipsReg(30), offset_l);
            this.mipsInstructions.add(sw1);
        }
    }

    /**
     * (%22 = mul i32 %20, %21)
     * => ret i32 %22
     * 需要区分主函数返回和调用函数返回
     * 都得将返回值存入$v0
     * 之后主函数 $v0 = 10 syscall
     * 其它函数 jr $ra
     */
    public void genMipsRet() {
        IrRet irRet = (IrRet) this.instruction;
        if (irRet.isHasReturnExp()) { //有返回值
            String expName = irRet.getName();
            if (isConst(expName)) {
                Li li = new Li(new MipsReg(2), Integer.parseInt(expName));
                this.mipsInstructions.add(li);
            }
            else {
                // 将返回值存入寄存器
                int offset = this.symbolTable.getOffset(irRet.getName());
                Lw lw = new Lw(new MipsReg(2), new MipsReg(30), offset);
                this.mipsInstructions.add(lw);
            }
            // 跳转语句
            if (isMainFunc) {
                Li li = new Li(new MipsReg(2), 10);
                Syscall syscall = new Syscall();
                this.mipsInstructions.add(li);
                this.mipsInstructions.add(syscall);
            } else {
                Jr jr = new Jr(new MipsReg(31));
                this.mipsInstructions.add(jr);
            }
        }
        else { //无返回值
            Jr jr = new Jr(new MipsReg(31));
            this.mipsInstructions.add(jr);
        }
    }

    // load左侧变量都是为了调用变量新建的临时变量
    public void genMipsLoad() {
        IrLoad irLoad = (IrLoad) this.instruction;
        String srcName = irLoad.getValue().getName();
        int offset_r = this.symbolTable.getOffset(srcName);
        /*if (symbolTable.getSpecialBase().contains(srcName) && irLoad.getValue().getValueType() instanceof IrIntType) { //地址值 在getElementPtr之后
            Lw lw = new Lw(new MipsReg(9), new MipsReg(30), offset_r);
            this.mipsInstructions.add(lw);
            lw = new Lw(new MipsReg(8), new MipsReg(9), 0);
            this.mipsInstructions.add(lw);
        }
        if (irLoad.getValue().getValueType() instanceof IrArrayType) { //获取数组基地址
            if (isGlobal(srcName)) {
                La la = new La(new MipsReg(8), srcName.substring(1));
                this.mipsInstructions.add(la);
            }
            else {
                Addi addi = new Addi(new MipsReg(8), new MipsReg(30), offset_r);
                this.mipsInstructions.add(addi);
            }
        }*/
        if (symbolTable.getSpecialBase().contains(srcName) && irLoad.getValue().getValueType() instanceof IrIntType) {
            Lw lw = new Lw(new MipsReg(9), new MipsReg(30), offset_r);
            this.mipsInstructions.add(lw);
            lw = new Lw(new MipsReg(8), new MipsReg(9), 0);
            this.mipsInstructions.add(lw);
        }
        else  {
            if (!isGlobal(srcName)) {
                // 在符号表中检索，找到存储的位置
                Lw lw = new Lw(new MipsReg(8), new MipsReg(30), offset_r);
                this.mipsInstructions.add(lw);
            }
            else { // 全局变量
                if (irLoad.getValue().getValueType() instanceof IrIntType) {
                    Lw lw = new Lw(new MipsReg(8), irLoad.getValue().getName().substring(1));
                    this.mipsInstructions.add(lw);
                }
                else {
                    La la = new La(new MipsReg(8), irLoad.getValue().getName().substring(1));
                    this.mipsInstructions.add(la);
                }
            }
        }
        //新建符号（对应左值），存入取得值
        int offset_l = this.symbolTable.getOffset(null);
        this.symbolTable.getSymbolMap().put(irLoad.getName(), offset_l);
        Sw sw = new Sw(new MipsReg(8), new MipsReg(30), offset_l);
        this.mipsInstructions.add(sw);
    }

    // int a = 1, a = getint(), a = 1;
    /**
     * LVal = Exp;
     * LVal = getint()
     * Decl
     */
    public void genMipsStore() {
        IrStore irStore = (IrStore) this.instruction;
        int offset_l = this.symbolTable.getOffset(irStore.getLeftOp().getName());
        int offset_r = this.symbolTable.getOffset(irStore.getRightOp().getName());
        // 判断赋值体本身
        if (isConst(irStore.getRightOp().getName())) {
            Li li = new Li(new MipsReg(8), Integer.parseInt(irStore.getRightOp().getName()));
            this.mipsInstructions.add(li);
        }
        else {
            Lw lw = new Lw(new MipsReg(8), new MipsReg(30), offset_r);
            this.mipsInstructions.add(lw);
        }
        //todo 约定上述三种情况均把计算结果取出放在$t0
        if (!symbolTable.getSpecialBase().contains(irStore.getLeftOp().getName())) {
            if (isGlobal(irStore.getLeftOp().getName())) {
                Sw sw = new Sw(new MipsReg(8), irStore.getLeftOp().getName().substring(1));
                this.mipsInstructions.add(sw);
            } else {
                Sw sw = new Sw(new MipsReg(8), new MipsReg(30), offset_l);
                this.mipsInstructions.add(sw);
            }
        }
        else {
            Lw lw = new Lw(new MipsReg(9), new MipsReg(30), offset_l);
            this.mipsInstructions.add(lw);
            Sw sw = new Sw(new MipsReg(8), new MipsReg(9), 0);
            this.mipsInstructions.add(sw);
        }
    }

    public boolean isConst(String name) {
        return !name.contains("@") && !name.contains("%");
    }

    public boolean isGlobal(String name) {
        return name.contains("@");
    }
}
