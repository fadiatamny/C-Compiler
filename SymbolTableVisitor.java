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

    public void put(SymbolTableEntry s) {
        this.symbols.get(index).put(s.name, s);
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

    /**
    * this is the same as the one below it was the code i used to check if the ID in the assignment exists 
    * but it was skipping it ofr the other check. since they are the same
    */
    
    // @Override
    // public Object visit(ASTvarAssignDef node, Object data) {
    //     System.out.println(node.firstToken.image);
    //     if (resolve(node.firstToken.next.image) == null) {
    //         System.err.println(String.format("ERROR: VAR %s ISNT DEFINED AT %d : %d", node.firstToken.next.image,
    //                 node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
    //         System.exit(-1);
    //     }
    //     return super.visit(node,data);
    // }

    @Override
    public Object visit(ASTassignExpressionDef node, Object data) {
        System.out.println(node.firstToken.image);
        if (resolve(node.firstToken.next.image) == null) {
            System.err.println(String.format("ERROR: VAR %s ISNT DEFINED AT %d : %d", node.firstToken.next.image,
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
        ++this.index;
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
        put(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image));

        Object c = super.visit(node, data);

        this.symbols.remove(this.index);
        this.index--;
        return c;
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