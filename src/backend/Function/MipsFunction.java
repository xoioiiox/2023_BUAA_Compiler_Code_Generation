package backend.Function;

import backend.BasicBlock.MipsBasicBlock;

import java.util.ArrayList;

public class MipsFunction {
    private String funcName;
    private boolean isMainFunc;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;

    public MipsFunction(String funcName, boolean isMainFunc, ArrayList<MipsBasicBlock> mipsBasicBlocks) {
        this.funcName = funcName;
        this.isMainFunc = isMainFunc;
        this.mipsBasicBlocks = mipsBasicBlocks;
    }

    //函数也应当有一个跳转的label
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("#----------------函数入口-----------------\n");
        sb.append(this.funcName);
        sb.append(":\n");
        for (MipsBasicBlock basicBlock : mipsBasicBlocks) {
            sb.append(basicBlock.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
