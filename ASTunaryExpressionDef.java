/* Generated By:JJTree: Do not edit this line. ASTunaryExpressionDef.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTunaryExpressionDef extends SimpleNode {
  public ASTunaryExpressionDef(int id) {
    super(id);
  }

  public ASTunaryExpressionDef(CLang p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CLangVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=8bc2a4a7ab20681c7581c39d0871baa6 (do not edit this line) */