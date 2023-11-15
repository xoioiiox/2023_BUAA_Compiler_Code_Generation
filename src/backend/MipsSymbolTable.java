package backend;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsSymbolTable {
    private static int sumOffset;
    private HashMap<String, Integer> symbolMap;
    private ArrayList<String> isUsed;
    private ArrayList<String> notTemp;
    private ArrayList<String> specialBase;
    private MipsSymbolTable prev;

    public MipsSymbolTable(MipsSymbolTable prev, int sumOffset) {
        MipsSymbolTable.sumOffset = sumOffset;
        this.prev = prev;
        this.symbolMap = new HashMap<>();
        this.isUsed = new ArrayList<>();
        this.notTemp = new ArrayList<>();
        this.specialBase = new ArrayList<>();
    }

    public void addNotTemp(String name) {
        this.notTemp.add(name);
    }

    // 这里的isTemp包括临时变量，常数，全局变量
    public boolean isTemp(String name) {
        return !this.notTemp.contains(name);
    }

    public void addUsed(String name) {
        this.isUsed.add(name);
    }

    public boolean isUsed(String name) {
        return isUsed.contains(name);
    }

    public void addSpecialBase(String name) {
        this.specialBase.add(name);
    }

    public ArrayList<String> getSpecialBase() {
        return specialBase;
    }

    public HashMap<String, Integer> getSymbolMap() {
        return symbolMap;
    }

    public int getOffset(String name) {
        if (this.symbolMap.containsKey(name)) {
            return this.symbolMap.get(name);
        }
        else {
            if (prev == null) {
                int res = sumOffset;
                sumOffset += 4;
                return res;
            }
            return this.prev.getOffset(name);
        }
    }

    public void changeOffset(int num) {
        sumOffset += num;
    }

    public int getSumOffset() {
        return sumOffset;
    }
}
