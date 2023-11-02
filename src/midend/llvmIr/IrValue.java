package midend.llvmIr;

import midend.llvmIr.type.IrValueType;

import java.util.ArrayList;

public class IrValue {
    private String name;
    private IrValueType irValueType;
    private ArrayList<IrUse> uses;

    public IrValue(String name, IrValueType valueType) {
        this.name = name;
        this.irValueType = valueType;
        this.uses = new ArrayList<>();
    }

    public IrValue(IrValueType valueType) {
        this.name = "";
        this.irValueType = valueType;
        this.uses = new ArrayList<>();
    }

    public IrValue(String name) { //basicBlock
        this.name = name;
    }

    public void addUse(IrUse use) {
        this.uses.add(use);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public IrValueType getValueType() {
        return irValueType;
    }
}
