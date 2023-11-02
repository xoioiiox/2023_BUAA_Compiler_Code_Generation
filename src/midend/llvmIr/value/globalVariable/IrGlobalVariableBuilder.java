package midend.llvmIr.value.globalVariable;

import frontend.parser.declaration.*;
import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.value.constant.IrIntConst;
import midend.symbol.SymbolCon;
import midend.symbol.SymbolTable;

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
        SymbolCon symbol = new SymbolCon(constDef.getIdent().getVal(), constDef.getIdent().getLineNum(), dim);
        if (constDef.getConstInitVal() != null) {
            symbol.setInitVal(constDef.getConstInitVal().calculate(symbolTable));
        }
        symbolTable.addSymbol(symbol);
        if (dim == 0) {
            IrIntType irIntType = new IrIntType(32);
            //todo 未初始化全局变量？
            IrValue irIntConst = new IrIntConst("@" + constDef.getIdent().getVal(), irIntType, symbol.getInitVal());
            symbol.setValue(irIntConst);
            irGlobalVariable  = new IrGlobalVariable("@" + constDef.getIdent().getVal(), irIntConst, true, irIntType);
        }
        else if (dim == 1) {
            //todo 一维数组
        }
        else if (dim == 2) {
            //todo 二维数组
        }
        return irGlobalVariable;
    }

    //全局变量
    public IrGlobalVariable genIrGlobalVar(VarDef varDef) {
        IrGlobalVariable irGlobalVariable = null;
        int dim = varDef.getDimension();
        SymbolCon symbol = new SymbolCon(varDef.getIdent().getVal(), varDef.getIdent().getLineNum(), dim);
        if (varDef.getInitVal() != null) {
            symbol.setInitVal(varDef.getInitVal().calculate(symbolTable));
        }
        symbolTable.addSymbol(symbol);
        if (dim == 0) {
            IrIntType irIntType = new IrIntType(32);
            IrValue irIntConst = new IrIntConst("@" + varDef.getIdent().getVal(), irIntType, symbol.getInitVal());
            symbol.setValue(irIntConst);
            irGlobalVariable  = new IrGlobalVariable("@" + varDef.getIdent().getVal(), irIntConst, false, irIntType);
        }
        else if (dim == 1) {
            //todo 一维数组
        }
        else if (dim == 2) {
            //todo 二维数组
        }
        return irGlobalVariable;
    }

}
