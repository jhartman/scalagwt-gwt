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
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.javac.impl.MockJribbleResource;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JProgram;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InheritanceJribbleTest extends JribbleTestBase {

  private List<MockJribbleResource> classes() {
    MockJribbleResource inter = new MockJribbleResource("test.A") {
      @Override
      protected CharSequence getContent() {
        return "public abstract interface Ltest/A; {\n"
            + "  public V foo() {\n" + "  }\n" + " }";
      }
    };

    MockJribbleResource baseClass = new MockJribbleResource("test.B") {
      @Override
      protected CharSequence getContent() {
        return "public class Ltest/B; extends Ljava/lang/Object; {\n"
            + "  public V baseFoo() { \"\"; }\n" + " }";
      }
    };

    MockJribbleResource clazz = new MockJribbleResource("test.C") {
      @Override
      protected CharSequence getContent() {
        return "public final class Ltest/C; extends Ltest/B; implements Ltest/A; {\n"
            + "  public V foo() { \"\"; }\n"
            + "  public V bar() { \"\"; }\n"
            + " }";
      }
    };

    List<MockJribbleResource> classes = new LinkedList<MockJribbleResource>();
    classes.add(inter);
    classes.add(baseClass);
    classes.add(clazz);
    return classes;
  }

  private JDeclaredType a;
  private JDeclaredType b;
  private JDeclaredType c;
  private JProgram program;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    program = compileClasses(classes(), "test.C");
    a = program.getFromTypeMap("test.A");
    b = program.getFromTypeMap("test.B");
    c = program.getFromTypeMap("test.C");
    assert a != null && b != null && c != null;
  }

  public void testExtendsInformation() {
    assertEquals(program.getTypeJavaLangObject(), b.getSuperClass());
    assertEquals(b, c.getSuperClass());
  }

  public void testImplementsInformation() {
    assertEquals(Collections.singletonList(a), c.getImplements());
    assertEquals(Collections.EMPTY_LIST, b.getImplements());
    assertEquals(Collections.EMPTY_LIST, a.getImplements());
  }

  public void testMethodOverridesCalculation() throws Exception {
    JMethod fooInC = findMethod(c, "foo");
    JMethod fooInA = findMethod(a, "foo");
    assertEquals(Collections.singletonList(fooInA), fooInC.getOverrides());
    JMethod bar = findMethod(c, "bar");
    assertEquals(Collections.EMPTY_LIST, bar.getOverrides());
  }

}
