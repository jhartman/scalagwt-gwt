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
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;

/**
 * Represents an array dimension that was not specified in an array
 * instantiation expression.
 */
public class JAbsentArrayDimension extends JLiteral {

  public static final JExpression INSTANCE = new JAbsentArrayDimension(
      SourceOrigin.UNKNOWN);

  /**
   * These are only supposed to be constructed by JProgram.
   */
  JAbsentArrayDimension(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public JType getType() {
    return JPrimitiveType.VOID;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }

  private Object readResolve() {
    return INSTANCE;
  }
}
