package backend;

import midend.llvmIr.type.IrArrayType;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.value.constant.IrArrayConst;
import midend.llvmIr.value.constant.IrConstant;
import midend.llvmIr.value.constant.IrIntConst;
import midend.llvmIr.value.globalVariable.IrGlobalVariable;

public class MipsGlobalVariable {
    private String name;
    private IrGlobalVariable globalVariable;

    public MipsGlobalVariable(String name, IrGlobalVariable globalVariable) {
        this.name = name;
        this.globalVariable = globalVariable;
    }

    public String getName() {
        return name;
    }

    public IrGlobalVariable getGlobalVariable() {
        return globalVariable;
    }

    @Override
    public String toString() {
        if (globalVariable.getValueType() instanceof IrIntType) {
            IrIntConst irIntConst = (IrIntConst) globalVariable.getValue();
            return name + ": .word " + ((irIntConst == null) ? 0 : irIntConst.getVal());
        }
        else { //数组
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            IrArrayConst irArrayConst = (IrArrayConst) globalVariable.getValue();
            if (irArrayConst.getVal().size() == 0) {
                sb.append(": .space ");
                int num;
                IrArrayType irArrayType = (IrArrayType) globalVariable.getValueType();
                if (irArrayType.getDim() == 1) {
                    num = 4 * irArrayType.getElementNum();
                }
                else {
                    num = 4 * irArrayType.getElementNum1() * irArrayType.getElementNum2();
                }
                sb.append(num);
            }
            else {
                sb.append(": .word ");
                for (IrConstant irConstant : irArrayConst.getVal()) {
                    if (irConstant instanceof IrIntConst) {
                        sb.append(((IrIntConst) irConstant).getVal());
                        sb.append(", ");
                    }
                    else {
                        for (IrConstant irConstant1 : ((IrArrayConst)irConstant).getVal()) {
                            sb.append(((IrIntConst) irConstant1).getVal());
                            sb.append(", ");
                        }
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
    }
}
