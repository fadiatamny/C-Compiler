/* Generated By:JJTree: Do not edit this line. ASTmulExpressionDef.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTmulExpressionDef extends SimpleNode {
  public ASTmulExpressionDef(int id) {
    super(id);
  }

  public ASTmulExpressionDef(CLang p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CLangVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=d30dfb4405f161459c0450343cec4f80 (do not edit this line) */