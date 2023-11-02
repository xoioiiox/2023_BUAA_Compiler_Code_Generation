package midend.llvmIr.value.instruction.terminator;

import midend.llvmIr.IrValue;
import midend.llvmIr.type.IrIntType;
import midend.llvmIr.type.IrValueType;
import midend.llvmIr.value.instruction.IrInstruction;

import java.util.ArrayList;

public class IrCall extends IrInstruction {
    private String funcName;
    private int c = 0;
    private IrValue value;
    private IrValueType intType = new IrIntType(32);
    private boolean isGetInt = false;
    private boolean isPutCh = false;
    private boolean isPutInt = false;
    private boolean isHasExpReturn = false;
    private boolean isNoExpReturn = false;


    //getint
    public IrCall(String name, IrValueType irValueType, String funcName) {
        super(irValueType);
        this.setName(name);
        this.funcName = funcName;
        this.isGetInt = true;
    }

    //putch
    public IrCall(IrValueType irValueType, String funcName, char c) {
        super(irValueType);
        this.funcName = funcName;
        this.c = c;
        this.isPutCh = true;
    }

    //putint
    public IrCall(IrValueType irValueType, String funcName, IrValue value) {
        super(irValueType);
        this.funcName = funcName;
        this.value = value;
        this.isPutInt = true;
    }

    //无返回值
    public IrCall(IrValueType irValueType, IrValue function, ArrayList<IrValue> params) {
        super(irValueType);
        this.setOperand(0, function);
        for (int i = 0; i < params.size(); i++) {
            this.setOperand(i + 1, params.get(i));
        }
        this.isNoExpReturn = true;
    }

    //有返回值
    public IrCall(String name, IrValueType irValueType, IrValue function, ArrayList<IrValue> params) {
        super(irValueType);
        this.setOperand(0, function);
        for (int i = 0; i < params.size(); i++) {
            this.setOperand(i + 1, params.get(i));
        }
        this.setName(name);
        this.isHasExpReturn = true;
    }

    public boolean isGetInt() {
        return isGetInt;
    }

    public boolean isPutCh() {
        return isPutCh;
    }

    public boolean isPutInt() {
        return isPutInt;
    }

    public boolean isHasExpReturn() {
        return isHasExpReturn;
    }

    public boolean isNoExpReturn() {
        return isNoExpReturn;
    }

    public int getC() {
        return c;
    }

    public IrValue getValue() {
        return value;
    }

    /**
     * aaa(a, b)
     * %5 = load i32, i32* %2
     * %6 = load i32, i32* %1
     * %7 = call i32 @aaa(i32 %5, i32 %6) #左值是自然产生的临时变量，用于接收函数返回值
     */
    @Override
    public String toString() {
        if (isPutCh) {
            return "call " + this.getValueType().toString()
                    + " " + this.funcName
                    + " (" + this.intType.toString() + " " + c + ")";
        }
        else if (isPutInt) {
            return "call " + this.getValueType().toString()
                    + " " + this.funcName
                    + "(" + this.intType.toString() + " " + value.getName() + ")";
        }
        else if (isGetInt) {
            return this.getName() + " = call " + this.getValueType().toString() + " " + this.funcName + "()";
        }
        else {
            StringBuilder sb = new StringBuilder();
            if (isHasExpReturn) {
                sb.append(this.getName());
                sb.append(" = ");
            }
            sb.append("call ");
            sb.append(this.getValueType().toString());
            sb.append(" ");
            sb.append(this.getOperand(0).getName());
            sb.append("(");
            IrValue value1 = this.getOperand(1);
            if (value1 != null) {
                sb.append(value1.getValueType().toString());
                sb.append(" ");
                sb.append(value1.getName());
                value1 = this.getOperand(2);
                int i = 2;
                while (value1 != null) {
                    sb.append(", ");
                    sb.append(value1.getValueType().toString());
                    sb.append(" ");
                    sb.append(value1.getName());
                    i++;
                    value1 = this.getOperand(i);
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
