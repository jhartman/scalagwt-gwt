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
import com.google.gwt.dev.jjs.ast.JProgram;

import com.google.jribble.DefParser;
import com.google.jribble.ast.ClassDef;
import com.google.jribble.ast.InterfaceDef;

import java.io.Reader;

import scala.Either;

/**
 * Parses Loose Java into a syntax tree.
 *
 */
public class JribbleParser {

  private static final DefParser parser = new DefParser();

  public static JDeclaredType parse(JProgram program, Reader source) {

    Either<ClassDef, InterfaceDef> def = parser.parse(source);

    if (def.isLeft()) {
      ClassDef classDef = def.left().get();
      return (new JribbleTransformer(program)).classDef(classDef);
    } else {
      InterfaceDef interfaceDef = def.right().get();
      return (new JribbleTransformer(program)).interfaceDef(interfaceDef);
    }
  }
}
