package com.google.gwt.dev.jjs.impl;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.jjs.ast.JIfStatement;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JProgram;

public class JribbleStatementTest extends JribbleTestBase {
  
  public void testIf() throws UnableToCompleteException {
    String snippet = "if (true) {\n" +
      "  true;\n" +
      "} else {\n" +
      "  false;\n" +
      "}";
    JProgram program = compileSnippet("V", snippet);
    JMethodBody body = (JMethodBody) findMainMethod(program).getBody();
    assertEquals(1, body.getStatements().size());
    JIfStatement ifStmt = (JIfStatement) body.getStatements().get(0);
    System.out.println(ifStmt);
    assertEquals(snippet, ifStmt.toString());
  }
  
}
