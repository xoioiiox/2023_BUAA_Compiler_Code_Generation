package backend;

import backend.Insturction.*;

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
                int offset;
                if (this.symbolTable.getSymbolMap().containsKey(name)) {
                    offset = this.symbolTable.getOffset(name);
                }
                else {
                    offset = this.symbolTable.getOffset(null);
                    this.symbolTable.getSymbolMap().put(name, offset);
                }
                Sw sw = new Sw(new MipsReg(8 + i), new MipsReg(30), offset);
                mipsInstructions.add(sw);
                isUsed.set(i, false);
            }
        }
        this.symbol_RegMap = new HashMap<>();
        this.reg_SymbolMap = new HashMap<>();
        this.protectList = new ArrayList<>();
        ptr = 0;
    }

    public void cleanProtectList() {
        this.protectList.clear();
    }

    public void addProtectList(Integer integer) {
        this.protectList.add(integer);
    }

    public MipsReg findReg(ArrayList<MipsInstruction> mipsInstructions, String objectName) {
        if (this.symbol_RegMap.containsKey(objectName)) {
            return symbol_RegMap.get(objectName);
        }
        // 如果不在现有的寄存器中，那么分配一个寄存器，将其值从内存中调入
        MipsReg reg = getEmptyReg(mipsInstructions, objectName);
        if (isConst(objectName)) {
            Li li = new Li(reg, Integer.parseInt(objectName));
            mipsInstructions.add(li);
        }
        else {
            int offset = this.symbolTable.getOffset(objectName); // 一定已经分配内存了！！
            Lw lw = new Lw(reg, new MipsReg(30), offset);
            mipsInstructions.add(lw);
        }
        return reg;
    }

    public MipsReg getEmptyReg(ArrayList<MipsInstruction> mipsInstructions, String newName) {
        for (int i = 0; i < 8; i++) {
            if (!isUsed.get(i)) { // 有空闲寄存器直接分配
                isUsed.set(i, true);
                reg_SymbolMap.put(i, newName);
                symbol_RegMap.put(newName, new MipsReg(i + 8));
                return new MipsReg(i + 8);
            }
            else {
                String oldName = reg_SymbolMap.get(i);
                if (symbolTable.isUsed(oldName) && !this.protectList.contains(i + 8)) {
                    if (i == ptr) {
                        ptr = (ptr + 1) % 8;
                    }
                    // 已经使用过的临时变量不需要写回
                    reg_SymbolMap.put(i, newName);
                    symbol_RegMap.remove(oldName);
                    symbol_RegMap.put(newName, new MipsReg(i + 8));
                    return new MipsReg(i + 8);
                }
            }
        }
        /* 没找到空闲寄存器，需要选择一个（最旧的）寄存器存入内存*/
        for (int i = 0; i < 8; i++) {
            int num = (i + ptr) % 8;
            if (protectList.contains(num + 8)) {
                continue;
            }
            if (num == ptr) {
                ptr = (ptr + 1) % 8;
            }
            String oldName = reg_SymbolMap.get(num);
            int offset;
            if (this.symbolTable.getSymbolMap().containsKey(oldName)) {
                offset = this.symbolTable.getOffset(oldName);
            }
            else {
                offset = this.symbolTable.getOffset(null);
                this.symbolTable.getSymbolMap().put(oldName, offset);
            }
            Sw sw = new Sw(new MipsReg(num + 8), new MipsReg(30), offset);
            mipsInstructions.add(sw);
            reg_SymbolMap.put(num, newName);
            symbol_RegMap.remove(oldName);
            symbol_RegMap.put(newName, new MipsReg(num + 8));
            return new MipsReg(num + 8);
        }
        return null;
    }

    public boolean isConst(String name) {
        return !name.contains("@") && !name.contains("%");
    }

}
