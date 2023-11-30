package backend;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsSymbolTable {
    private int sumOffset;
    private HashMap<String, Integer> symbolMap;
    private ArrayList<String> usedTempVals;
    private ArrayList<String> localVals;
    private ArrayList<String> specialBase;

    public MipsSymbolTable() {
        this.sumOffset = 0;
        this.symbolMap = new HashMap<>();
        this.usedTempVals = new ArrayList<>();
        this.localVals = new ArrayList<>();
        this.specialBase = new ArrayList<>();
    }

    public void addLocalVal(String name) {
        this.localVals.add(name);
    }

    // 这里的isTemp包括临时变量，常数，全局变量
    public boolean isLocalVal(String name) {
        return this.localVals.contains(name);
    }

    public void addUsedTempVal(String name) {
        this.usedTempVals.add(name);
    }

    public boolean isUsedTempVal(String name) {
        return usedTempVals.contains(name);
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
        if (name != null && !this.symbolMap.containsKey(name)) {
            throw new RuntimeException("haven't record symbol");
        }
        if (this.symbolMap.containsKey(name)) {
            return this.symbolMap.get(name);
        }
        else {
            int res = sumOffset;
            sumOffset += 4;
            return res;
        }
    }

    public void changeOffset(int num) {
        sumOffset += num;
    }

    public int getSumOffset() {
        return sumOffset;
    }
}
