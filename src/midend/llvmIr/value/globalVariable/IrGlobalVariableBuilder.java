package midend.llvmIr.value.globalVariable;

import frontend.parser.declaration.*;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrArrayType;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.value.constant.IrArrayConst;
import midend.llvmIr.value.constant.IrConstant;
import midend.llvmIr.value.constant.IrIntConst;
import midend.symbol.SymbolCon;
import midend.symbol.SymbolTable;
import midend.symbol.SymbolVar;

import java.util.ArrayList;

public class IrGlobalVariableBuilder {
    private Decl decl;
    private SymbolTable symbolTable;

    public IrGlobalVariableBuilder(Decl decl, SymbolTable symbolTable) {
        this.decl = decl;
        this.symbolTable = symbolTable;
    }

    public ArrayList<IrGlobalVariable> genIrGlobalVariable() {
        ArrayList<IrGlobalVariable> irGlobalVariables = new ArrayList<>();
        if (this.decl instanceof ConstDecl) {
            for (ConstDef constDef : ((ConstDecl) decl).getConstDefs()) {
                irGlobalVariables.add(genIrGlobalConstVar(constDef));
            }
        }
        else if (this.decl instanceof VarDecl) {
            for (VarDef varDef : ((VarDecl)decl).getVarDefs()) {
                irGlobalVariables.add(genIrGlobalVar(varDef));
            }
        }
        return irGlobalVariables;
    }

    //全局常数变量
    public IrGlobalVariable genIrGlobalConstVar(ConstDef constDef) {
        IrGlobalVariable irGlobalVariable = null;
        int dim = constDef.getDimension();
        SymbolCon symbolCon = new SymbolCon(constDef.getIdent().getVal(), 0, dim);
        symbolTable.addSymbol(symbolCon);
        if (dim == 0) { //todo 符号表中存的名字带不带符号？？
            if (constDef.getConstInitVal() != null) {
                symbolCon.setInitVal(constDef.getConstInitVal().calculate(symbolTable));
            }
            else { //未初始化
                symbolCon.setInitVal(0);
            }
            IrIntType irIntType = new IrIntType(32);
            IrValue irIntConst = new IrIntConst("@" + constDef.getIdent().getVal(), irIntType, symbolCon.getInitVal());
            symbolCon.setValue(irIntConst);
            irGlobalVariable  = new IrGlobalVariable("@" + constDef.getIdent().getVal(), irIntConst, true, irIntType);
        }
        else if (dim == 1) {
            ArrayList<IrConstant> initVal = new ArrayList<>();
            if (constDef.getConstInitVal() != null) {
                for (ConstInitVal constInitVal : constDef.getConstInitVal().getConstInitVals()) {
                    IrIntConst irIntConst = new IrIntConst("#", new IrIntType(32), constInitVal.calculate(symbolTable));
                    initVal.add(irIntConst);
                }
                symbolCon.setInitValArray(initVal);
            } // 未初始化？
            int eleNum = constDef.getConstExps().get(0).calculate(symbolTable);
            IrArrayType irArrayType = new IrArrayType(new IrIntType(32), eleNum);
            IrArrayConst irArrayConst = new IrArrayConst("@" + constDef.getIdent().getVal(), irArrayType, initVal);
            symbolCon.setValue(irArrayConst);
            irGlobalVariable = new IrGlobalVariable("@" + constDef.getIdent().getVal(), irArrayConst, true, irArrayType);
        }
        else if (dim == 2) {
            int eleNum1 = constDef.getConstExps().get(0).calculate(symbolTable);
            int eleNum2 = constDef.getConstExps().get(1).calculate(symbolTable);
            IrArrayType irArrayType1 = new IrArrayType(new IrIntType(32), eleNum2); //2
            IrArrayType irArrayType = new IrArrayType(irArrayType1, eleNum1, eleNum2);
            ArrayList<IrConstant> initVal = new ArrayList<>();
            if (constDef.getConstInitVal() != null) {
                for (ConstInitVal constInitVal : constDef.getConstInitVal().getConstInitVals()) {
                    ArrayList<IrConstant> initVal1 = new ArrayList<>();
                    for (ConstInitVal constInitVal1 : constInitVal.getConstInitVals()) {
                        IrIntConst irIntConst = new IrIntConst("#", new IrIntType(32), constInitVal1.calculate(symbolTable));
                        initVal1.add(irIntConst);
                    }
                    IrArrayConst irArrayConst = new IrArrayConst("#", irArrayType1, initVal1);
                    initVal.add(irArrayConst);
                }
            }
            IrArrayConst irArrayConst = new IrArrayConst("@" + constDef.getIdent().getVal(), irArrayType, initVal);
            symbolCon.setValue(irArrayConst);
            irGlobalVariable = new IrGlobalVariable("@" + constDef.getIdent().getVal(), irArrayConst, true, irArrayType);
        }
        return irGlobalVariable;
    }

