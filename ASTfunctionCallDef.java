/* Generated By:JJTree: Do not edit this line. ASTfunctionCallDef.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTfunctionCallDef extends SimpleNode {
  public ASTfunctionCallDef(int id) {
    super(id);
  }

  public ASTfunctionCallDef(CLang p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CLangVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=4154d7d3adc1fc2404ef5539c4ec44d3 (do not edit this line) */