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

package com.google.gwt.dev.javac.jribble;

import static com.google.gwt.dev.jjs.SourceOrigin.UNKNOWN;

import com.google.gwt.dev.javac.jribble.ast.JribConstructorRef;
import com.google.gwt.dev.javac.jribble.ast.JribMethodCall;
import com.google.gwt.dev.javac.jribble.ast.JribMethodRef;
import com.google.gwt.dev.javac.jribble.ast.JribNewInstance;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JConstructor;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JInterfaceType;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVariableRef;
import com.google.jribble.ast.Array;
import com.google.jribble.ast.Assignment;
import com.google.jribble.ast.ClassDef;
import com.google.jribble.ast.Constructor;
import com.google.jribble.ast.ConstructorStatement;
import com.google.jribble.ast.Expression;
import com.google.jribble.ast.InterfaceDef;
import com.google.jribble.ast.Literal;
import com.google.jribble.ast.MethodCall;
import com.google.jribble.ast.MethodDef;
import com.google.jribble.ast.MethodStatement;
import com.google.jribble.ast.NewCall;
import com.google.jribble.ast.ParamDef;
import com.google.jribble.ast.Primitive;
import com.google.jribble.ast.Ref;
import com.google.jribble.ast.Signature;
import com.google.jribble.ast.StaticMethodCall;
import com.google.jribble.ast.StringLiteral;
import com.google.jribble.ast.SuperConstructorCall;
import com.google.jribble.ast.ThisRef$;
import com.google.jribble.ast.Type;
import com.google.jribble.ast.VarDef;
import com.google.jribble.ast.VarRef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class that transforms jribble AST into GWT AST.
 *
 * It's important to note that this class makes use of artificial (throw-away)
 * instance of JProgram and thus produces AST which has to be further
 * processed.
 *
 * Basically, this class creates a skeleton for GWT's AST with node
 * place-holders like JribMethodCall that are replaced by real nodes like
 * JMethodCall.
 *
 * We place-holders because at the point of execution of JribbleTransformer
 * there is no valid JProgram instance that is needed to create GWT AST.
 *
 */
public class JribbleTransformer {

  private final JProgram program;

  public JribbleTransformer(JProgram program) {
    this.program = program;
    // FIXME (grek): workaround that initializes JProgram's state so we can
    // use JProgram.getStringLiteral safely. To be removed once we work with
    // real JProgram.
    program.createClass(UNKNOWN, "java.lang.String", false, true);
  }

  public JExpressionStatement assignment(Assignment assignment,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JMethodBody enclosingBody, JClassType enclosingClass) {
    JLocal local = findLocal(enclosingBody.getLocals(), assignment.name());
    JLocalRef ref = new JLocalRef(UNKNOWN, local);
    JExpression expr = expression(assignment.value(), varDict, paramDict, enclosingClass);
    return JProgram.createAssignmentStmt(UNKNOWN, ref, expr);
  }

  public JClassType classDef(ClassDef classDef) {
    String name = refName(classDef.name());
    boolean isFinal = classDef.modifs().contains("final");
    boolean isAbstract = classDef.modifs().contains("abstract");
    JClassType clazz = new JClassType(UNKNOWN, name, isAbstract, isFinal);
    List<Constructor> constructors = classDef.jconstructors();
    List<MethodDef> methods = classDef.jmethodDefs();
    if (classDef.ext().isDefined()) {
      clazz.setSuperClass(ref(classDef.ext().get()));
    }
    for (Ref i : classDef.jimplements()) {
      clazz.addImplements(program.createInterface(UNKNOWN, refName(i)));
    }
    for (Constructor i : constructors) {
      constructor(i, classDef, clazz);
    }
    for (MethodDef i : methods) {
      methodDef(i, clazz);
    }
    return clazz;
  }

  public JConstructor constructor(Constructor constructor, ClassDef classDef,
      JClassType enclosingClass) {
    Map<String, JLocal> varDict = new HashMap<String, JLocal>();
    Map<String, JParameter> paramDict = new HashMap<String, JParameter>();
    JConstructor jc = program.createConstructor(UNKNOWN, enclosingClass);
    for (ParamDef param : constructor.jparams()) {
      paramDef(param, paramDict, jc);
    }
    JBlock block = jc.getBody().getBlock();
    for (ConstructorStatement x : constructor.jbody()) {
      final JStatement js;
      if (x instanceof SuperConstructorCall) {
        js = superConstructorCall((SuperConstructorCall) x, classDef.ext().get(),
            varDict, paramDict, enclosingClass).makeStatement();
      } else if (x instanceof MethodStatement) {
        js = methodStatement((MethodStatement) x, varDict, paramDict, jc.getBody(), enclosingClass);
      } else {
        throw new RuntimeException("Unexpected case " + x);
      }
      block.addStmt(js);
    }

    return jc;
  }

