package backend;

import backend.Function.MipsFunction;
import backend.Function.MipsFunctionBuilder;
import backend.Insturction.Li;
import backend.Insturction.MipsInstruction;
import backend.Insturction.Sw;
import midend.llvmIr.IrModule;
import midend.llvmIr.value.constant.IrIntConst;
import midend.llvmIr.value.function.IrFunction;
import midend.llvmIr.value.globalVariable.IrGlobalVariable;

import java.util.ArrayList;

public class MipsBuilder {
    private IrModule irModule;
    private ArrayList<MipsGlobalVariable> mipsGlobalVariables;
    private ArrayList<MipsInstruction> mipsInstructions;
    private ArrayList<MipsFunction> mipsFunctions;
    private MipsSymbolTable symbolTable;
    private static int offset = 0;

    public MipsBuilder(IrModule irModule) {
        this.irModule = irModule;
        this.mipsGlobalVariables = new ArrayList<>();
        this.mipsInstructions = new ArrayList<>();
        this.mipsFunctions = new ArrayList<>();
        this.symbolTable = new MipsSymbolTable(null, offset); //根符号表
    }

    public MipsModule genMipsModule() {
        int cnt = 0;
        //todo symbol table
        //global variable -> .data
        for (IrGlobalVariable globalVariable : this.irModule.getIrGlobalVariables()) {
            MipsGlobalVariable mipsGlobalVariable =
                    new MipsGlobalVariable(globalVariable.getName().substring(1), globalVariable);
            genMipsGlobalVariable(mipsGlobalVariable, cnt);
            this.mipsGlobalVariables.add(mipsGlobalVariable);
            cnt++;
        }
        // functions
        for (IrFunction function : this.irModule.getIrFunctions()) {
            MipsSymbolTable symbolTable1 = new MipsSymbolTable(symbolTable, offset);
            MipsFunctionBuilder mipsFunctionBuilder = new MipsFunctionBuilder(function, symbolTable1);
            this.mipsFunctions.add(mipsFunctionBuilder.genMipsFunction());
        }
        return new MipsModule(mipsGlobalVariables, mipsInstructions, mipsFunctions);
    }

    public void genMipsGlobalVariable(MipsGlobalVariable mipsGlobalVariable, int cnt) {
        this.symbolTable.getSymbolMap().put("@" +mipsGlobalVariable.getName(), cnt * 4);
        Li li = new Li(new MipsReg(8), ((IrIntConst)mipsGlobalVariable.getGlobalVariable().getIrConstant()).getVal());
        Sw sw = new Sw(new MipsReg(8), new MipsReg(28), cnt * 4);
        this.mipsInstructions.add(li);
        this.mipsInstructions.add(sw);
    }

}
