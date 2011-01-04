/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.javac.CompilationStateBuilder;
import com.google.gwt.dev.javac.impl.MockJavaResource;
import com.google.gwt.dev.javac.impl.MockJribbleResource;
import com.google.gwt.dev.javac.impl.MockResourceOracle;
import com.google.gwt.dev.jjs.JavaAstConstructor;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A useful base class for tests that build JJS ASTs.
 */
public abstract class JribbleTestBase extends TestCase {

  public static final String MAIN_METHOD_NAME = "onModuleLoad";

  /**
   * Finds a field with a type.
   */
  public static JField findField(JDeclaredType type, String fieldName) {
    for (JField field : type.getFields()) {
      if (field.getName().equals(fieldName)) {
        return field;
      }
    }
    return null;
  }

  /**
   * Finds a field by name, e.g. <code>Foo.field</code>.
   */
  public static JField findField(JProgram program, String qualifiedFieldName) {
    int pos = qualifiedFieldName.lastIndexOf('.');
    assertTrue(pos > 0);
    String typeName = qualifiedFieldName.substring(0, pos);
    String fieldName = qualifiedFieldName.substring(pos + 1);
    JDeclaredType type = findType(program, typeName);
    JField field = findField(type, fieldName);
    return field;
  }

  /**
   * Find a local variable declared within a JMethod.
   */
  public static JLocal findLocal(JMethod method, final String localName) {
    JLocal found = null;
    if (method.getBody() instanceof JMethodBody) {
      JMethodBody body = (JMethodBody) method.getBody();
      for (JLocal x : body.getLocals()) {
        if (x.getName().equals(localName)) {
          found = x;
        }
      }
    } else {
      throw new UnsupportedOperationException("Operation not supported for "
          + "methods with bodies that are not instances of JMethodBody");
    }
    assert found != null;
    return found;
  }

  public static JMethod findMainMethod(JProgram program) {
    return findMethod(program, MAIN_METHOD_NAME);
  }

  public static JMethod findMethod(JDeclaredType type, String methodName) {
    for (JMethod method : type.getMethods()) {
      if (method.getName().equals(methodName)) {
        return method;
      }
    }

    return null;
  }

  public static JMethod findMethod(JProgram program, String methodName) {
    JDeclaredType mainType = program.getFromTypeMap("test.EntryPoint");
    return findMethod(mainType, methodName);
  }

  /**
   * Finds a type by name. The type name may be short, e.g. <code>"Foo"</code>,
   * or fully-qualified, e.g. <code>"com.google.example.Foo"</code>. If a short
   * name is used, it must be unambiguous.
   */
  public static JDeclaredType findType(JProgram program, String typeName) {
    JDeclaredType type = program.getFromTypeMap(typeName);
    if (type == null && typeName.indexOf('.') < 0) {
      // Do a slow lookup by short name.
      for (JDeclaredType checkType : program.getDeclaredTypes()) {
        if (checkType.getShortName().equals(typeName)) {
          if (type == null) {
            type = checkType;
          } else {
            fail("Ambiguous type reference '" + typeName + "' might be '"
                + type.getName() + "' or '" + checkType.getName()
                + "' (possibly more matches)");
          }
        }
      }
    }
    return type;
  }

  public static String getMainMethodSource(JProgram program) {
    JMethod mainMethod = findMainMethod(program);
    return mainMethod.getBody().toSource();
  }

  /**
   * Tweak this if you want to see the log output.
   */
  private static TreeLogger createTreeLogger() {
    boolean reallyLog = true;
    if (reallyLog) {
      AbstractTreeLogger logger = new PrintWriterTreeLogger();
      logger.setMaxDetail(TreeLogger.WARN);
      return logger;
    }
    return TreeLogger.NULL;
  }

  protected TreeLogger logger = createTreeLogger();

  protected final MockResourceOracle sourceOracle = new MockResourceOracle();

  private final Set<String> snippetClassDecls = new TreeSet<String>();

  public JribbleTestBase() {
    sourceOracle.add(JavaAstConstructor.getCompilerTypes());
  }

  /**
   * Adds a snippet of code, for example a field declaration, to the class that
   * encloses the snippet subsequently passed to
   * {@link #compileSnippet(String, String)}.
   */
  protected void addSnippetClassDecl(String fieldDecl) {
    snippetClassDecls.add(fieldDecl);
  }

  /**
   * Returns the program that results from compiling the specified code snippet
   * as the body of an entry point method.
   * 
   * @param returnType the return type of the method to compile; use "void" if
   *          the code snippet has no return statement
   * @param codeSnippet the body of the entry method
   */
  protected JProgram compileSnippet(final String returnType,
      final String codeSnippet) throws UnableToCompleteException {
    return compileSnippet(returnType, "", codeSnippet);
  }

  /**
   * Returns the program that results from compiling the specified code snippet
   * as the body of an entry point method.
   * 
   * @param returnType the return type of the method to compile; use "void" if
   *          the code snippet has no return statement
   * @param params the parameter list of the method to compile
   * @param codeSnippet the body of the entry method
   */
  protected JProgram compileSnippet(final String returnType,
      final String params, final String codeSnippet)
      throws UnableToCompleteException {
    MockJribbleResource entryPoint = new MockJribbleResource("test.EntryPoint") {
      @Override
      protected CharSequence getContent() {
        StringBuffer code = new StringBuffer();
        code.append("public class Ltest/EntryPoint; {\n");
        for (String snippetClassDecl : snippetClassDecls) {
          code.append(snippetClassDecl + ";\n");
        }
        code.append("  public static " + returnType + " onModuleLoad(" + params
            + ") {\n");
        code.append(codeSnippet);
        code.append("  }\n");
        code.append("}\n");
        return code;
      }
    };
    return compileClasses(Collections.singletonList(entryPoint),
        "test.EntryPoint");
  }

  protected JProgram compileClasses(final List<MockJribbleResource> resources,
      String entryPoint) throws UnableToCompleteException {
    for (MockJribbleResource x : resources) {
      sourceOracle.addOrReplace(x);
    }
    addBuiltinClasses(sourceOracle);
    CompilationState state = CompilationStateBuilder.buildFrom(logger,
        sourceOracle.getResources());
    JProgram program = JavaAstConstructor.construct(logger, state, entryPoint,
        "com.google.gwt.lang.Exceptions");
    return program;
  }

  protected void addBuiltinClasses(MockResourceOracle sourceOracle) {
    sourceOracle.addOrReplace(new MockJavaResource("java.lang.RuntimeException") {
      @Override
      protected CharSequence getContent() {
        return "package java.lang;"
            + "public class RuntimeException extends Exception { }";
      }
    });

    sourceOracle.addOrReplace(new MockJavaResource(
        "com.google.gwt.lang.Exceptions") {
      @Override
      protected CharSequence getContent() {
        return "package com.google.gwt.lang;"
            + "public class Exceptions { static boolean throwAssertionError() { throw new RuntimeException(); } }";
      }
    });

    sourceOracle.addOrReplace(new MockJavaResource("java.lang.String") {
      @Override
      protected CharSequence getContent() {
        return "package java.lang;" + "public class String {"
            + "  public int length() { return 0; }"
            + "  public char charAt(int pos) { return 0; }" + "}";
      }
    });

    sourceOracle.addOrReplace(new MockJribbleResource("scala.ScalaObject") {
      @Override
      protected CharSequence getContent() {
        return "public abstract interface Lscala/ScalaObject; extends Ljava/lang/Object; {"
            + "}";
      }
    });
  }
}