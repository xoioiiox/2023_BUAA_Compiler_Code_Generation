package backend;

import backend.Function.MipsFunction;
import backend.Function.MipsFunctionBuilder;
import midend.llvmIr.IrModule;
import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.globalVariable.IrGlobalVariable;

import java.util.ArrayList;

public class MipsBuilder {
    private IrModule irModule;
    private ArrayList<MipsGlobalVariable> mipsGlobalVariables;
    private ArrayList<MipsFunction> mipsFunctions;

    public MipsBuilder(IrModule irModule) {
        this.irModule = irModule;
        this.mipsGlobalVariables = new ArrayList<>();
        this.mipsFunctions = new ArrayList<>();
    }

    public MipsModule genMipsModule() {
        //global variable -> .data
        for (IrGlobalVariable globalVariable : this.irModule.getIrGlobalVariables()) {
            MipsGlobalVariable mipsGlobalVariable =
                    new MipsGlobalVariable(globalVariable.getName().substring(1), globalVariable);
            this.mipsGlobalVariables.add(mipsGlobalVariable);
        }
        // functions
        for (IrFunction function : this.irModule.getIrFunctions()) {
            MipsFunctionBuilder mipsFunctionBuilder = new MipsFunctionBuilder(function);
            this.mipsFunctions.add(mipsFunctionBuilder.genMipsFunction());
        }
        return new MipsModule(mipsGlobalVariables, mipsFunctions);
    }

}
