package midend.llvmIr.value.constant;

import midend.llvmIr.type.IrArrayType;
import midend.llvmIr.type.IrValueType;

import java.util.ArrayList;

public class IrArrayConst extends IrConstant{
    private ArrayList<IrConstant> val;

    public IrArrayConst(String name, IrValueType valueType, ArrayList<IrConstant> val) {
        super(name, valueType); //valueType 应当为数组的类型
        this.val = val;
    }

    //@c = dso_local global
    // [5 x [5 x i32]]
    // [[5 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0],
    // [5 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5],
    // [5 x i32] zeroinitializer,
    // [5 x i32] zeroinitializer,
    // [5 x i32] zeroinitializer]
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.val.size() == 0) {
            sb.append("zeroinitializer");
        }
        else {
            IrValueType type = this.getValueType();
            sb.append(type.toString());
            sb.append(" ");
            sb.append("[");
            sb.append(this.val.get(0).toString());
            for (int i = 1; i < val.size(); i++) {
                sb.append(", ");
                sb.append(this.val.get(i).toString());
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
