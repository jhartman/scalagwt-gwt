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

import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JConstructor;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JNewInstance;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVariableRef;
import com.google.gwt.dev.jjs.impl.BuildTypeMap;
import com.google.gwt.dev.jjs.impl.TypeMap;
import com.google.jribble.ast.Assignment;
import com.google.jribble.ast.ClassDef;
import com.google.jribble.ast.Constructor;
import com.google.jribble.ast.ConstructorStatement;
import com.google.jribble.ast.Expression;
import com.google.jribble.ast.Literal;
import com.google.jribble.ast.MethodCall;
import com.google.jribble.ast.MethodDef;
import com.google.jribble.ast.MethodStatement;
import com.google.jribble.ast.NewCall;
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
 * More precisely, for any given ClassDef it will look up it's corresponding
 * GWT AST node in TypeMap and will fill in constructor/method bodies.
 * 
 * Constructor/method nodes are being looked up in TypeMap as well and only
 * their bodies are being constructed out of jribble AST.
 * 
 * This class relies on the fact that {@link BuildTypeMap} adds incomplete
 * GWT nodes for each {@link JribbleUnit}.
 *
 */
public class JribbleMethodBodies {
  
  private final TypeMap typeMap;
  private final JProgram program;
  
  public JribbleMethodBodies(TypeMap typeMap) {
    this.typeMap = typeMap;
    this.program = typeMap.getProgram();
  }

  public JExpressionStatement assignment(Assignment assignment,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JMethodBody enclosingBody, JClassType enclosingClass) {
    JLocal local = findLocal(enclosingBody.getLocals(), assignment.name());
    JLocalRef ref = new JLocalRef(UNKNOWN, local);
    JExpression expr = expression(assignment.value(), varDict, paramDict, enclosingClass);
    return JProgram.createAssignmentStmt(UNKNOWN, ref, expr);
  }
  
  public void classDef(ClassDef def) {
    JClassType clazz = (JClassType) typeMap.get(def.name());
    List<Constructor> constructors = def.jconstructors();
    List<MethodDef> methods = def.jmethodDefs();
    for (Constructor i : constructors) {
      constructor(i, def, clazz);
    }
    for (MethodDef i : methods) {
      methodDef(i, clazz, def);
    }
  }
  
  public void constructor(Constructor constructor, ClassDef classDef, 
      JClassType enclosingClass) {
    Map<String, JLocal> varDict = new HashMap<String, JLocal>();
    Map<String, JParameter> paramDict = new HashMap<String, JParameter>();
    JMethod jc = findMethod(enclosingClass, constructor.signature(classDef.name()));
    JMethodBody body = (JMethodBody) jc.getBody();
    JBlock block = body.getBlock();
    for (ConstructorStatement x : constructor.body().jstatements()) {
      final JStatement js;
      if (x instanceof SuperConstructorCall)
        js = superConstructorCall((SuperConstructorCall)x, varDict, paramDict, 
            enclosingClass).makeStatement();
      else if (x instanceof MethodStatement)
        js = methodStatement((MethodStatement)x, varDict, paramDict, body, enclosingClass);
      else throw new RuntimeException("Unexpected case " + x);
      block.addStmt(js);
    }
  }
  
  public JExpression expression(Expression expr, Map<String, JLocal> varDict, 
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    if (expr instanceof Literal)
      return literal((Literal)expr);
    else if (expr instanceof ThisRef$) {
      return thisRef(enclosingType);
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
  
  public JExpression literal(Literal literal) {
    if (literal instanceof StringLiteral) {
      return program.getLiteralString(UNKNOWN, ((StringLiteral) literal).v());
    } else {
      throw new RuntimeException("to be implemented");
    }
  }
  
  public JMethodCall methodCall(MethodCall call, Map<String, JLocal> varDict, 
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JExpression on = expression(call.on(), varDict, paramDict, enclosingType);
    
    JMethod method = typeMap.getMethod(call.signature().on().javaName(), 
        jsniSignature(call.signature()));
    
    List<JExpression> params = params(call.signature().jparamTypes(),
        call.jparams(), varDict, paramDict, enclosingType);
    
    JMethodCall jcall = new JMethodCall(UNKNOWN, on, method);
    jcall.addArgs(params);
    return jcall;
  }
  
  public void methodDef(MethodDef def, JClassType enclosingClass, ClassDef classDef) {
    Map<String, JLocal> varDict = new HashMap<String, JLocal>();
    Map<String, JParameter> paramDict = new HashMap<String, JParameter>();
    JMethod m = findMethod(enclosingClass, def.signature(classDef.name()));
    JMethodBody body = (JMethodBody) m.getBody();
    JBlock block = body.getBlock();
    for (MethodStatement x : def.body().jstatements()) {
      JStatement js = methodStatement(x, varDict, paramDict, body, enclosingClass);
      block.addStmt(js);
    }
  }

  public JStatement methodStatement(MethodStatement statement, 
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict, 
      JMethodBody enclosingBody, JClassType enclosingClass) {
    if (statement instanceof VarDef)
      return varDef((VarDef)statement, varDict, paramDict, enclosingBody, enclosingClass);
    else if (statement instanceof Assignment)
      return assignment((Assignment)statement, varDict, paramDict, enclosingBody, enclosingClass);
    else if (statement instanceof Expression) {
      return expression((Expression) statement, varDict, paramDict, enclosingClass).makeStatement();
    } else throw new RuntimeException("Unexpected case " + statement);
  }
  
  public JNewInstance newCall(NewCall call, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JConstructor constructor = (JConstructor) typeMap.getMethod(call.signature().on().javaName(), 
        jsniSignature(call.signature()));
    
    List<JExpression> params = params(call.signature().jparamTypes(), call.jparams(), varDict, 
        paramDict, enclosingType);

    JNewInstance jnew = new JNewInstance(UNKNOWN, constructor, enclosingType);
    jnew.addArgs(params);
    return jnew;
  }
  
  public JMethodCall staticMethodCall(StaticMethodCall call, Map<String, JLocal> varDict, 
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JMethod method = typeMap.getMethod(call.signature().on().javaName(), 
        jsniSignature(call.signature()));
    
    List<JExpression> params = params(call.signature().jparamTypes(),
        call.jparams(), varDict, paramDict, enclosingType);
    
    JMethodCall jcall = new JMethodCall(UNKNOWN, null, method);
    jcall.addArgs(params);
    return jcall;
  }
  
  public JMethodCall superConstructorCall(SuperConstructorCall call, 
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict, 
      JClassType enclosingType) {
    Signature signature = call.signature();
    JMethod method = typeMap.getMethod(signature.on().javaName(), jsniSignature(signature));
    List<JExpression> params = params(signature.jparamTypes(), call.jparams(), varDict, 
        paramDict, enclosingType);
    JMethodCall jcall = new JMethodCall(UNKNOWN, thisRef(enclosingType), method);
    //not sure why this is needed; inspired by JavaASTGenerationVisitor.processConstructor
    jcall.setStaticDispatchOnly();
    jcall.addArgs(params);
    return jcall;
  }
  
  public JThisRef thisRef(JClassType enclosingType) {
    return program.getExprThisRef(UNKNOWN, enclosingType);
  }

  public JType type(Type type) {
    return typeMap.get(type);
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

  private JMethod findMethod(JDeclaredType type, Signature signature) {
    JMethod result = null;
    for (JMethod x : type.getMethods()) {
      if (x.getJsniSignature().equals(jsniSignature(signature))) {
        result = x;
        break;
      }
    }
    assert result != null;
    return result;
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
