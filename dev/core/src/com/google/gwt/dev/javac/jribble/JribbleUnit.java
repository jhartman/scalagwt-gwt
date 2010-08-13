/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.dev.javac.jribble;

import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.jribble.ast.DeclaredType;

/**
 * A compiled unit of Jribble code. It corresponds to one Java class.
 */
public class JribbleUnit {
  private final String name;
  private final JDeclaredType syntaxTree;
  private final DeclaredType jribbleSyntaxTree;

  public JribbleUnit(String name, DeclaredType jribbleSyntaxTree,
      JDeclaredType syntaxTree) {
    this.name = name;
    this.syntaxTree = syntaxTree;
    this.jribbleSyntaxTree = jribbleSyntaxTree;
  }

  public String getName() {
    return name;
  }

  /**
   * Return the syntax tree. The {@link com.google.gwt.dev.jjs.ast.JNode}
   * hierarchy is mostly reused, but some node types replaced by Loose Java
   * equivalents. {@link com.google.gwt.dev.jjs.impl.GenerateJavaAST} can
   * convert this syntax tree to a proper JJS syntax tree.
   */
  public JDeclaredType getSyntaxTree() {
    return syntaxTree;
  }

  public DeclaredType getJribbleSyntaxTree() {
    return jribbleSyntaxTree;
  }

  @Override
  public String toString() {
    return name;
  }
}
