package backend;

import backend.Function.MipsFunction;
import backend.Function.MipsFunctionBuilder;
import backend.Insturction.Li;
import backend.Insturction.MipsInstruction;
import backend.Insturction.Sw;
import midend.llvmIr.IrModule;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.value.constant.IrIntConst;
import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.globalVariable.IrGlobalVariable;

import java.util.ArrayList;

public class MipsBuilder {
    private IrModule irModule;
    private ArrayList<MipsGlobalVariable> mipsGlobalVariables;
    private ArrayList<MipsInstruction> mipsInstructions;
    private ArrayList<MipsFunction> mipsFunctions;
    //private MipsSymbolTable symbolTable;
    private static int offset = 0;

    public MipsBuilder(IrModule irModule) {
        this.irModule = irModule;
        this.mipsGlobalVariables = new ArrayList<>();
        this.mipsInstructions = new ArrayList<>();
        this.mipsFunctions = new ArrayList<>();
        //this.symbolTable = new MipsSymbolTable(null, offset); //根符号表
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
            MipsSymbolTable symbolTable = new MipsSymbolTable(offset); // 对于每个函数都建一个符号表记录内存位置
            MipsFunctionBuilder mipsFunctionBuilder = new MipsFunctionBuilder(function, symbolTable);
            this.mipsFunctions.add(mipsFunctionBuilder.genMipsFunction());
        }
        return new MipsModule(mipsGlobalVariables, mipsInstructions, mipsFunctions);
    }

}
