package midend.llvmIr.type;

import java.util.ArrayList;

public class IrFunctionType extends IrValueType {
    private IrValueType retType;
    private ArrayList<IrValueType> paramTypes; // 函数形参对应的参数类型
    private ArrayList<String> paramNames; // 函数形参分配的寄存器名

    public IrFunctionType(IrValueType retType, ArrayList<IrValueType> paramTypes, ArrayList<String> paramNames) {
        this.retType = retType;
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
    }

    public IrValueType getRetType() {
        return retType;
    }

    public ArrayList<String> getParamNames() {
        return paramNames;
    }

    public ArrayList<IrValueType> getParamTypes() {
        return paramTypes;
    }
}
