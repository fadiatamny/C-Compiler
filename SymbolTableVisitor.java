import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

public class SymbolTableVisitor extends CLangDefaultVisitor {

    Vector<String> _data;
    Vector<String> _text;
    int stackIndex = 0;
    int valuesCount;
    int globalCount = 0;
    int forCount = 0;
    boolean forStatus = false;

    public static class SymbolTableEntry {
        public String name;
        public String type;
        public int offset;

        public SymbolTableEntry(String name, String type, int offset) {
            this.name = name;
            this.type = type;
            this.offset = offset;
        }

    }

    public static class FunctionTableEntry {
        public Vector<SymbolTableEntry> variables;
        public String name;
        public String type;
        public int offset;

        public FunctionTableEntry(String name, String type, int offset) {
            this.name = name;
            this.type = type;
            this.offset = offset;
            this.variables = new Vector<SymbolTableEntry>();
        }
    }

    HashMap<String, FunctionTableEntry> functions = new HashMap<String, FunctionTableEntry>();
    Vector<HashMap<String, SymbolTableEntry>> symbols = new Vector<HashMap<String, SymbolTableEntry>>();
    int index = 0;

    public SymbolTableVisitor() {
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
        this._text = new Vector<>();
        this._data = new Vector<>();

    }

    public Object visit(ASTStart node, Object data) {
        Object o = super.visit(node, data);

        // System.out.println("SECTION .TEXT\n" + "GLOBAL main\n" + "\n" +
        // "printChar:\n" + " push rbp\n"
        // + " mov rbp, rsp\n" + " push rdi\n" + " mov byte [rbp - 5], 0x41\n" + " mov
        // byte [rbp - 4], 0x53\n"
        // + " mov byte [rbp - 3], 0x41\n" + " mov byte [rbp - 2], 0x46\n" + " mov byte
        // [rbp - 1], 0\n"
        // + " mov rax, 1\n" + " mov rdi, 1\n" + " lea rsi, [rbp -5]\n" + " mov rdx,
        // 5\n" + " syscall \n" + "\n"
        // + " mov rsp, rbp\n" + " pop rbp\n" + " ret\n" + "\n" + "printNumber:\n" + "
        // push rbp\n"
        // + " mov rbp, rsp\n" + " mov rsi, rdi\n" + " lea rdi, [rbp - 1]\n" + " mov
        // byte [rdi], 0\n"
        // + " mov rax, rsi\n" + " while:\n" + " cmp rax, 0\n" + " je done\n" + " mov
        // rcx, 10\n" + " mov rdx, 0\n"
        // + " div rcx\n" + " dec rdi\n" + " add dl, 0x30\n" + " mov byte [rdi], dl\n" +
        // " jmp while\n" + "\n"
        // + " done:\n" + " mov rax, 1\n" + " lea rsi, [rdi]\n" + " mov rdx, rsp\n" + "
        // sub rdx, rsi\n"
        // + " mov rdi, 1\n" + " syscall \n" + "\n" + " mov rsp, rbp\n" + " pop rbp\n" +
        // " ret\n" + "\n"
        // + "readInteger:\n" + " push rbp\n" + " mov rbp, rsp\n" + "\n" + " mov rdx,
        // 10\n"
        // + " mov qword [rbp - 10], 0\n" + " mov word [rbp - 2], 0\n" + " lea rsi, [rbp
        // - 10]\n"
        // + " mov rdi, 0 ; stdin\n" + " mov rax, 0 ; sys_read\n" + " syscall\n" + "\n"
        // + " xor rax, rax\n"
        // + " xor rbx, rbx\n" + " lea rcx, [rbp - 10]\n" + " \n" + " copy_byte:\n" + "
        // cmp rbx, 10\n"
        // + " je read_done \n" + " mov dl, byte [rcx]\n" + " cmp dl, 10\n" + " jle
        // read_done\n"
        // + " sub rdx, 0x30\n" + " imul rax, 10\n" + " add rax, rdx\n" + " nextchar:\n"
        // + " inc rcx\n"
        // + " inc rbx\n" + " jmp copy_byte\n" + " read_done:\n" + " mov rsp, rbp\n" + "
        // pop rbp\n" + " ret\n"
        // + "\n");
        for (String s : _text)
            System.out.println(s);
        return o;
    }

