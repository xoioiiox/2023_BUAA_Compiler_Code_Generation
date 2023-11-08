package midend.llvmIr.value.function;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrFunctionType;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.basicBlock.IrBasicBlock;
import midend.llvmIr.value.instruction.IrInstruction;

import java.util.ArrayList;

public class IrFunction extends IrValue {
    private boolean isMainFunc;
    private ArrayList<IrBasicBlock> basicBlocks;
    //private ArrayList<IrInstruction> paramInst;

    public IrFunction(String name, IrValueType irValueType,
                      boolean isMainFunc, ArrayList<IrBasicBlock> basicBlocks){
                      //ArrayList<IrInstruction> paramInst) {
        super(name, irValueType);
        this.isMainFunc = isMainFunc;
        this.basicBlocks = basicBlocks;
        //this.paramInst = paramInst;
    }

    public boolean isMainFunc() {
        return isMainFunc;
    }

    public ArrayList<IrBasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void setBasicBlocks(ArrayList<IrBasicBlock> basicBlocks) {
        this.basicBlocks = basicBlocks;
    }

    /**
     * define dso_local i32 @main() {
     *     %1 = sub i32 0, 1
     *     ret i32 %27
     * }
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ");
        sb.append(((IrFunctionType)this.getValueType()).getRetType().toString());
        sb.append(" ");
        sb.append(this.getName());
        sb.append("(");
        ArrayList<IrValueType> paramTypes = ((IrFunctionType)this.getValueType()).getParamTypes();
        ArrayList<String> paramNames = ((IrFunctionType)this.getValueType()).getParamNames();
        if (paramTypes.size() != 0) {
            IrValueType paramType1 = paramTypes.get(0);
            sb.append(paramType1.toString());
            sb.append(" ");
            sb.append(paramNames.get(0));
            for (int i = 1; i < paramTypes.size(); i++) {
                sb.append(", ");
                sb.append(paramTypes.get(i).toString());
                sb.append(" ");
                sb.append(paramNames.get(i));
            }
        }
        sb.append(")");
        sb.append(" {");
        /*for (IrInstruction instruction : paramInst) {
            sb.append("\n");
            sb.append(instruction.toString());
        }*/
        for (IrBasicBlock basicBlock : this.basicBlocks) {
            sb.append(basicBlock.toString());
        }
        sb.append("\n}\n");
        return sb.toString();
    }
}
