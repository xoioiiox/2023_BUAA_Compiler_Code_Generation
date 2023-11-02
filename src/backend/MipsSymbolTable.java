package backend;

import java.util.HashMap;

public class MipsSymbolTable {
    private MipsCnt mipsCnt;
    private static int sumOffset;
    private HashMap<String, Integer> symbolMap;
    private MipsSymbolTable prev;

    public MipsSymbolTable(MipsSymbolTable prev, int sumOffset) {
        MipsSymbolTable.sumOffset = sumOffset;
        this.prev = prev;
        this.symbolMap = new HashMap<>();
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

    public int getSumOffset() {
        return sumOffset;
    }
}
