/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.dev.javac.jribble.ast;

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JType;

import java.util.List;

/**
 * A method call that uses a {@Link JribMethodRef} to refer to the called
 * method.
 */
@SuppressWarnings("serial")
public class JribNewInstance extends JribMethodCall {

  private final JribConstructorRef constructor;
  private final JType type;

  public JribNewInstance(SourceInfo sourceInfo, JribConstructorRef constructorRef,
      List<JExpression> arguments, JType type) {
    super(sourceInfo, constructorRef, null, arguments, type);
    this.constructor = constructorRef;
    this.type = type;
  }

  @Override
  public JType getType() {
    return this.type;
  }

}
