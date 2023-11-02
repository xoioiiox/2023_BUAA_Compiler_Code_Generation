package backend;

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
        IrIntConst irIntConst = (IrIntConst)globalVariable.getIrConstant();
        return name + ": .word " + ((irIntConst == null) ? 0 : irIntConst.getVal());
    }
}
