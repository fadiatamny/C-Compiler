import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

public class SymbolTableVisitor extends CLangDefaultVisitor {

    Vector<String> _data;
    Vector<String> _text;
    int stackIndex = 0;
    int gOffset = 0;
    

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

    public static class FunctionTableEntry extends SymbolTableEntry {
        public String name;
        public String type;
        public int offset;
        public Vector<SymbolTableEntry> variables;

        public FunctionTableEntry(String name, String type, int offset) {
            super(name, type, offset);
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

        System.out.println("SECTION .TEXT\n" + "GLOBAL main\n" + "\n" + "printChar:\n" + " push rbp\n"
                + " mov rbp, rsp\n" + " push rdi\n" + " mov byte [rbp - 5], 0x41\n" + " mov byte [rbp - 4], 0x53\n"
                + " mov byte [rbp - 3], 0x41\n" + " mov byte [rbp - 2], 0x46\n" + " mov byte [rbp - 1], 0\n"
                + " mov rax, 1\n" + " mov rdi, 1\n" + " lea rsi, [rbp -5]\n" + " mov rdx, 5\n" + " syscall \n" + "\n"
                + " mov rsp, rbp\n" + " pop rbp\n" + " ret\n" + "\n" + "printNumber:\n" + " push rbp\n"
                + " mov rbp, rsp\n" + " mov rsi, rdi\n" + " lea rdi, [rbp - 1]\n" + " mov byte [rdi], 0\n"
                + " mov rax, rsi\n" + " while:\n" + " cmp rax, 0\n" + " je done\n" + " mov rcx, 10\n" + " mov rdx, 0\n"
                + " div rcx\n" + " dec rdi\n" + " add dl, 0x30\n" + " mov byte [rdi], dl\n" + " jmp while\n" + "\n"
                + " done:\n" + " mov rax, 1\n" + " lea rsi, [rdi]\n" + " mov rdx, rsp\n" + " sub rdx, rsi\n"
                + " mov rdi, 1\n" + " syscall \n" + "\n" + " mov rsp, rbp\n" + " pop rbp\n" + " ret\n" + "\n"
                + "readInteger:\n" + " push rbp\n" + " mov rbp, rsp\n" + "\n" + " mov rdx, 10\n"
                + " mov qword [rbp - 10], 0\n" + " mov word [rbp - 2], 0\n" + " lea rsi, [rbp - 10]\n"
                + " mov rdi, 0 ; stdin\n" + " mov rax, 0 ; sys_read\n" + " syscall\n" + "\n" + " xor rax, rax\n"
                + " xor rbx, rbx\n" + " lea rcx, [rbp - 10]\n" + " \n" + " copy_byte:\n" + " cmp rbx, 10\n"
                + " je read_done \n" + " mov dl, byte [rcx]\n" + " cmp dl, 10\n" + " jle read_done\n"
                + " sub rdx, 0x30\n" + " imul rax, 10\n" + " add rax, rdx\n" + " nextchar:\n" + " inc rcx\n"
                + " inc rbx\n" + " jmp copy_byte\n" + " read_done:\n" + " mov rsp, rbp\n" + " pop rbp\n" + " ret\n"
                + "\n");
        for (String s : _text)
            System.out.println(s);
        return o;
    }

    public SymbolTableEntry resolve(String s) {
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
        // System.out.println("ASTvarDefineDef num of chidren= "+node.children.length);
        // System.out.println("ASTvarDefineDef node children[0]= "+node.children[0]);

        boolean isInt = node.firstToken.image.equals("int");
        if (isInt)
            this.stackIndex += 4;
        else
            this.stackIndex++;

        SymbolTableEntry e = new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex);
        // System.out.println("ASTvarDefineDef 1 data= "+data);
        // asembly
        // data = super.visit(node, data);
        if (node.children.length > 0) {
            // System.out.println("ASTvarDefineDef 1 offset= "+e.offset);
            data = node.children[0].jjtAccept(this, data);
            // System.out.println("ASTvarDefineDef 2 data= "+data);
            // System.out.println("ASTvarDefineDef 2 offset= "+e.offset);
            _text.add("pop rax");
            _text.add(String.format("mov %s [rbp - %d], %s", isInt ? "dword" : "byte", e.offset, isInt ? "eax" : "al"));
        }

        put(e);
        return data;
        // return super.visit(node, data);
    }

    // @Override
    // public Object visit(ASTboolExpressionDef node, Object data) {
    // Object ret = super.visit(node, data);
    // return ret;
    // }

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