    public SymbolTableEntry resolve(String s) {
        // System.out.println("resolve stuff " + this.index);
        for (int i = this.index; i >= 0; --i) {
            SymbolTableEntry tmp = this.symbols.get(i).get(s);

            if (tmp != null)
                return tmp;
        }
        return null;
    }

    public FunctionTableEntry resolveFunc(String s) {
        FunctionTableEntry st = this.functions.get(s);
        if (st != null)
            return st;
        return null;
    }

    public void put(SymbolTableEntry s) {
        this.symbols.get(index).put(s.name, s);
    }

    public void putFunction(FunctionTableEntry s) {
        this.functions.put(s.name, s);
    }

    @Override
    public Object visit(ASTvarDefineDef node, Object data) {
        if (resolve(node.firstToken.next.image) != null) {
            System.err.println(String.format("ERROR: VAR %s REDEFINITION AT %d : %d", node.firstToken.next.image,
                    node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
            System.exit(-1);
        }

        boolean isInt = node.firstToken.image.equals("int");
        if (isInt)
            this.stackIndex += 4;
        else
            this.stackIndex++;

        SymbolTableEntry e = new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex);

        if (node.children.length > 0) {
            data = node.children[0].jjtAccept(this, data);
            _text.add("pop rax");
            _text.add(String.format("mov %s [rbp - %d], %s", isInt ? "dword" : "byte", e.offset, isInt ? "eax" : "al"));
        }

        put(e);
        return data;
    }

    @Override
    public Object visit(ASTaddExpressionDef node, Object data) {
        data = node.children[0].jjtAccept(this, data);
        if (node.children.length > 1) {
            String e = node.firstToken.next.image;
            if (e.equals("+")) {
                data = node.children[1].jjtAccept(this, data);
                _text.add("pop rbx");
                _text.add("pop rax");
                _text.add("add rax, rbx");
                _text.add("push rax");

            } else if (e.equals("-")) {
                data = node.children[1].jjtAccept(this, data);
                _text.add("pop rbx");
                _text.add("pop rax");
                _text.add("sub rax, rbx");
                _text.add("push rax");
            }
        }
        return data;
    }

    @Override
    public Object visit(ASTexpressionDef node, Object data) {
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTassignExpressionDef node, Object data) {
        if (resolve(node.firstToken.image) == null) {
            System.err.println(String.format("ASSING ERROR: VAR %s ISNT DEFINED AT %d : %d", node.firstToken.image,
                    node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
            System.exit(-1);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTconstExpressionDef node, Object data) {

        if (this.calling && this.valuesCount <= 0) {
            System.err.println(String.format("Too many variables sent to %s", this.funcName));
            System.exit(-1);
        }

        if (node.firstToken.kind == CLang.ID) {
            SymbolTableEntry e = resolve(node.firstToken.image);
            SymbolTableEntry var = resolve(node.firstToken.next.next.image);
            if (e == null) {
                System.err.println(String.format("Variable %s is not defined at %d : %d", node.firstToken.image,
                        node.firstToken.beginLine, node.firstToken.beginColumn));
                System.exit(-1);
            }
            if (this.calling) {
                FunctionTableEntry f = resolveFunc(this.funcName);
                for (SymbolTableEntry elem : f.variables) {
                    if (!elem.type.equals(e.type)) {
                        System.err.println(String.format(
                                "Variable %s ( %s ) is compatible type with what the function is expecting ( %s ) at %d : %d ",
                                node.firstToken.image, e.type, elem.type, node.firstToken.beginLine,
                                node.firstToken.beginColumn));
                        System.exit(-1);
                    }
                }
            }

            boolean isInt = e.type.equals("int");

            _text.add(String.format("mov %s, %s [rbp - %d]", isInt ? "eax" : "al", isInt ? "dword" : "byte", e.offset));
            _text.add("push rax");

        } else {
            boolean isInt = node.firstToken.kind == CLang.NUMBER;
            _text.add(String.format("mov %s, %s", isInt ? "eax" : "al", node.firstToken.image));
            _text.add("push rax");
        }

        if (this.calling)
            this.valuesCount--;

        return super.visit(node, data);

    }

    private boolean checkReturn(ASTfunctionDef node) {
        Token tmp = node.firstToken;
        while (tmp != null) {
            if (tmp.image.equals("return"))
                return true;
            tmp = tmp.next;
        }

        return false;
    }

    String funcName;
    boolean calling = false;

    @Override
    public Object visit(ASTfunctionDef node, Object data) {

        if (resolve(node.firstToken.next.image) != null) {
            System.err.println(String.format("ERROR: VAR %s REDEFINITION AT %d : %d", node.firstToken.next.image,
                    node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
            System.exit(-1);
        }
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
        this.calling = false;
        this.funcName = node.firstToken.next.image;

        put(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex));
        putFunction(new FunctionTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex));
        if (node.firstToken.image.equals("int"))
            if (!this.checkReturn(node)) {
                System.err
                        .println(String.format("ERROR FUNCTION %s DOES NOT HAVE RETURN!", node.firstToken.next.image));
                System.exit(-1);
            }

        _text.add(String.format("%s:", node.firstToken.next.image));
        _text.add("push rbp");
        _text.add("mov rbp, rsp");
        Object c = super.visit(node, data);
        _text.add("mov rsp, rbp");
        _text.add("pop rbp");
        _text.add("ret");
        this.symbols.remove(this.index);

        return c;
    }

    @Override
    public Object visit(ASTfunctionCallDef node, Object data) {
        if (resolveFunc(node.firstToken.image) == null) {
            System.err.println(String.format("ERROR: Function %s NOT DEFINED AT %d : %d", node.firstToken.image,
                    node.firstToken.beginLine, node.firstToken.beginColumn));
            System.exit(-1);
        }
        String tmpHolder = this.funcName;
        this.funcName = node.firstToken.image;
        this.calling = true;
        this.valuesCount = resolveFunc(this.funcName).variables.size();
        Object o = super.visit(node, data);

        this.calling = false;
        this.funcName = tmpHolder;
        return o;
    }

    @Override
    public Object visit(ASTReturnStatment node, Object data) {

        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTparamDef node, Object data) {
        Object res = super.visit(node, data);

        SymbolTableEntry e = new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex);
        if (node.firstToken.image.equals("int"))
            this.stackIndex += 4;
        else
            this.stackIndex += 1;
        put(e);

        FunctionTableEntry f = resolveFunc(this.funcName);
        f.variables.add(e);

        return res;
    }

    @Override
    public Object visit(ASTStatementBlockDef node, Object data) {
        ++this.index;
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
        Object c = super.visit(node, data);
        this.symbols.remove(this.index);
        --this.index;
        return c;
    }

    void printTokens(Token t) {
        Token tmp = t;
        while (tmp != null) {
            System.out.println(tmp.image);
            tmp = tmp.next;
        }
    }

    // @Override
    // public Object visit(ASTbinaryBoolExpressionOrDef node, Object data) {
    // Object ret = super.visit(node, data);

    // return ret;
    // }

    // @Override
    // public Object visit(ASTbinaryBoolExpressionAndDef node, Object data) {
    // Object ret = super.visit(node, data);

    // return ret;
    // }

    // if (node.children.length > 1) {
    // printTokens(node.firstToken);
    // String op = node.firstToken.next.image;
    // }
    // return ret;
    // }

    @Override
    public Object visit(ASTbinaryBoolExpressionOrDef node, Object data) {
        if (node.firstToken.next.kind != CLang.ID && node.firstToken.next.kind != CLang.NUMBER && node.firstToken.next.kind != CLang.CHAR_VALUE && this.forStatus){ 
            _text.add("whileStart"+this.globalCount+ ":");
            this.forStatus = false;
        }

        if (node.firstToken.next.kind == CLang.OR) {
            node.children[0].jjtAccept(this, data);
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("je true" + this.globalCount);
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("je true" + this.globalCount);
            _text.add("push 0");
            _text.add("jmp compdone");
            _text.add("true" + this.globalCount + ":");
            _text.add("push 1");
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTbinaryBoolExpressionAndDef node, Object data) {
        if (node.firstToken.next.kind == CLang.AND) {
            node.children[0].jjtAccept(this, data);
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("je true" + this.globalCount);
            _text.add("jmp false");
            _text.add("true" + this.globalCount + ":");
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("jne false");
            _text.add("push 1");
            _text.add("jmp compdone");
            _text.add("false" + this.globalCount + ":");
            _text.add("push 0");
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTbinaryExpressionEqDef node, Object data) {
        if (node.firstToken.next.kind == CLang.EQ) {
            node.children[0].jjtAccept(this, data);
            _text.add("pop rax");
            _text.add("mov rbx rax");
            _text.add("pop rax");
            _text.add("cmp rax rbx");
            _text.add("jne true" + this.globalCount);
            _text.add("push 1");
            _text.add("jmp compdone");
            _text.add("true" + this.globalCount + ":");
            _text.add("push 0");
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
        }
        if (node.firstToken.next.kind == CLang.NEQ) {
            node.children[0].jjtAccept(this, data);
            _text.add("pop rax");
            _text.add("mov rbx rax");
            _text.add("pop rax");
            _text.add("cmp rax rbx");
            _text.add("jne true" + this.globalCount);
            _text.add("push 0");
            _text.add("jmp compdone");
            _text.add("true" + this.globalCount + ":");
            _text.add("push 1");
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTbinaryBoolExpressionCompareDef node, Object data) {
        if (node.firstToken.next.kind == CLang.COMPAREOPS) {
            node.children[0].jjtAccept(this, data);
            String type = "";
            switch (node.firstToken.next.image) {
                case "<":
                    type = "jge";
                    break;
                case "<=":
                    type = "jg";
                    break;
                case ">":
                    type = "jle";
                    break;
                case ">=":
                    type = "jl";
                    break;
            }
            _text.add("pop rax");
            _text.add("mov rbx rax");
            _text.add("pop rax");
            _text.add("cmp rax rbx");
            _text.add(type + " true" + this.globalCount);
            _text.add("push 1");
            _text.add("jmp compdone" + this.globalCount);
            _text.add("true" + this.globalCount + ":");
            _text.add("push 0");
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
        }

        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTIfStatementDef node, Object data) {
        Object d = node.children[0].jjtAccept(this, data);
        if (node.firstToken.kind == CLang.IF) {
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("jne compdone" + this.globalCount);
            Object o = node.children[1].jjtAccept(this, data);;
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
            return o;
        } else
            return d;
    }

    @Override
    public Object visit(ASTWhileStatementDef node, Object data) {
        if (node.firstToken.kind == CLang.WHILE) 
            _text.add("whileStart"+this.globalCount+ ":");

        Object d = node.children[0].jjtAccept(this, data);
        if (node.firstToken.kind == CLang.WHILE) {
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("jne compdone" + this.globalCount);
            Object o = node.children[1].jjtAccept(this, data);
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("je whileStart" + this.globalCount);
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
            return o;
        } else
            return d;
    }

    @Override
    public Object visit(ASTForStatementDef node, Object data) {
        this.forStatus = true;
        Object d = node.children[0].jjtAccept(this, data);
        if (node.firstToken.kind == CLang.WHILE) {
            _text.add("pop rax");
            _text.add("cmp rax 1");
            _text.add("jne compdone" + this.globalCount);
            Object o = node.children[1].jjtAccept(this, data);
            
            // _text.add("pop rax");
            // _text.add("cmp rax 1");
            // _text.add("je whileStart" + this.forCount);
            _text.add("compdone" + this.globalCount + ":");
            this.globalCount++;
            this.forCount++;
            this.forStatus = false;
            return o;
        } else{
            this.forStatus = false;
            return d;
        }
    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {

        new CLang(new FileInputStream(args[0]));

        CLang.Start();

        CLang.jjtree.rootNode().jjtAccept(new SymbolTableVisitor(), null);
    }
}