    //全局变量
    public IrGlobalVariable genIrGlobalVar(VarDef varDef) {
        IrGlobalVariable irGlobalVariable = null;
        int dim = varDef.getDimension();
        SymbolVar symbolVar = new SymbolVar(varDef.getIdent().getVal(), 0, dim);
        symbolTable.addSymbol(symbolVar);
        if (dim == 0) { // 符号表中存的名字带不带符号？？
            if (varDef.getInitVal() != null) {
                symbolVar.setInitVal(varDef.getInitVal().calculate(symbolTable));
            }
            else { //未初始化
                symbolVar.setInitVal(0);
            }
            IrIntType irIntType = new IrIntType(32);
            IrValue irIntConst = new IrIntConst("@" + varDef.getIdent().getVal(), irIntType, symbolVar.getInitVal());
            symbolVar.setValue(irIntConst);
            irGlobalVariable  = new IrGlobalVariable("@" + varDef.getIdent().getVal(), irIntConst, false, irIntType);
        }
        else if (dim == 1) {
            ArrayList<IrConstant> initVal = new ArrayList<>();
            if (varDef.getInitVal() != null) {
                for (InitVal initVal1 : varDef.getInitVal().getInitVals()) {
                    IrIntConst irIntConst = new IrIntConst("#", new IrIntType(32), initVal1.calculate(symbolTable));
                    initVal.add(irIntConst);
                }
                symbolVar.setInitValArray(initVal);
            } // 未初始化？
            int eleNum = varDef.getConstExps().get(0).calculate(symbolTable);
            IrArrayType irArrayType = new IrArrayType(new IrIntType(32), eleNum);
            IrArrayConst irArrayConst = new IrArrayConst("@" + varDef.getIdent().getVal(), irArrayType, initVal);
            symbolVar.setValue(irArrayConst);
            irGlobalVariable = new IrGlobalVariable("@" + varDef.getIdent().getVal(), irArrayConst, false, irArrayType);
        }
        else if (dim == 2) {
            int eleNum1 = varDef.getConstExps().get(0).calculate(symbolTable);
            int eleNum2 = varDef.getConstExps().get(1).calculate(symbolTable);
            IrArrayType irArrayType1 = new IrArrayType(new IrIntType(32), eleNum2); //2
            IrArrayType irArrayType = new IrArrayType(irArrayType1, eleNum1, eleNum2);
            ArrayList<IrConstant> initVal = new ArrayList<>();
            if (varDef.getInitVal() != null) {
                for (InitVal initVal_ : varDef.getInitVal().getInitVals()) {
                    ArrayList<IrConstant> initVal1 = new ArrayList<>();
                    for (InitVal initVal2 : initVal_.getInitVals()) {
                        IrIntConst irIntConst = new IrIntConst("#", new IrIntType(32), initVal2.calculate(symbolTable));
                        initVal1.add(irIntConst);
                    }
                    IrArrayConst irArrayConst = new IrArrayConst("#", irArrayType1, initVal1);
                    initVal.add(irArrayConst);
                }
            }
            IrArrayConst irArrayConst = new IrArrayConst("@" + varDef.getIdent().getVal(), irArrayType, initVal);
            symbolVar.setValue(irArrayConst);
            irGlobalVariable = new IrGlobalVariable("@" + varDef.getIdent().getVal(), irArrayConst, false, irArrayType);
        }
        return irGlobalVariable;
    }

}
