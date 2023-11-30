package midend.llvmIr.value.function;

public class NameCnt {
    private int cnt = 0;
    private int blockCnt = 0;
    public NameCnt() {}

    public int getCnt() {
        int res = cnt;
        cnt++;
        return res;
    }

    public int getCntOnly() {
        return cnt;
    }

    //public int getBlockCntOnly() {return blockCnt;}
}
