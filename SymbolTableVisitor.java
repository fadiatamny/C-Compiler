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
    HashMap<String, SymbolTableEntry> functions = new HashMap<String, SymbolTableEntry>();
    Vector<HashMap<String, SymbolTableEntry>> symbols = new Vector<HashMap<String, SymbolTableEntry>>();
    int index = 0;

    public SymbolTableVisitor() {
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
    }

    public SymbolTableEntry resolve(String s) {
        for (int i = this.index; i >= 0; --i) {
            SymbolTableEntry tmp = this.symbols.get(i).get(s);
            if (tmp != null)
                return tmp;
        }
        return null;
    }

    public SymbolTableEntry resolveFunc(String s) {
        SymbolTableEntry st = this.functions.get(s);
        if(st != null)
            return st;
        return null;
    }

    public void put(SymbolTableEntry s) {
        this.symbols.get(index).put(s.name, s);
    }

    
    public void putFunction(SymbolTableEntry s) {
        this.functions.put(s.name, s);
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
    public Object visit(ASTunaryExpressionDef node, Object data) {
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTaddExpressionDef node, Object data) {
        data = node.children[0].jjtAccept(this, data);
        if (node.children.length > 1)
        {
            data = node.children[1].jjtAccept(this, data);
        }
        return data;
    }

    @Override
    public Object visit(ASTassignExpressionDef node, Object data) {
        if (resolve(node.firstToken.image) == null) {
            System.err.println(String.format("ASSING ERROR: VAR %s ISNT DEFINED AT %d : %d", node.firstToken.image,
                    node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
            System.exit(-1);
        }
        return super.visit(node,data);
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
    public Object visit(ASTfunctionDef node, Object data) {
        if (resolve(node.firstToken.next.image) != null) {
            System.err.println(String.format("ERROR: VAR %s REDEFINITION AT %d : %d", node.firstToken.next.image,
                    node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
            System.exit(-1);
        }
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
        put(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image));
        putFunction(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image));
        ++this.index;

        Object c = super.visit(node, data);

        this.index--;
        return c;
    }

    @Override
    public Object visit(ASTfunctionCallDef node, Object data)
    {
        if (resolveFunc(node.firstToken.image) == null) {
            System.err.println(String.format("ERROR: Function %s NOT DEFINED AT %d : %d", node.firstToken.image,
                    node.firstToken.beginLine, node.firstToken.beginColumn));
            System.exit(-1);
        }
        return super.visit(node,data);
    }

    @Override
    public Object visit(ASTparamDef node, Object data) {
        put(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image));
        return super.visit(node, data);
    }


    @Override
    public Object visit(ASTStatementBlockDef node, Object data) {
        ++this.index;
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);

        Object c = super.visit(node, data);

        this.symbols.remove(this.index);
        this.index--;

        return c;
    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {

        new CLang(new FileInputStream(args[0]));

        CLang.Start();

        CLang.jjtree.rootNode().jjtAccept(new SymbolTableVisitor(), null);
    }
}