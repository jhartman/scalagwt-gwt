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

import com.google.gwt.dev.jjs.ast.JArrayRef;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JCastOperation;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JConditional;
import com.google.gwt.dev.jjs.ast.JConstructor;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JExpressionStatement;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JIfStatement;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JNewArray;
import com.google.gwt.dev.jjs.ast.JArrayType;
import com.google.gwt.dev.jjs.ast.JNewInstance;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReturnStatement;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JThrowStatement;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVariableRef;
import com.google.gwt.dev.jjs.impl.BuildTypeMap;
import com.google.gwt.dev.jjs.impl.TypeMap;
import com.google.jribble.ast.ArrayLength;
import com.google.jribble.ast.ArrayRef;
import com.google.jribble.ast.Assignment;
import com.google.jribble.ast.BinaryOp;
import com.google.jribble.ast.Block;
import com.google.jribble.ast.BooleanLiteral;
import com.google.jribble.ast.Cast;
import com.google.jribble.ast.ClassDef;
import com.google.jribble.ast.Conditional;
import com.google.jribble.ast.Constructor;
import com.google.jribble.ast.ConstructorCall;
import com.google.jribble.ast.Equal;
import com.google.jribble.ast.Expression;
import com.google.jribble.ast.FieldRef;
import com.google.jribble.ast.If;
import com.google.jribble.ast.Literal;
import com.google.jribble.ast.MethodCall;
import com.google.jribble.ast.MethodDef;
import com.google.jribble.ast.NewArray;
import com.google.jribble.ast.NewCall;
import com.google.jribble.ast.Return;
import com.google.jribble.ast.Signature;
import com.google.jribble.ast.Statement;
import com.google.jribble.ast.StaticFieldRef;
import com.google.jribble.ast.StaticMethodCall;
import com.google.jribble.ast.StringLiteral;
import com.google.jribble.ast.ThisRef$;
import com.google.jribble.ast.Throw;
import com.google.jribble.ast.Type;
import com.google.jribble.ast.VarDef;
import com.google.jribble.ast.VarRef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import scala.Option;

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
  
  private final JProgram program;
  private final TypeMap typeMap;
  
  public JribbleMethodBodies(TypeMap typeMap) {
    this.typeMap = typeMap;
    this.program = typeMap.getProgram();
  }

  public JExpressionStatement assignment(Assignment assignment,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JMethodBody enclosingBody, JClassType enclosingClass) {
    JExpression lhs = expression(assignment.lhs(), varDict, paramDict, enclosingClass);
    JExpression rhs = expression(assignment.rhs(), varDict, paramDict, enclosingClass);
    return JProgram.createAssignmentStmt(UNKNOWN, lhs, rhs);
  }
  
  public void block(Block block, JBlock jblock, Map<String, JLocal> varDict, 
      Map<String, JParameter> paramDict, JMethodBody enclosingBody, JClassType enclosingClass) {
    for (Statement x : block.jstatements()) {
      final JStatement js;
      if (x instanceof ConstructorCall) {
        js = constructorCall((ConstructorCall)x, varDict, paramDict, 
            enclosingClass).makeStatement();
      } else {
        js = methodStatement(x, varDict, paramDict, enclosingBody, enclosingClass);
      }
      jblock.addStmt(js);
    }
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
  
  public JConditional conditional(Conditional conditional, Map<String, JLocal> varDict, 
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JExpression condition = expression(conditional.condition(), varDict, paramDict, enclosingType);
    JExpression then = expression(conditional.then(), varDict, paramDict, enclosingType);
    JExpression elsee = expression(conditional.elsee(), varDict, paramDict, enclosingType);
    return new JConditional(UNKNOWN, type(conditional.typ()), condition, then, elsee);
  }
  
  public void constructor(Constructor constructor, ClassDef classDef, 
      JClassType enclosingClass) {
    Map<String, JLocal> varDict = new HashMap<String, JLocal>();
    Map<String, JParameter> paramDict = new HashMap<String, JParameter>();
    JMethod jc = findMethod(enclosingClass, constructor.signature(classDef.name()));
    for (JParameter x : jc.getParams()) {
      paramDict.put(x.getName(), x);
    }
    JMethodBody body = (JMethodBody) jc.getBody();
    JBlock jblock = body.getBlock();
    block(constructor.body(), jblock, varDict, paramDict, body, enclosingClass);
  }
  
  public JExpression expression(Expression expr, Map<String, JLocal> varDict, 
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    if (expr instanceof Literal) {
      return literal((Literal) expr);
    } else if (expr instanceof VarRef) {
      return varRef((VarRef) expr, varDict, paramDict);
    } else if (expr instanceof ThisRef$) {
      return thisRef(enclosingType);
    } else if (expr instanceof MethodCall) {
      return methodCall((MethodCall) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof StaticMethodCall) {
      return staticMethodCall((StaticMethodCall) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof VarRef) {
      return varRef((VarRef) expr, varDict, paramDict);
    } else if (expr instanceof NewCall) {
      return newCall((NewCall) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof Conditional) {
      return conditional((Conditional) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof Cast) {
      return cast((Cast) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof BinaryOp) {
      return binaryOp((Equal) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof FieldRef) {
      return fieldRef((FieldRef) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof StaticFieldRef) {
      return staticFieldRef((StaticFieldRef) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof ArrayRef) {
      return arrayRef((ArrayRef) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof NewArray) {
      return newArray((NewArray) expr, varDict, paramDict, enclosingType);
    } else if (expr instanceof ArrayLength) {
      return arrayLength((ArrayLength) expr, varDict, paramDict, enclosingType);
    } else {
      throw new RuntimeException("to be implemented handling of " + expr);
    }
  }
  
  private JFieldRef arrayLength(ArrayLength expr,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JClassType enclosingType) {
    JExpression on = expression(expr.on(), varDict, paramDict, enclosingType);
    return new JFieldRef(UNKNOWN, on, program.getIndexedField("Array.length"), enclosingType);
  }
  
  private JNewArray newArray(NewArray expr,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JClassType enclosingType) {
    JArrayType typ = program.getTypeArray(type(expr.typ()), expr.jdims().size());
    List<JExpression> dims = new LinkedList<JExpression>();
    for (Option<Expression> i : expr.jdims()) {
      if (i.isDefined()) {
        dims.add(expression(i.get(), varDict, paramDict, enclosingType));
      } else {
        dims.add(program.getLiteralAbsentArrayDimension());
      }
    }
    return JNewArray.createDims(program, UNKNOWN, typ, dims);
  }
  
  private JArrayRef arrayRef(ArrayRef expr,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JClassType enclosingType) {
    return new JArrayRef(UNKNOWN, expression(expr.on(), varDict, paramDict, enclosingType), 
        expression(expr.index(), varDict, paramDict, enclosingType));
  }
  
  private JFieldRef staticFieldRef(StaticFieldRef expr,
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict,
      JClassType enclosingType) {
    if (expr.name().equals("MODULE$")) {
      JField field = typeMap.getField(expr.on().javaName(), expr.name());
      return new JFieldRef(UNKNOWN, null, field, enclosingType);
    } else {
      throw new RuntimeException("to be implemented handling of " + expr);
    }
  }

  private JFieldRef fieldRef(FieldRef expr, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    if (expr.on().equals(ThisRef$.MODULE$)) {
      List<JField> fields = enclosingType.getFields();
      JField field = findField(fields, expr.name());
      JExpression on = expression(expr.on(), varDict, paramDict, enclosingType);
      return new JFieldRef(UNKNOWN, on, field, enclosingType);
    } else if (expr.on() instanceof StaticFieldRef) {
      StaticFieldRef staticRef = (StaticFieldRef) expr.on();
      if (staticRef.name().equals("MODULE$") && 
          staticRef.on().javaName().equals("scala.runtime.BoxedUnit") && expr.name().equals("UNIT")) {
        JField field = typeMap.getField("scala.runtime.BoxedUnit", "UNIT");
        return new JFieldRef(UNKNOWN, null, field, enclosingType);
      } else {
        throw new RuntimeException("to be implemented handling of " + expr);
      }
    } else {
      throw new RuntimeException("to be implemented handling of " + expr);
    }
  }

  private JBinaryOperation binaryOp(BinaryOp op, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JExpression lhs = expression(op.lhs(), varDict, paramDict, enclosingType);
    JExpression rhs = expression(op.lhs(), varDict, paramDict, enclosingType);
    JBinaryOperator jop;
    JType type;
    if (op.symbol() == "==") {
      jop = JBinaryOperator.EQ;
      type = JPrimitiveType.BOOLEAN;
    } else {
      throw new RuntimeException("Uknown symbol " + op.symbol());
    }
    return new JBinaryOperation(UNKNOWN, type, jop, lhs, rhs);
  }

  private JCastOperation cast(Cast cast, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JExpression on = expression(cast.on(), varDict, paramDict, enclosingType); 
    return new JCastOperation(UNKNOWN, type(cast.typ()), on);
  }

  public JIfStatement ifStmt(If statement, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JMethodBody enclosingBody, JClassType enclosingClass) {
    JExpression condition = expression(statement.condition(), varDict, paramDict, enclosingClass);
    
    final JBlock then = new JBlock(UNKNOWN); 
    block(statement.then(), then, varDict, paramDict, enclosingBody, enclosingClass);
    JBlock elsee = null;
    if (statement.elsee().isDefined()) {
      elsee = new JBlock(UNKNOWN);
      block(statement.elsee().get(), elsee, varDict, paramDict, enclosingBody, enclosingClass);
    }
    return new JIfStatement(UNKNOWN, condition, then, elsee);
  }
  
  public JExpression literal(Literal literal) {
    if (literal instanceof StringLiteral) {
      return program.getLiteralString(UNKNOWN, ((StringLiteral) literal).v());
    } else if (literal instanceof BooleanLiteral) {
      return program.getLiteralBoolean(((BooleanLiteral) literal).v());
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
    for (JParameter x : m.getParams()) {
      paramDict.put(x.getName(), x);
    }
    JMethodBody body = (JMethodBody) m.getBody();
    JBlock block = body.getBlock();
    if (def.body().isDefined()) {
      for (Statement x : def.body().get().jstatements()) {
        JStatement js = methodStatement(x, varDict, paramDict, body, enclosingClass);
        block.addStmt(js);
      }
    }
  }
  
  public JStatement methodStatement(Statement statement, 
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict, 
      JMethodBody enclosingBody, JClassType enclosingClass) {
    if (statement instanceof VarDef) {
      return varDef((VarDef) statement, varDict, paramDict, enclosingBody, enclosingClass);
    } else if (statement instanceof Assignment) {
      return assignment((Assignment) statement, varDict, paramDict, enclosingBody, enclosingClass);
    } else if (statement instanceof Expression) {
      return expression((Expression) statement, varDict, paramDict, enclosingClass).makeStatement();
    } else if (statement instanceof If) {
      return ifStmt((If) statement, varDict, paramDict, enclosingBody, enclosingClass);
    } else if (statement instanceof Return) {
      return returnStmt((Return) statement, varDict, paramDict, enclosingBody, enclosingClass);
    } else if (statement instanceof Throw) {
      return throwStmt((Throw) statement, varDict, paramDict, enclosingBody, enclosingClass);
    } else throw new RuntimeException("Unexpected case " + statement);
  }
  
  private JThrowStatement throwStmt(Throw statement, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JMethodBody enclosingBody,
      JClassType enclosingClass) {
    JExpression expression = expression(statement.expression(), varDict, paramDict, enclosingClass);
    return new JThrowStatement(UNKNOWN, expression);
  }

  private JReturnStatement returnStmt(Return statement, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JMethodBody enclosingBody,
      JClassType enclosingClass) {
    JExpression expression = null;
    if (statement.expression().isDefined()) {
      expression = expression(statement.expression().get(), varDict, paramDict, enclosingClass);
    }
    return new JReturnStatement(UNKNOWN, expression);
  }

  public JNewInstance newCall(NewCall call, Map<String, JLocal> varDict,
      Map<String, JParameter> paramDict, JClassType enclosingType) {
    JMethodCall methodCall = constructorCall(call.constructor(), varDict, paramDict, enclosingType); 
    JConstructor constructor = (JConstructor) methodCall.getTarget();

    JNewInstance jnew = new JNewInstance(UNKNOWN, constructor, enclosingType);
    jnew.addArgs(methodCall.getArgs());
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
  
  public JMethodCall constructorCall(ConstructorCall call, 
      Map<String, JLocal> varDict, Map<String, JParameter> paramDict, 
      JClassType enclosingType) {
    Signature signature = call.signature();
    JMethod method = typeMap.getMethod(signature.on().javaName(), jsniSignature(signature));
    List<JExpression> params = params(signature.jparamTypes(), call.jparams(), varDict, 
        paramDict, enclosingType);
    JMethodCall jcall = new JMethodCall(UNKNOWN, thisRef(enclosingType), method);
    // not sure why this is needed; inspired by JavaASTGenerationVisitor.processConstructor
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
    JExpression expr = null;
    if (def.value().isDefined()) {
      expr = expression(def.value().get(), varDict, paramDict, enclosingClass);
    }
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
  
  private JField findField(List<JField> fields, String name) {
    JField result = null;
    for (JField l : fields) {
      if (l.getName().equals(name)) {
        result = l;
        break;
      }
    }
    return result;        
  }

  private JLocal findLocal(List<JLocal> locals, String name) {
    JLocal result = null;
    for (JLocal l : locals) {
      if (l.getName().equals(name)) {
        result = l;
        break;
      }
    }
    return result;        
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
    } else if (s.name().equals("this")) {
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