    // void printTokens(Token t){
    // Token tmp = t;
    // while(tmp != null){
    // System.out.println(tmp.image);
    // tmp = tmp.next;
    // }
    // }

    // @Override
    // public Object visit(ASTbinaryBoolExpressionCompareDef node, Object data) {
    // Object ret = super.visit(node, data);
    // System.out.println("printing bool exp comp");

    // if (node.children.length > 1) {
    // printTokens(node.firstToken);
    // String op = node.firstToken.next.image;
    // }
    // return ret;
    // }

    // @Override
    // public Object visit(ASTunaryExpressionDef node, Object data) {
    // Object ret = super.visit(node, data);
    // return ret;
    // }

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
        if (node.firstToken.kind == CLang.ID) {
            SymbolTableEntry e = resolve(node.firstToken.image);
            SymbolTableEntry var = resolve(node.firstToken.next.next.image);
            if (e == null) {
                System.err.println(String.format("Variable %s is not defined at %d : %d", node.firstToken.image,
                        node.firstToken.beginLine, node.firstToken.beginColumn));
                System.exit(-1);
            }

            // System.out.println("ASTconstExpressionDef e = " + e);
            // System.out.println("ASTconstExpressionDef var = " + var);

            boolean isInt = e.type.equals("int");
            gOffset = e.offset;

            _text.add(String.format("mov %s, %s [rbp - %d]", isInt ? "eax" : "al", isInt ? "dword" : "byte", e.offset));
            _text.add("push rax");
        }
        // System.out.println("ASTconstExpressionDef no if");

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

