package backend;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsSymbolTable {
    private MipsCnt mipsCnt;
    private static int sumOffset;
    private HashMap<String, Integer> symbolMap;
    private ArrayList<String> specialBase;
    private MipsSymbolTable prev;

    public MipsSymbolTable(MipsSymbolTable prev, int sumOffset) {
        MipsSymbolTable.sumOffset = sumOffset;
        this.prev = prev;
        this.symbolMap = new HashMap<>();
        this.specialBase = new ArrayList<>();
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
