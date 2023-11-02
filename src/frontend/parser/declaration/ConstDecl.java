package frontend.parser.declaration;

import java.util.ArrayList;

public class ConstDecl extends Decl {
    private Btype btype;
    private ArrayList<ConstDef> constDefs;

    public ConstDecl(Btype btype, ArrayList<ConstDef> constDefs) {
        this.btype = btype;
        this.constDefs = constDefs;
    }

    public ArrayList<ConstDef> getConstDefs() {
        return constDefs;
    }
}
