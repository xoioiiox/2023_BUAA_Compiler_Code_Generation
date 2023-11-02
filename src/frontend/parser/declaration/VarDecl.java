package frontend.parser.declaration;

import java.util.ArrayList;

public class VarDecl extends Decl {
    private Btype btype;
    private ArrayList<VarDef> varDefs;

    public VarDecl(Btype btype, ArrayList<VarDef> varDefs) {
        this.btype = btype;
        this.varDefs = varDefs;
    }

    public ArrayList<VarDef> getVarDefs() {
        return varDefs;
    }
}
