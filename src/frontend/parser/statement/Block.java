package frontend.parser.statement;

import java.util.ArrayList;

public class Block extends Stmt {
    ArrayList<BlockItem> blockItems;

    public Block(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }
}
