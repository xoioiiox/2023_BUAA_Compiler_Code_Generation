package midend.llvmIr;

import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.globalVariable.IrGlobalVariable;

import java.util.ArrayList;

public class IrModule {
    private ArrayList<IrGlobalVariable> irGlobalVariables;
    private ArrayList<IrFunction> irFunctions;

    public IrModule(ArrayList<IrGlobalVariable> irGlobalVariables, ArrayList<IrFunction> irFunctions) {
        this.irGlobalVariables = irGlobalVariables;
        this.irFunctions = irFunctions;
    }

    public ArrayList<IrGlobalVariable> getIrGlobalVariables() {
        return irGlobalVariables;
    }

    public ArrayList<IrFunction> getIrFunctions() {
        return irFunctions;
    }

    public void IrPrint() {
        String libFunc = "declare i32 @getint()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)";
        System.out.println(libFunc);
        for (IrGlobalVariable irGlobalVariable : irGlobalVariables) {
            System.out.println(irGlobalVariable);
        }
        for (IrFunction function : irFunctions) {
            System.out.println(function);
        }
    }
}
