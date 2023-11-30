package backend.Insturction;

import backend.MipsReg;
import backend.MipsRegManager;
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
    private MipsRegManager mipsRegManager;

    public MipsInstructionBuilder(IrInstruction instruction, MipsSymbolTable symbolTable,
                                  boolean isMainFunc, MipsRegManager mipsRegManager) {
        this.instruction = instruction;
        this.symbolTable = symbolTable;
        this.mipsInstructions = new ArrayList<>();
        this.isMainFunc = isMainFunc;
        this.mipsRegManager = mipsRegManager;
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
            genMipsZext();
        }
        return this.mipsInstructions;
    }

    public void genMipsZext() { //可以删掉
        IrZext irZext = (IrZext) this.instruction;
        MipsReg srcReg = getMipsReg(irZext.getSrc().getName(), new MipsReg(16));
        mipsRegManager.addProtectList(srcReg.getRegNum());
        MipsReg desReg = mipsRegManager.getEmptyReg(mipsInstructions, irZext.getName());
        Move move = new Move(desReg, srcReg);
        this.mipsInstructions.add(move);
        symbolTable.addUsedTempVal(irZext.getSrc().getName());
        mipsRegManager.cleanProtectList();
    }

    //%19 = icmp ne i32 0, %18
    public void genMipsIcmp() {
        IrIcmp icmp = (IrIcmp) this.instruction;
        IrValue value1 = icmp.getOperand(0);
        IrValue value2 = icmp.getOperand(1);
        // reg处理分配
        MipsReg reg_rs = getMipsReg(value1.getName(), new MipsReg(16));
        mipsRegManager.addProtectList(reg_rs.getRegNum());
        MipsReg reg_rt = getMipsReg(value2.getName(), new MipsReg(17));
        mipsRegManager.addProtectList(reg_rt.getRegNum());
        MipsReg reg_rd = this.mipsRegManager.getEmptyReg(this.mipsInstructions, icmp.getName());
        // 指令生成
        SCmpType type = getType(icmp.getType());
        SCmp sCmp = new SCmp(type, reg_rd, reg_rs, reg_rt);
        this.mipsInstructions.add(sCmp);
        this.symbolTable.addUsedTempVal(value1.getName());
        this.symbolTable.addUsedTempVal(value2.getName());
        mipsRegManager.cleanProtectList();
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

    // b[][] 加载b[]
    public void genMipsGetElementPtr() {
        IrGetElementPtr getElementPtr = (IrGetElementPtr) this.instruction;
        String name = getElementPtr.getPtrVal().getName();
        /*------先获取数组首地址------*/
        // 获取存放数组首地址的寄存器
        MipsReg firstAddrReg;
        if (!symbolTable.isLocalVal(name) && !isGlobal(name)) {
            firstAddrReg = this.mipsRegManager.findReg(this.mipsInstructions, name);
            mipsRegManager.addProtectList(firstAddrReg.getRegNum());
        }
        else {
            if (isGlobal(name)) { //全局数组
                La la = new La(new MipsReg(16), name.substring(1));
                this.mipsInstructions.add(la);
            } else if (getElementPtr.isFParam() || getElementPtr.getPtrVal().getValueType() instanceof IrPointerType) { //形参
                int offset = symbolTable.getOffset(name);
                Lw lw = new Lw(new MipsReg(16), new MipsReg(30), offset);
                this.mipsInstructions.add(lw);
            } else { //局部数组
                int offset = symbolTable.getOffset(name);
                Addiu addiu = new Addiu(new MipsReg(16), new MipsReg(30), offset);
                this.mipsInstructions.add(addiu);
            }
            firstAddrReg = new MipsReg(16);
        }
        /*------再计算总偏移量------*/
        MipsReg offsetReg = new MipsReg(17);
        if (getElementPtr.getIndex() != null) { //一维a[]->a 或二维b[][]->b[1]
            IrValue value = getElementPtr.getIndex();
            // 由llvm特性，这里的index一定为临时变量
            MipsReg index = getMipsReg(value.getName(), new MipsReg(18));

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
                MipsReg mulNumReg = new MipsReg(19);
                Li li = new Li(mulNumReg, elementNum);
                this.mipsInstructions.add(li);
                Binary binary = new Binary(BinaryType.mul, offsetReg, index, mulNumReg);
                this.mipsInstructions.add(binary);
            }
            else {
                Move move = new Move(offsetReg, index);
                this.mipsInstructions.add(move);
            }
            this.symbolTable.addUsedTempVal(value.getName());
        }
        else { //一定是二维
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
            MipsReg index1 = getMipsReg(value1.getName(), new MipsReg(18));
            mipsRegManager.addProtectList(index1.getRegNum());
            MipsReg index2 = getMipsReg(value2.getName(), new MipsReg(19));
            mipsRegManager.addProtectList(index2.getRegNum());

            MipsReg mulNumReg = new MipsReg(20);
            Li li = new Li(mulNumReg, elementNum2);
            this.mipsInstructions.add(li);

            MipsReg mulResReg = new MipsReg(21);
            Binary binary = new Binary(BinaryType.mul, mulResReg, index1, mulNumReg);
            this.mipsInstructions.add(binary);
            Binary binary1 = new Binary(BinaryType.addu, offsetReg, mulResReg, index2);
            this.mipsInstructions.add(binary1);
            this.symbolTable.addUsedTempVal(value1.getName());
            this.symbolTable.addUsedTempVal(value2.getName());
        }
        /*------计算绝对位置------*/
        Sll sll = new Sll(offsetReg, offsetReg, 2);
        this.mipsInstructions.add(sll);

        MipsReg addrReg = this.mipsRegManager.getEmptyReg(mipsInstructions, getElementPtr.getName());
        Binary binary = new Binary(BinaryType.addu, addrReg, firstAddrReg, offsetReg);
        this.mipsInstructions.add(binary);

        symbolTable.addSpecialBase(getElementPtr.getName());
        mipsRegManager.cleanProtectList();
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
        String name = irAlloca.getValue().getName();
        this.symbolTable.addLocalVal(name); // 在allocate时记录其为非临时变量
        this.symbolTable.getSymbolMap().put(name, this.symbolTable.getOffset(null));
        this.symbolTable.changeOffset(4 * num); // 是不是多加了一个？
    }

    public void genMipsBinaryInst() {
        IrBinaryInst irBinaryInst = (IrBinaryInst) this.instruction;
        /*------获取操作数(li lw)------*/
        String rdName = irBinaryInst.getName();
        String rsName = irBinaryInst.getOperand(0).getName();
        String rtName = irBinaryInst.getOperand(1).getName();

        MipsReg rsReg = getMipsReg(rsName, new MipsReg(16));
        mipsRegManager.addProtectList(rsReg.getRegNum());
        MipsReg rtReg = getMipsReg(rtName, new MipsReg(17));
        mipsRegManager.addProtectList(rtReg.getRegNum());
        MipsReg rdReg = mipsRegManager.getEmptyReg(mipsInstructions, rdName);
        /*------生成指令(add sub...)------*/
        BinaryType binaryType = getBinaryType(irBinaryInst.getType());
        if (binaryType == BinaryType.addu || binaryType == BinaryType.subu || binaryType == BinaryType.mul) {
            Binary binary = new Binary(binaryType, rdReg, rsReg, rtReg);
            this.mipsInstructions.add(binary);
        } else if (binaryType == BinaryType.div) {
            Div div = new Div(rsReg, rtReg);
            this.mipsInstructions.add(div);
            Mfhi_lo mflo = new Mfhi_lo(rdReg, false);
            this.mipsInstructions.add(mflo);
        } else if (binaryType == BinaryType.mod) {
            Div div = new Div(rsReg, rtReg);
            this.mipsInstructions.add(div);
            Mfhi_lo mfhi = new Mfhi_lo(rdReg, true);
            this.mipsInstructions.add(mfhi);
        }
        this.symbolTable.addUsedTempVal(rsName); //若非临时变量 则不会发生对于该符号的查询
        this.symbolTable.addUsedTempVal(rtName);
        mipsRegManager.cleanProtectList();
    }

    public BinaryType getBinaryType(IrBinaryType type) {
        BinaryType binaryType = null;
        switch(type) {
            case add: binaryType = BinaryType.addu; break;
            case sub: binaryType = BinaryType.subu; break;
            case mul: binaryType = BinaryType.mul; break;
            case sdiv: binaryType = BinaryType.div; break;
            case srem: binaryType = BinaryType.mod; break;
        }
        return binaryType;
    }

    //br i1 %16, label %20, label %17
    public void genMipsBr() {
        IrBr irBr = (IrBr) this.instruction;
        IrValue value = irBr.getCond();
        if (value != null) {
            // 一定是临时变量
            MipsReg condReg = getMipsReg(value.getName(), new MipsReg(16));
            this.symbolTable.addUsedTempVal(value.getName()); //先标记使用，从而释放寄存器
            String trueLabel = irBr.getTrueLabel();
            String falseLabel = irBr.getFalseLabel();
            // restore
            this.mipsRegManager.restore(this.mipsInstructions);
            // 生成跳转语句
            Beq beq = new Beq(condReg, 1, trueLabel); // if (2) todo ?
            this.mipsInstructions.add(beq);
            J j = new J(falseLabel);
            this.mipsInstructions.add(j);
            symbolTable.addUsedTempVal(value.getName());
        }
        else {
            // restore
            this.mipsRegManager.restore(this.mipsInstructions);
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
        String name = irCall.getValue().getName();
        MipsReg srcReg = getMipsReg(name, new MipsReg(16));
        Move move = new Move(new MipsReg(4), srcReg);
        this.mipsInstructions.add(move);
        this.symbolTable.addUsedTempVal(name);
        Syscall syscall = new Syscall();
        this.mipsInstructions.add(syscall);
    }

    public void genMipsGetInt(IrCall irCall) {
        Li li = new Li(new MipsReg(2), 5);
        this.mipsInstructions.add(li);
        Syscall syscall = new Syscall();
        this.mipsInstructions.add(syscall);
        MipsReg reg = mipsRegManager.getEmptyReg(mipsInstructions, irCall.getName());
        Move move = new Move(reg, new MipsReg(2));
        this.mipsInstructions.add(move);
    }

    public void genMipsCallFunc(IrCall irCall) {
        // 保存$ra，防止覆盖
        Sw sw = new Sw(new MipsReg(31), new MipsReg(29), 0);
        this.mipsInstructions.add(sw);
        Addiu addiu = new Addiu(new MipsReg(29), new MipsReg(29), -4);
        this.mipsInstructions.add(addiu);
        // 保存寄存器值 todo 函数参数其实不用sw
        this.mipsRegManager.restore(this.mipsInstructions);
        // 获取fp指针当前偏移量
        int fpOffset = this.symbolTable.getSumOffset();
        // push函数参数
        IrFunction irFunction = (IrFunction) irCall.getOperand(0);
        int paramNum = ((IrFunctionType)irFunction.getValueType()).getParamTypes().size();
        // push的过程中将寄存器中原有的值存到内存，这样0($fp)就不是第一个参数了 :不会存在因为restore时已经给每个寄存器分配了offset
        for (int i = 0; i < paramNum; i++) {
            IrValue param = irCall.getOperand(i + 1);
            // 一定是临时变量
            MipsReg paramReg = getMipsReg(param.getName(), new MipsReg(16));
            Sw sw1 = new Sw(paramReg, new MipsReg(30), this.symbolTable.getOffset(null));
            this.mipsInstructions.add(sw1);
            symbolTable.addUsedTempVal(param.getName());
        }
        // 调整fp指针位置
        Addiu addiu1 = new Addiu(new MipsReg(30), new MipsReg(30), fpOffset);
        this.mipsInstructions.add(addiu1);
        // 调用函数
        Jal jal = new Jal(irFunction.getName().substring(1));
        this.mipsInstructions.add(jal);
        // 恢复现场
        Addiu addiu3 = new Addiu(new MipsReg(30), new MipsReg(30), -1 * fpOffset);
        this.mipsInstructions.add(addiu3);
        Addiu addiu2 = new Addiu(new MipsReg(29), new MipsReg(29), 4);
        this.mipsInstructions.add(addiu2);
        Lw lw = new Lw(new MipsReg(31), new MipsReg(29), 0);
        this.mipsInstructions.add(lw);
        if (irCall.isHasExpReturn()) {
            MipsReg resReg = mipsRegManager.getEmptyReg(mipsInstructions, irCall.getName());
            Move move = new Move(resReg, new MipsReg(2));
            this.mipsInstructions.add(move);
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
            String retName = irRet.getName();
            MipsReg retReg = getMipsReg(retName, new MipsReg(16));
            Move move = new Move(new MipsReg(2), retReg);
            this.mipsInstructions.add(move);
            symbolTable.addUsedTempVal(retName);
            this.mipsRegManager.restore(mipsInstructions);
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
            this.mipsRegManager.restore(mipsInstructions);
            Jr jr = new Jr(new MipsReg(31));
            this.mipsInstructions.add(jr);
        }
    }

    // load左侧变量都是为了调用变量新建的临时变量
    public void genMipsLoad() {
        IrLoad irLoad = (IrLoad) this.instruction;
        String srcName = irLoad.getValue().getName();
        String desName = irLoad.getName();
        // 一定是临时变量
        MipsReg desReg = mipsRegManager.getEmptyReg(mipsInstructions, desName);
        mipsRegManager.addProtectList(desReg.getRegNum());
        if (symbolTable.getSpecialBase().contains(srcName)) { // 从该地址获取数值
            // 一定是临时变量
            MipsReg srcReg = mipsRegManager.findReg(mipsInstructions, srcName);
            if(irLoad.getValue().getValueType() instanceof IrIntType) { //todo when?
                Lw lw = new Lw(desReg, srcReg, 0);
                this.mipsInstructions.add(lw);
            }
            else {
                Move move = new Move(desReg, srcReg);
                this.mipsInstructions.add(move);
            }
        }
        else  {
            if (isGlobal(srcName)) { // 全局变量
                if (irLoad.getValue().getValueType() instanceof IrIntType) {
                    Lw lw = new Lw(desReg, irLoad.getValue().getName().substring(1));
                    this.mipsInstructions.add(lw);
                }
                else {
                    La la = new La(desReg, irLoad.getValue().getName().substring(1));
                    this.mipsInstructions.add(la);
                }
            }
            else {
                if (irLoad.getValue().getValueType() instanceof IrIntType
                        || irLoad.getValue().getValueType() instanceof IrPointerType) {
                    // 在符号表中检索，找到存储的位置
                    if (isConst(srcName)) {
                        Li li = new Li(desReg, Integer.parseInt(srcName));
                        this.mipsInstructions.add(li);
                    }
                    else if (symbolTable.isLocalVal(srcName)) {
                        int offset_src = this.symbolTable.getOffset(srcName);
                        Lw lw = new Lw(desReg, new MipsReg(30), offset_src);
                        this.mipsInstructions.add(lw);
                    }
                    else {
                        MipsReg srcReg = mipsRegManager.findReg(mipsInstructions, srcName);
                        Move move = new Move(desReg, srcReg);
                        this.mipsInstructions.add(move);
                    }
                }
                else { //局部数组
                    int offset_src = this.symbolTable.getOffset(srcName);
                    Addiu addiu = new Addiu(desReg, new MipsReg(30), offset_src);
                    this.mipsInstructions.add(addiu);
                }
            }
        }
        this.symbolTable.addUsedTempVal(srcName);
        mipsRegManager.cleanProtectList();
    }

    public void genMipsStore() {
        IrStore irStore = (IrStore) this.instruction;
        String srcName = irStore.getRightOp().getName();
        String desName = irStore.getLeftOp().getName();
        MipsReg srcReg = getMipsReg(srcName, new MipsReg(16));
        mipsRegManager.addProtectList(srcReg.getRegNum());
        if (symbolTable.getSpecialBase().contains(desName)) { // 对数组赋值
            MipsReg desReg = mipsRegManager.findReg(mipsInstructions, desName);
            Sw sw = new Sw(srcReg, desReg, 0);
            this.mipsInstructions.add(sw);
            symbolTable.addUsedTempVal(desName);
        }
        else {
            if (isGlobal(desName)) {
                Sw sw = new Sw(srcReg, desName.substring(1));
                this.mipsInstructions.add(sw);
            }
            else if (symbolTable.isLocalVal(desName)) {
                Sw sw = new Sw(srcReg, new MipsReg(30), symbolTable.getOffset(desName));
                this.mipsInstructions.add(sw);
            }
            else {
                MipsReg desReg = mipsRegManager.findReg(mipsInstructions, desName);
                Move move = new Move(desReg, srcReg);
                this.mipsInstructions.add(move);
            }
        }
        symbolTable.addUsedTempVal(srcName);
        mipsRegManager.cleanProtectList();
    }

    public boolean isConst(String name) {
        return !name.contains("@") && !name.contains("%");
    }

    public boolean isGlobal(String name) {
        return name.contains("@");
    }

    public MipsReg getMipsReg(String name, MipsReg reg) {
        if (isConst(name)) {
            Li li = new Li(reg, Integer.parseInt(name));
            this.mipsInstructions.add(li);
            return reg;
        }
        else if (isGlobal(name)) { //全局数组会单独考虑到
            Lw lw = new Lw(reg, name.substring(1));
            this.mipsInstructions.add(lw);
            return reg;
        }
        else if (symbolTable.isLocalVal(name)) {
            Lw lw = new Lw(reg, new MipsReg(30), symbolTable.getOffset(name));
            this.mipsInstructions.add(lw);
            return reg;
        }
        else {
            return mipsRegManager.findReg(mipsInstructions, name);
        }
    }
}
