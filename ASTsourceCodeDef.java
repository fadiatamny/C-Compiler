/* Generated By:JJTree: Do not edit this line. ASTsourceCodeDef.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTsourceCodeDef extends SimpleNode {
  public ASTsourceCodeDef(int id) {
    super(id);
  }

  public ASTsourceCodeDef(CLang p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CLangVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=6b625284031c7e542fa2f94be11360fb (do not edit this line) */