/* Generated By:JavaCC: Do not edit this line. CLangDefaultVisitor.java Version 7.0.5 */
public class CLangDefaultVisitor implements CLangVisitor{
  public Object defaultVisit(SimpleNode node, Object data){
    node.childrenAccept(this, data);
    return data;
  }
  public Object visit(SimpleNode node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTStart node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTsourceCodeDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTparamListDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTparamDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTfunctionDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTvarAssignDefInInit node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTvarAssignDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTvarDefineDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTStatementBlockDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTStatementDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTIfStatementDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTexpressionDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTassignExpressionDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTboolExpressionDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTbinaryBoolExpressionOrDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTbinaryBoolExpressionAndDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTbinaryExpressionEqDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTbinaryBoolOperatorCompareDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTbinaryBoolExpressionCompareDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTaddExpressionDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTmulExpressionDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTunaryExpressionDef node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTconstExpressionDef node, Object data){
    return defaultVisit(node, data);
  }
}
/* JavaCC - OriginalChecksum=44c8b4f8d427e874ae9000e819415433 (do not edit this line) */
