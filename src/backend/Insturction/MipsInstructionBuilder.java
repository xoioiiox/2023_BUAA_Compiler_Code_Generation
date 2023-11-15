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
import java.util.HashMap;

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

    public void genMipsZext() {
        IrZext irZext = (IrZext) this.instruction;
        MipsReg srcReg = mipsRegManager.findReg(mipsInstructions, irZext.getSrc().getName());
        MipsReg desReg = mipsRegManager.getEmptyReg(mipsInstructions, irZext.getName());
        Move move = new Move(desReg, srcReg);
        this.mipsInstructions.add(move);
        symbolTable.addUsed(irZext.getSrc().getName());
        int offset = this.symbolTable.getOffset(null);
        this.symbolTable.getSymbolMap().put(irZext.getName(), offset);
    }

    //%19 = icmp ne i32 0, %18
    public void genMipsIcmp() {
        IrIcmp icmp = (IrIcmp) this.instruction;
        IrValue value1 = icmp.getOperand(0);
        IrValue value2 = icmp.getOperand(1);
        // reg处理分配
        MipsReg reg_rs;
        MipsReg reg_rt;
        if (isConst(value1.getName())) {
            Li li = new Li(new MipsReg(16), Integer.parseInt(value1.getName()));
            this.mipsInstructions.add(li);
            reg_rs = new MipsReg(16);
        }
        else if (symbolTable.isTemp(value1.getName())) {
            reg_rs = this.mipsRegManager.findReg(this.mipsInstructions, value1.getName());
            mipsRegManager.addProtectList(reg_rs.getRegNum());
        }
        else {
            Lw lw = new Lw(new MipsReg(16), new MipsReg(30), symbolTable.getOffset(value1.getName()));
            this.mipsInstructions.add(lw);
            reg_rs = new MipsReg(16);
        }
        if (isConst(value2.getName())) {
            Li li = new Li(new MipsReg(17), Integer.parseInt(value2.getName()));
            this.mipsInstructions.add(li);
            reg_rt = new MipsReg(17);
        }
        else if (symbolTable.isTemp(value2.getName())) {
            reg_rt = this.mipsRegManager.findReg(this.mipsInstructions, value2.getName());
            mipsRegManager.addProtectList(reg_rt.getRegNum());
        }
        else {
            Lw lw = new Lw(new MipsReg(17), new MipsReg(30), symbolTable.getOffset(value2.getName()));
            this.mipsInstructions.add(lw);
            reg_rt = new MipsReg(17);
        }
        this.symbolTable.addUsed(value1.getName());
        this.symbolTable.addUsed(value2.getName());
        MipsReg reg_rd = this.mipsRegManager.getEmptyReg(this.mipsInstructions, icmp.getName());
        // 指令生成
        SCmpType type = getType(icmp.getType());
        SCmp sCmp = new SCmp(type, reg_rd, reg_rs, reg_rt);
        this.mipsInstructions.add(sCmp);
        int offset = this.symbolTable.getOffset(null);
        this.symbolTable.getSymbolMap().put(icmp.getName(), offset);
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
        if (symbolTable.isTemp(name) && !isGlobal(name)) {
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
                Addi addi = new Addi(new MipsReg(16), new MipsReg(30), offset);
                this.mipsInstructions.add(addi);
            }
            firstAddrReg = new MipsReg(16);
        }
        /*------再计算总偏移量------*/
        MipsReg offsetReg = this.mipsRegManager.getEmptyReg(mipsInstructions, "temp");
        mipsRegManager.addProtectList(offsetReg.getRegNum());
        if (getElementPtr.getIndex() != null) { //一维a[]->a 或二维b[][]->b[1]
            IrValue value = getElementPtr.getIndex();
            // 由llvm特性，这里的index一定为临时变量
            MipsReg index = this.mipsRegManager.findReg(mipsInstructions, value.getName());
            mipsRegManager.addProtectList(index.getRegNum());

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
                MipsReg mulNumReg = this.mipsRegManager.getEmptyReg(mipsInstructions, "temp1");
                Li li = new Li(mulNumReg, elementNum);
                this.mipsInstructions.add(li);
                Binary binary = new Binary(BinaryType.mulu, offsetReg, index, mulNumReg);
                this.mipsInstructions.add(binary);
                this.symbolTable.addUsed("temp1");
                this.symbolTable.addUsed(value.getName());
            }
            else {
                Move move = new Move(offsetReg, index);
                this.mipsInstructions.add(move);
            }
            this.symbolTable.addUsed(value.getName());
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

            MipsReg index1 = this.mipsRegManager.findReg(mipsInstructions, value1.getName());
            mipsRegManager.addProtectList(index1.getRegNum());
            MipsReg index2 = this.mipsRegManager.findReg(mipsInstructions, value2.getName());
            mipsRegManager.addProtectList(index2.getRegNum());

            MipsReg mulNumReg = this.mipsRegManager.getEmptyReg(mipsInstructions, "temp1");
            mipsRegManager.addProtectList(mulNumReg.getRegNum());
            Li li = new Li(mulNumReg, elementNum2);
            this.mipsInstructions.add(li);

            MipsReg mulResReg = this.mipsRegManager.getEmptyReg(mipsInstructions, "temp2");
            Binary binary = new Binary(BinaryType.mulu, mulResReg, index1, mulNumReg);
            this.mipsInstructions.add(binary);
            Binary binary1 = new Binary(BinaryType.addu, offsetReg, mulResReg, index2);
            this.mipsInstructions.add(binary1);
            this.symbolTable.addUsed("temp1");
            this.symbolTable.addUsed("temp2");
            this.symbolTable.addUsed(value1.getName());
            this.symbolTable.addUsed(value2.getName());
        }
        this.symbolTable.addUsed("temp");
        /*------计算绝对位置------*/
        Sll sll = new Sll(offsetReg, offsetReg, 2);
        this.mipsInstructions.add(sll);

        MipsReg addrReg = this.mipsRegManager.getEmptyReg(mipsInstructions, getElementPtr.getName());
        Binary binary = new Binary(BinaryType.addu, addrReg, firstAddrReg, offsetReg);
        this.mipsInstructions.add(binary);

        /*MipsReg resReg = mipsRegManager.getEmptyReg(mipsInstructions, getElementPtr.getName());
        Move move = new Move(resReg, addrReg);
        this.mipsInstructions.add(move);*/

        int offset = this.symbolTable.getOffset(null);
        symbolTable.getSymbolMap().put(getElementPtr.getName(), offset);
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
        this.symbolTable.addNotTemp(name); // 在allocate时记录其为非临时变量
        this.symbolTable.getSymbolMap().put(name, this.symbolTable.getOffset(null));
        this.symbolTable.changeOffset(4 * num);
    }

    public void genMipsBinaryInst() {
        IrBinaryInst irBinaryInst = (IrBinaryInst) this.instruction;
        /*------获取操作数(li lw)------*/
        String rdName = irBinaryInst.getName();
        String rsName = irBinaryInst.getOperand(0).getName();
        String rtName = irBinaryInst.getOperand(1).getName();
        MipsReg rdReg;
        MipsReg rsReg;
        MipsReg rtReg;
        rdReg = mipsRegManager.getEmptyReg(mipsInstructions, rdName);
        mipsRegManager.addProtectList(rdReg.getRegNum());

        if (isConst(rsName)) {
            Li li = new Li(new MipsReg(17), Integer.parseInt(rsName));
            this.mipsInstructions.add(li);
            rsReg = new MipsReg(17);
        }
        else if (symbolTable.isTemp(rsName)) {
            rsReg = mipsRegManager.findReg(mipsInstructions, rsName);
            mipsRegManager.addProtectList(rsReg.getRegNum());
        }
        else {
            Lw lw = new Lw(new MipsReg(17), new MipsReg(30), symbolTable.getOffset(rsName));
            this.mipsInstructions.add(lw);
            rsReg = new MipsReg(17);
        }
        if (isConst(rtName)) {
            Li li = new Li(new MipsReg(18), Integer.parseInt(rtName));
            this.mipsInstructions.add(li);
            rtReg = new MipsReg(18);
        }
        else if (symbolTable.isTemp(rtName)) {
            rtReg = mipsRegManager.findReg(mipsInstructions, rtName);
            mipsRegManager.addProtectList(rtReg.getRegNum());
        }
        else {
            Lw lw = new Lw(new MipsReg(18), new MipsReg(30), symbolTable.getOffset(rtName));
            this.mipsInstructions.add(lw);
            rtReg = new MipsReg(18);
        }
        /*------生成指令(add sub...)------*/
        BinaryType binaryType = getBinaryType(irBinaryInst.getType());
        if (binaryType == BinaryType.addu || binaryType == BinaryType.subu || binaryType == BinaryType.mulu) {
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
        this.symbolTable.addUsed(rsName); //若非临时变量 则不会发生对于该符号的查询
        this.symbolTable.addUsed(rtName);
        int rdOffset = this.symbolTable.getOffset(null);
        this.symbolTable.getSymbolMap().put(rdName, rdOffset); // todo 需要放在末尾吗？
        mipsRegManager.cleanProtectList();
    }

    public BinaryType getBinaryType(IrBinaryType type) {
        BinaryType binaryType = null;
        switch(type) {
            case add: binaryType = BinaryType.addu; break;
            case sub: binaryType = BinaryType.subu; break;
            case mul: binaryType = BinaryType.mulu; break;
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
            // 一定是临时变量
            MipsReg condReg = mipsRegManager.findReg(mipsInstructions, value.getName());
            String trueLabel = irBr.getTrueLabel();
            String falseLabel = irBr.getFalseLabel();
            Beq beq = new Beq(condReg, 1, trueLabel);
            this.mipsInstructions.add(beq);
            J j = new J(falseLabel);
            this.mipsInstructions.add(j);
            symbolTable.addUsed(value.getName());
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
        String name = irCall.getValue().getName();
        MipsReg srcReg;
        if (isConst(name)) {
            Li li1 = new Li(new MipsReg(16), Integer.parseInt(name));
            this.mipsInstructions.add(li1);
            srcReg = new MipsReg(16);
        }
        else {
            srcReg = mipsRegManager.findReg(mipsInstructions, name);
        }
        Move move = new Move(new MipsReg(4), srcReg);
        this.mipsInstructions.add(move);
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
        // 一定是临时变量
        MipsReg reg = mipsRegManager.getEmptyReg(mipsInstructions, irCall.getName());
        Move move = new Move(reg, new MipsReg(2));
        this.mipsInstructions.add(move);
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
            // 一定是临时变量
            MipsReg paramReg;
            if (isConst(param.getName())) {
                Li li = new Li(new MipsReg(16), Integer.parseInt(param.getName()));
                this.mipsInstructions.add(li);
                paramReg = new MipsReg(16);
            }
            else {
                paramReg = mipsRegManager.findReg(mipsInstructions, param.getName());
            }
            Sw sw1 = new Sw(paramReg, new MipsReg(30), this.symbolTable.getOffset(null));
            this.mipsInstructions.add(sw1);
        }
        // 保存寄存器值
        this.mipsRegManager.restore(this.mipsInstructions);
        // 调整fp指针位置
        Addi addi1 = new Addi(new MipsReg(30), new MipsReg(30), fpOffset);
        this.mipsInstructions.add(addi1);
        // 调用函数
        Jal jal = new Jal(irFunction.getName().substring(1));
        this.mipsInstructions.add(jal);
        // 恢复现场
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
            // 一定是临时变量
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
            MipsReg retReg;
            if (isConst(retName)) {
                Li li = new Li(new MipsReg(16), Integer.parseInt(retName));
                this.mipsInstructions.add(li);
                retReg = new MipsReg(16);
            }
            else {
                retReg = mipsRegManager.findReg(mipsInstructions, retName);
            }
            Move move = new Move(new MipsReg(2), retReg);
            this.mipsInstructions.add(move);
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
        int offset_src = this.symbolTable.getOffset(srcName);
        // 一定是临时变量
        MipsReg desReg;
        if (isConst(irLoad.getName())) {
            Li li = new Li(new MipsReg(16), Integer.parseInt(irLoad.getName()));
            this.mipsInstructions.add(li);
            desReg = new MipsReg(16);
        }
        else {
            desReg = mipsRegManager.getEmptyReg(mipsInstructions, irLoad.getName());
        }
        mipsRegManager.addProtectList(desReg.getRegNum());
        if (symbolTable.getSpecialBase().contains(srcName)) {
            // 一定是临时变量
            MipsReg srcReg = mipsRegManager.findReg(mipsInstructions, srcName);
            if(irLoad.getValue().getValueType() instanceof IrIntType) {
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
                    Lw lw = new Lw(new MipsReg(16), irLoad.getValue().getName().substring(1));
                    this.mipsInstructions.add(lw);
                }
                else {
                    La la = new La(new MipsReg(16), irLoad.getValue().getName().substring(1));
                    this.mipsInstructions.add(la);
                }
                Move move = new Move(desReg, new MipsReg(16));
                this.mipsInstructions.add(move);
            }
            else {
                if (irLoad.getValue().getValueType() instanceof IrIntType
                        || irLoad.getValue().getValueType() instanceof IrPointerType) {
                    // 在符号表中检索，找到存储的位置
                    MipsReg srcReg;
                    if (isConst(srcName)) {
                        Li li = new Li(new MipsReg(16), Integer.parseInt(srcName));
                        this.mipsInstructions.add(li);
                        srcReg = new MipsReg(16);
                    }
                    else if (symbolTable.isTemp(srcName)) {
                        srcReg = mipsRegManager.findReg(mipsInstructions, srcName);
                    }
                    else {
                        Lw lw = new Lw(new MipsReg(16), new MipsReg(30), offset_src);
                        this.mipsInstructions.add(lw);
                        srcReg = new MipsReg(16);
                    }
                    Move move = new Move(desReg, srcReg);
                    this.mipsInstructions.add(move);
                }
                else { //局部数组
                    Addi addi = new Addi(desReg, new MipsReg(30), offset_src);
                    this.mipsInstructions.add(addi);
                }
            }
        }
        this.symbolTable.addUsed(srcName);
        //新建符号（对应左值），存入取得值
        int offset_des = this.symbolTable.getOffset(null);
        this.symbolTable.getSymbolMap().put(irLoad.getName(), offset_des);
        mipsRegManager.cleanProtectList();
    }

    // int a = 1, a = getint(), a = 1;
    /**
     * LVal = Exp;
     * LVal = getint()
     * Decl
     */
    public void genMipsStore() {
        IrStore irStore = (IrStore) this.instruction;
        String srcName = irStore.getRightOp().getName();
        String desName = irStore.getLeftOp().getName();
        // 一定是临时变量
        MipsReg srcReg;
        if (isConst(srcName)) {
            Li li = new Li(new MipsReg(16), Integer.parseInt(srcName));
            this.mipsInstructions.add(li);
            srcReg = new MipsReg(16);
        }
        else {
            srcReg = mipsRegManager.findReg(mipsInstructions, srcName);
        }
        mipsRegManager.addProtectList(srcReg.getRegNum());
        if (symbolTable.getSpecialBase().contains(desName)) { // 对数组赋值
            MipsReg desReg = mipsRegManager.findReg(mipsInstructions, desName);
            Sw sw = new Sw(srcReg, desReg, 0);
            this.mipsInstructions.add(sw);
        }
        else {
            if (isGlobal(desName)) {
                Sw sw = new Sw(srcReg, desName.substring(1));
                this.mipsInstructions.add(sw);
            }
            else {
                if (symbolTable.isTemp(desName)) {
                    MipsReg desReg = mipsRegManager.findReg(mipsInstructions, desName);
                    Move move = new Move(desReg, srcReg);
                    this.mipsInstructions.add(move);
                }
                else {
                    Sw sw = new Sw(srcReg, new MipsReg(30), symbolTable.getOffset(desName));
                    this.mipsInstructions.add(sw);
                }
            }
        }
        mipsRegManager.cleanProtectList();
    }

    public boolean isConst(String name) {
        return !name.contains("@") && !name.contains("%");
    }

    public boolean isGlobal(String name) {
        return name.contains("@");
    }
}
