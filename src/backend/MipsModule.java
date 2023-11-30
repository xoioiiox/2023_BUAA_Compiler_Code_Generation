package backend;

import backend.Function.MipsFunction;

import java.util.ArrayList;

public class MipsModule {
    private ArrayList<MipsGlobalVariable> mipsGlobalVariables;
    private ArrayList<MipsFunction> mipsFunctions;

    public MipsModule(
            ArrayList<MipsGlobalVariable> mipsGlobalVariables,
            ArrayList<MipsFunction> mipsFunctions) {
        this.mipsGlobalVariables = mipsGlobalVariables;
        this.mipsFunctions = mipsFunctions;
    }

    public void printMips() {
        System.out.println("#----------全局变量区-----------");
        System.out.println(".data");
        for (MipsGlobalVariable mipsGlobalVariable : mipsGlobalVariables) {
            System.out.println(mipsGlobalVariable.toString());
        }
        System.out.println(".text");
        System.out.println("li $fp, 0x10040000");
        System.out.println("j main\n");
        System.out.println("#----------全局函数及主函数-----------");
        for (MipsFunction mipsFunction : mipsFunctions) {
            System.out.println(mipsFunction.toString());
        }
    }
}