  public JExpression expression(Expression expr, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    if (expr instanceof Literal) {
      return literal((Literal) expr);
    } else if (expr instanceof ThisRef$) {
      return program.getExprThisRef(UNKNOWN, enclosingType);
    } else if (expr instanceof MethodCall) {
      return methodCall((MethodCall) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof ThisRef$) {
      return thisRef(enclosingType);
    } else if (expr instanceof StaticMethodCall) {
      return staticMethodCall((StaticMethodCall) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof VarRef) {
      return varRef((VarRef) expr, varDict, paramDict);
    } else if (expr instanceof NewCall) {
      return newCall((NewCall) expr, varDict, paramDict, enclosingType);
    } else {
      throw new RuntimeException("to be implemented handling of " + expr);
    }
  }

  public JInterfaceType interfaceDef(InterfaceDef interfaceDef) {
    String name = refName(interfaceDef.name());
    JInterfaceType inter = program.createInterface(UNKNOWN, name);
    List<MethodDef> methods = interfaceDef.jbody();
    if (interfaceDef.ext().isDefined()) {
      inter.setSuperClass(ref(interfaceDef.ext().get()));
    }
    for (MethodDef i : methods) {
      interMethodDef(i, inter);
    }
    return inter;
  }

  public JMethod interMethodDef(MethodDef def, JInterfaceType enclosingInterface) {
    Map<String, JParameter> paramDict = new HashMap<String, JParameter>();
    JMethod m = program.createMethod(UNKNOWN, def.name(), enclosingInterface,
        type(def.returnType()), false, false, false, false, false);
    for (ParamDef param : def.jparams()) {
      paramDef(param, paramDict, m);
    }
    return m;
  }

  public JExpression literal(Literal literal) {
    if (literal instanceof StringLiteral) {
      return program.getLiteralString(UNKNOWN, ((StringLiteral) literal).v());
    } else {
      throw new RuntimeException("to be implemented");
    }
  }

  public JribMethodCall methodCall(MethodCall call, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JType returnType = type(call.signature().returnType());
    JExpression on = expression(call.on(), varDict, paramDict, enclosingType);
    JribMethodRef ref = new JribMethodRef(ref(call.signature().on()).getName(), jsniSignature(call.signature()));
    List<JExpression> params = params(call.signature().jparamTypes(), call.jparams(), varDict, paramDict, enclosingType);
    return new JribMethodCall(UNKNOWN, ref, on, params, returnType);
  }

  public JMethod methodDef(MethodDef def, JClassType enclosingClass) {
    Map<String, JLocal> varDict = new HashMap<String, JLocal>();
    Map<String, JParameter> paramDict = new HashMap<String, JParameter>();
    JMethod m = program.createMethod(UNKNOWN, def.name(), enclosingClass,
        type(def.returnType()), false, false, false, false, false);
    for (ParamDef param : def.jparams()) {
      paramDef(param, paramDict, m);
    }
    JMethodBody body = (JMethodBody) m.getBody();
    JBlock block = body.getBlock();
    for (MethodStatement x : def.jbody()) {
      JStatement js = methodStatement(x, varDict, paramDict, body, enclosingClass);
      block.addStmt(js);
    }
    return m;
  }

  public JStatement methodStatement(MethodStatement statement,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JMethodBody enclosingBody, JClassType enclosingClass) {
    if (statement instanceof VarDef) {
      return varDef((VarDef) statement, varDict, paramDict, enclosingBody, enclosingClass);
    } else if (statement instanceof Assignment) {
      return assignment((Assignment) statement, varDict, paramDict, enclosingBody, enclosingClass);
    } else if (statement instanceof Expression) {
      return expression((Expression) statement, varDict, paramDict, enclosingClass).makeStatement();
    } else {
      throw new RuntimeException("Unexpected case " + statement);
    }
  }

  public JribNewInstance newCall(NewCall call, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JClassType on = ref(call.signature().on());
    JType returnType = type(call.signature().returnType());
    JribConstructorRef ref = new JribConstructorRef(on.getName(), jsniSignature(call.signature()));

    List<JExpression> params = params(call.signature().jparamTypes(), call.jparams(), varDict, paramDict, enclosingType);

    return new JribNewInstance(UNKNOWN, ref, params, returnType);
  }

  @SuppressWarnings("static-access")
  public JParameter paramDef(ParamDef paramDef,
      Map<String, JParameter> paramDict, JMethod enclosingMethod) {
    JParameter param = program.createParameter(UNKNOWN, paramDef.name(),
        type(paramDef.typ()), false, false, enclosingMethod);
    paramDict.put(paramDef.name(), param);
    return param;
  }

  public JClassType ref(Ref ref) {
    // TODO(grek) abstract and final are hard-coded
    return new JClassType(UNKNOWN, refName(ref), false, false);
  }

  public String refName(Ref ref) {
    return ref.pkg().name().replace('/', '.') + "." + ref.name();
  }

  public JribMethodCall staticMethodCall(StaticMethodCall call, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JClassType on = ref(call.signature().on());
    JType returnType = type(call.signature().returnType());
    JribMethodRef ref = new JribMethodRef(on.getName(), jsniSignature(call.signature()));

    List<JExpression> params = params(call.signature().jparamTypes(), call.jparams(), varDict, paramDict, enclosingType);
    return new JribMethodCall(UNKNOWN, ref, null, params, returnType);
  }

  public JribMethodCall superConstructorCall(SuperConstructorCall call,
      Ref ext, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict,
      JClassType enclosingType) {
    JribMethodRef constructorRef = new JribConstructorRef(ref(ext).getName(),
        jsniSignature(call.signature()));
    List<JExpression> params = params(call.signature().jparamTypes(), call.jparams(), varDict,
        paramDict, enclosingType);
    return new JribMethodCall(UNKNOWN, constructorRef, thisRef(enclosingType), params,
        JPrimitiveType.VOID);
  }

  public JThisRef thisRef(JClassType enclosingType) {
    return program.getExprThisRef(UNKNOWN, enclosingType);
  }

  public JType type(Type type) {
    if (type instanceof Ref) {
      return ref((Ref) type);
    } else if (type instanceof Primitive) {
      Primitive primitive = (Primitive) type;
      throw new RuntimeException("To be implemeneted");
    } else if (type instanceof Array) {
      Array array = (Array) type;
      throw new RuntimeException("To be implemeneted");
    } else if (type instanceof com.google.jribble.ast.Void$) {
      return JPrimitiveType.VOID;
    } else {
      throw new RuntimeException("Missed case");
    }
  }

  @SuppressWarnings("static-access")
  public JDeclarationStatement varDef(VarDef def, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JMethodBody enclosingBody,
      JClassType enclosingClass) {
    JLocal local = program.createLocal(UNKNOWN, def.name(), type(def.typ()),
        false, enclosingBody);
    varDict.put(def.name(), local);
    JLocalRef ref = new JLocalRef(UNKNOWN, local);
    JExpression expr = expression(def.value(), varDict, paramDict, enclosingClass);
    return new JDeclarationStatement(UNKNOWN, ref, expr);
  }

  public JVariableRef varRef(VarRef ref, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict) {
    if (varDict.containsKey(ref.name())) {
      return new JLocalRef(UNKNOWN, varDict.get(ref.name()));
    } else if (paramDict.containsKey(ref.name())) {
      return new JParameterRef(UNKNOWN, paramDict.get(ref.name()));
    } else {
      throw new RuntimeException("Reference to unkown variable '" + ref.name() + "'");
    }
  }

  private JLocal findLocal(List<JLocal> locals, String name) {
    JLocal result = null;
    for (JLocal l : locals) {
      if (l.getName().equals(name)) {
        result = l;
        break;
      }
    }
    if (result != null) {
      return result;
    } else {
      throw new RuntimeException("Local variable '" + name + "' cound not" +
          " be found.");
    }        
  }

  private String jsniSignature(Signature s) {
    StringBuilder b = new StringBuilder();
    // TODO(grek): establish convention for super calls and get rid of this special case
    if (s.name().equals("super")) {
      b.append(s.on().name());
    } else {
      b.append(s.name());
    }
    b.append("(");
    for (Type t : s.jparamTypes()) {
      b.append(type(t).getJsniSignatureName());
    }
    b.append(")");
    b.append(type(s.returnType()).getJsniSignatureName());
    return b.toString();
  }

  private List<JExpression> params(List<Type> paramTypes, List<Expression> params,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JClassType enclosingType) {
    assert paramTypes.size() == params.size();
    List<JExpression> result = new LinkedList<JExpression>();
    for (int i = 0; i < params.size(); i++) {
      JExpression expr = expression(params.get(i), varDict, paramDict, enclosingType);
      result.add(expr);
    }
    return result;
  }

}
