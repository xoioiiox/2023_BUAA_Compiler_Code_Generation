package backend;

import backend.BasicBlock.MipsBasicBlock;
import backend.Insturction.*;
import midend.symbol.Symbol;
import midend.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 使用临时寄存器池管理和分配临时寄存器
 * 每个函数分配一个新的寄存器管理表
 */
public class MipsRegManager {
    private MipsSymbolTable symbolTable;
    private HashMap<String, MipsReg> symbol_RegMap;
    private HashMap<Integer, String> reg_SymbolMap;
    private ArrayList<Boolean> isUsed; // $t0 - $t7 (8-15)
    private int ptr; //指向第一个空余寄存器
    private ArrayList<Integer> protectList;

    public MipsRegManager(MipsSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.symbol_RegMap = new HashMap<>();
        this.reg_SymbolMap = new HashMap<>();
        this.isUsed = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            isUsed.add(false);
        }
        this.protectList = new ArrayList<>();
        this.ptr = 0;
    }

    public void restore(ArrayList<MipsInstruction> mipsInstructions) {
        for (int i = 0; i < 8; i++) {
            if (isUsed.get(i)) {
                String name = reg_SymbolMap.get(i);
                int offset = this.symbolTable.getOffset(name);
                Sw sw = new Sw(new MipsReg(8 + i), new MipsReg(30), offset);
                mipsInstructions.add(sw);
                isUsed.set(i, false);
            }
        }
        this.symbol_RegMap = new HashMap<>();
        this.reg_SymbolMap = new HashMap<>();
        this.protectList = new ArrayList<>();
    }

    public void cleanProtectList() {
        this.protectList.clear();
    }

    public void addProtectList(Integer integer) {
        this.protectList.add(integer);
    }

    public MipsReg findReg(ArrayList<MipsInstruction> mipsInstructions, String objectName) {
        if (this.symbol_RegMap.containsKey(objectName)) {
            //this.isArray.put(objectName, isArray);
            return symbol_RegMap.get(objectName);
        }
        MipsReg reg = getEmptyReg(mipsInstructions, objectName);
        if (isConst(objectName)) {
            Li li = new Li(reg, Integer.parseInt(objectName));
            mipsInstructions.add(li);
        }
        /*else if (isGlobal(objectName)) {
            if (isArray) {
                La la = new La(reg, objectName.substring(1));
                mipsInstructions.add(la);
            }
            else {
                Lw lw = new Lw(reg, objectName.substring(1));
                mipsInstructions.add(lw);
            }
        }*/
        else { //todo 会出现多余的lw
            int offset = this.symbolTable.getOffset(objectName);
            Lw lw = new Lw(reg, new MipsReg(30), offset);
            mipsInstructions.add(lw);
        }
        return reg;
    }

    public MipsReg getEmptyReg(ArrayList<MipsInstruction> mipsInstructions, String newName) {
        for (int i = 0; i < 8; i++) {
            if (!isUsed.get(i)) { // 有空闲寄存器直接分配
                isUsed.set(i, true);
                // 更新symbol
                //this.isArray.put(newName, isArray);
                reg_SymbolMap.put(i, newName);
                symbol_RegMap.put(newName, new MipsReg(i + 8));
                return new MipsReg(i + 8);
            }
            else {
                String oldName = reg_SymbolMap.get(i);
                if (symbolTable.isUsed(oldName) && !this.protectList.contains(i + 8)) {
                    isUsed.set(i, true);
                    // 更新symbol
                    if (i == ptr) {
                        ptr = (ptr + 1) % 8;
                    }
                    int offset = this.symbolTable.getOffset(oldName);
                    Sw sw = new Sw(new MipsReg(i + 8), new MipsReg(30), offset);
                    mipsInstructions.add(sw);
                    //this.isArray.put(newName, isArray);
                    reg_SymbolMap.put(i, newName);
                    symbol_RegMap.remove(oldName);
                    symbol_RegMap.put(newName, new MipsReg(i + 8));
                    return new MipsReg(i + 8);
                }
            }
        }
        /* 没找到空闲寄存器，需要选择一个（最旧的）寄存器存入内存*/
        for (int i = ptr; i < ptr + 8; i++) {
            int num = i % 8;
            if (protectList.contains(num + 8)) {
                continue;
            }
            if (num == ptr) {
                ptr = (ptr + 1) % 8;
            }
            String replaceName = reg_SymbolMap.get(num);
            // 将symbol写回
            int offset = this.symbolTable.getOffset(replaceName);
            Sw sw = new Sw(new MipsReg(num + 8), new MipsReg(30), offset);
            mipsInstructions.add(sw);
            // 更新symbol
            //this.isArray.put(newName, isArray);
            reg_SymbolMap.put(num, newName);
            symbol_RegMap.remove(replaceName);
            symbol_RegMap.put(newName, new MipsReg(num + 8));
            return new MipsReg(num + 8);
        }
        return null;
    }

    public boolean isConst(String name) {
        return !name.contains("@") && !name.contains("%");
    }

    public boolean isGlobal(String name) {
        return name.contains("@");
    }

}
