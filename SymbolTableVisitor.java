import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

public class SymbolTableVisitor extends CLangDefaultVisitor {

    public static class SymbolTableEntry {
        public String name;
        public String type;

        public SymbolTableEntry(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    Vector<HashMap<String, SymbolTableEntry>> symbols = new Vector<HashMap<String, SymbolTableEntry>>();

    public SymbolTableVisitor() {
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
    }

    public SymbolTableEntry resolve(String s) {
        return symbols.get(0).get(s);
    }

    public void put(SymbolTableEntry s) {
        symbols.get(0).put(s.name, s);
    }

    @Override
    public Object visit(ASTvarDefineDef node, Object data) {
        if (resolve(node.firstToken.next.image) != null) {
            System.err.println(String.format("ERROR: VAR %s REDEFINITION AT %d : %d", node.firstToken.next.image,
                    node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
            System.exit(-1);
        }
        put(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image));
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTconstExpressionDef node, Object data) {

        if (node.firstToken.kind == CLang.ID) {
            if (resolve(node.firstToken.image) == null) {
                System.err.println(String.format("Variable %s is not defined at %d : %d", node.firstToken.image,
                        node.firstToken.beginLine, node.firstToken.beginColumn));
                System.exit(-1);
            }
        }

        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTparamDef node, Object data) {
        put(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image));
        return super.visit(node, data);
    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {
        new CLang(new FileInputStream(args[0]));

        CLang.Start();

        CLang.jjtree.rootNode().jjtAccept(new SymbolTableVisitor(), null);
    }
}