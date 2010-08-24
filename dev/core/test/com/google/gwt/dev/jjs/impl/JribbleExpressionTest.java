package com.google.gwt.dev.jjs.impl;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.jjs.ast.JConditional;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JProgram;

public class JribbleExpressionTest extends JribbleTestBase {
  
  public void testConditional() throws UnableToCompleteException {
    //TODO(grek): Right now it's always required that conditional expression is put
    //in parenthesis, this requirement should be removed where there is no problem with
    //parsing the expression
    JProgram program = compileSnippet("V", "(true ?(Ljava/lang/String;) \"0\" : \"1\");");
    JMethodBody body = (JMethodBody) findMainMethod(program).getBody();
    assertEquals(1, body.getStatements().size());
    JExpressionStatement exprStmt = (JExpressionStatement) body.getStatements().get(0); 
    JConditional x = (JConditional) (exprStmt).getExpr();
    assertEquals("true ? \"0\" : \"1\"", x.toString());
    assertEquals(program.getTypeJavaLangString(), x.getType());
  }
  
}