    @Override
    public Object visit(ASTfunctionDef node, Object data) {
        // System.out.println("ASTfunctionDef");
        if (resolve(node.firstToken.next.image) != null) {
            System.err.println(String.format("ERROR: VAR %s REDEFINITION AT %d : %d", node.firstToken.next.image,
                    node.firstToken.next.beginLine, node.firstToken.next.beginColumn));
            System.exit(-1);
        }
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);
        // System.out.println("node.firstToken.image = " + node.firstToken.image);
        // System.out.println("node.firstToken.next.image = " +
        // node.firstToken.next.image);
        put(new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex));
        putFunction(new FunctionTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex));
        if (node.firstToken.image.equals("int"))
            if (!this.checkReturn(node)) {
                System.err.println(String.format("ERROR FUNCTION DOES NOT HAVE RETURN!"));
                System.exit(-1);
            }

        ++this.index;

        _text.add(String.format("%s:", node.firstToken.next.image));
        _text.add("push rbp");
        _text.add("mov rbp, rsp");
        Object c = super.visit(node, data);
        _text.add("mov rsp, rbp");
        _text.add("pop rbp");
        _text.add("ret");

        this.index--;
        return c;
    }

    @Override
    public Object visit(ASTfunctionCallDef node, Object data) {
        if (resolveFunc(node.firstToken.image) == null) {
            System.err.println(String.format("ERROR: Function %s NOT DEFINED AT %d : %d", node.firstToken.image,
                    node.firstToken.beginLine, node.firstToken.beginColumn));
            System.exit(-1);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTReturnStatment node, Object data) {

        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTparamDef node, Object data) {
        Object res = super.visit(node, data);

        SymbolTableEntry e = new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex);
        this.stackIndex += 4;
        put(e);

        return res;
    }

    @Override
    public Object visit(ASTStatementBlockDef node, Object data) {
        ++this.index;
        // System.out.println("StatementBlockDef");
        HashMap<String, SymbolTableEntry> s = new HashMap<>();
        this.symbols.add(s);

        Object c = super.visit(node, data);

        this.symbols.remove(this.index);
        this.index--;

        return c;
    }

    @Override
    public Object visit(ASTStatementDef node, Object data) {
        // System.out.println("ASTStatementDef first= "+node.firstToken.image);
        // System.out.println("ASTStatementDef secend= "+node.firstToken.next.image);
        // System.out.println("ASTStatementDef 3=
        // "+node.firstToken.next.next.next.image);
        // SymbolTableEntry e = resolve(node.firstToken.next.next.image);

        // boolean isIf = node.firstToken.image.equals("if");
        // boolean isFor = node.firstToken.image.equals("for");
        // boolean isWhile = node.firstToken.image.equals("while");

        // for(int i = 0; i < node.children.length; i++)
        // {
        // System.out.println("the array"+node.children[i]);
        // }

        if (node.firstToken.image.equals("if")) {
            _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));
            switch (node.firstToken.next.next.next.image) {
                case ">":
                    _text.add("jl end");
                    break;
                case "<":
                    _text.add("jg end");
                    break;
                case "==":
                    _text.add("jen end");
                    break;
                case "=>":
                    _text.add("jle end");
                    break;
                case "=<":
                    _text.add("jge end");
                    break;
                case "!=":
                    _text.add("je end");
                    break;
                case "!":
                    //
                    break;
            }

            super.visit(node, data);
            // _text.add("mov eax, $x");
            // _text.add("cmp eax, 0");
            // _text.add("jne end");
            // _text.add(" mov eax, 1");
            // _text.add("end:");
        } else if (node.firstToken.image.equals("for")) // not fin
        {
            SymbolTableEntry tmp = resolve(String.format("%s", node.firstToken.next.next));
            _text.add(String.format("mov [rbp - %d], %s",tmp.offset,node.firstToken.next.next.next.next.image));
            //data = node.children[0].jjtAccept(this, data);
            

            _text.add(String.format("cmp eax %s",node.firstToken.next.next.next.next.next.next.next.next.image));
            switch (node.firstToken.next.next.next.next.next.next.next.image) {
                case ">":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jl end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add("inc eax");
                    _text.add(String.format("cmp eax %s",node.firstToken.next.next.next.next.next.next.next.next.image));// not complited;
                    _text.add("jg beginning");
                    _text.add("end:");
                    break;

                case "<":
                    // _text.add("cmp eax x ");//not complited;
                    _text.add("jg end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add("inc eax");
                    _text.add(String.format("cmp eax %s",node.firstToken.next.next.next.next.next.next.next.next.image));// not complited;
                    _text.add("jl beginning");
                    _text.add("end:");
                    break;
                case "==":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jne end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add("inc eax");
                    _text.add(String.format("cmp eax %s",node.firstToken.next.next.next.next.next.next.next.next.image));// not complited;
                    _text.add("je beginning");
                    _text.add("end:");
                    break;
                case "=>":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jle end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add("inc eax ");
                    _text.add(String.format("cmp eax %s",node.firstToken.next.next.next.next.next.next.next.next.image));// not complited;
                    _text.add("jge beginning");
                    _text.add("end:");
                    break;
                case "=<":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jge end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add("inc eax");
                    _text.add(String.format("cmp eax %s",node.firstToken.next.next.next.next.next.next.next.next.image));// not complited;
                    _text.add("jle beginning");
                    _text.add("end:");
                    break;
                case "!=":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("je end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add("inc eax");
                    _text.add(String.format("cmp eax %s",node.firstToken.next.next.next.next.next.next.next.next.image));// not complited;
                    _text.add("jne beginning");
                    _text.add("end:");
                    break;
                case "!":
                    //
                    break;
            }
            // mov c 0
            // cmp i c
            // jg end
            // beginning:
            // //
            // //
            // inc i
            // cmp i c
            // jl beginning
            // end:
            // //
            // //

        }

        else if (node.firstToken.image.equals("while")) // not fin
        {
            _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));
            switch (node.firstToken.next.next.next.image) {
                case ">":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jl end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));// not complited;
                    _text.add("jg beginning");
                    _text.add("end:");
                    break;

                case "<":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jg end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));
                    _text.add("jl beginning");
                    _text.add("end:");
                    break;
                case "==":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jne end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));
                    _text.add("je beginning");
                    _text.add("end:");
                    break;
                case "=>":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jle end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));
                    _text.add("jge beginning");
                    _text.add("end:");
                    break;
                case "=<":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("jge end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));
                    _text.add("jle beginning");
                    _text.add("end:");
                    break;
                case "!=":
                    // _text.add("cmp eax ");//not complited;
                    _text.add("je end");
                    _text.add("beginning:");
                    data = node.children[0].jjtAccept(this, data);
                    _text.add(String.format("cmp eax %s ", node.firstToken.next.next.next.next.image));
                    _text.add("jne beginning");
                    _text.add("end:");
                    break;
                case "!":
                    //
                    break;
            }

            // mov eax, $x
            // cmp eax, 0x0A
            // jg end
            // beginning:
            // inc eax
            // cmp eax, 0x0A
            // jle beginning
            // end:
        }

        Object c = super.visit(node, data);
        return c;
    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {

        new CLang(new FileInputStream(args[0]));

        CLang.Start();

        CLang.jjtree.rootNode().jjtAccept(new SymbolTableVisitor(), null);
    }
}