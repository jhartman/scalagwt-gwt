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

import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.ast.JArrayRef;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JCastOperation;
import com.google.gwt.dev.jjs.ast.JClassLiteral;
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
import com.google.gwt.dev.jjs.ast.JInstanceOf;
import com.google.gwt.dev.jjs.ast.JLabel;
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
import com.google.gwt.dev.jjs.ast.JPrefixOperation;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JReturnStatement;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JThrowStatement;
import com.google.gwt.dev.jjs.ast.JTryStatement;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JUnaryOperator;
import com.google.gwt.dev.jjs.ast.JVariableRef;
import com.google.gwt.dev.jjs.ast.JWhileStatement;
import com.google.gwt.dev.jjs.impl.BuildTypeMap;
import com.google.gwt.dev.jjs.impl.TypeMap;
import com.google.jribble.ast.And;
import com.google.jribble.ast.Array;
import com.google.jribble.ast.ArrayInitializer;
import com.google.jribble.ast.ArrayLength;
import com.google.jribble.ast.ArrayRef;
import com.google.jribble.ast.Assignment;
import com.google.jribble.ast.BinaryOp;
import com.google.jribble.ast.BitAnd;
import com.google.jribble.ast.BitLShift;
import com.google.jribble.ast.BitNot;
import com.google.jribble.ast.BitOr;
import com.google.jribble.ast.BitRShift;
import com.google.jribble.ast.BitXor;
import com.google.jribble.ast.Block;
import com.google.jribble.ast.BooleanLiteral;
import com.google.jribble.ast.Cast;
import com.google.jribble.ast.CharLiteral;
import com.google.jribble.ast.ClassDef;
import com.google.jribble.ast.ClassOf;
import com.google.jribble.ast.Conditional;
import com.google.jribble.ast.Constructor;
import com.google.jribble.ast.ConstructorCall;
import com.google.jribble.ast.Divide;
import com.google.jribble.ast.DoubleLiteral;
import com.google.jribble.ast.Equal;
import com.google.jribble.ast.Expression;
import com.google.jribble.ast.FieldRef;
import com.google.jribble.ast.FloatLiteral;
import com.google.jribble.ast.Greater;
import com.google.jribble.ast.GreaterOrEqual;
import com.google.jribble.ast.If;
import com.google.jribble.ast.InstanceOf;
import com.google.jribble.ast.IntLiteral;
import com.google.jribble.ast.Lesser;
import com.google.jribble.ast.LesserOrEqual;
import com.google.jribble.ast.Literal;
import com.google.jribble.ast.LongLiteral;
import com.google.jribble.ast.MethodCall;
import com.google.jribble.ast.MethodDef;
import com.google.jribble.ast.Minus;
import com.google.jribble.ast.Modulus;
import com.google.jribble.ast.Multiply;
import com.google.jribble.ast.NewArray;
import com.google.jribble.ast.NewCall;
import com.google.jribble.ast.Not;
import com.google.jribble.ast.NullLiteral$;
import com.google.jribble.ast.NotEqual;
import com.google.jribble.ast.Or;
import com.google.jribble.ast.Plus;
import com.google.jribble.ast.Ref;
import com.google.jribble.ast.Return;
import com.google.jribble.ast.Signature;
import com.google.jribble.ast.Statement;
import com.google.jribble.ast.StaticFieldRef;
import com.google.jribble.ast.StaticMethodCall;
import com.google.jribble.ast.StringLiteral;
import com.google.jribble.ast.ThisRef$;
import com.google.jribble.ast.Throw;
import com.google.jribble.ast.Try;
import com.google.jribble.ast.Type;
import com.google.jribble.ast.UnaryMinus;
import com.google.jribble.ast.UnaryOp;
import com.google.jribble.ast.VarDef;
import com.google.jribble.ast.VarRef;
import com.google.jribble.ast.While;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import scala.Option;
import scala.Tuple3;

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
  
  //TODO(grek): This class should be implemented as immutable data structure
  //but I fail to see how to achieve that without lots of code and not cloning
  //underlying collections all the time.
  private final class LocalStack {
    private final Stack<Map<String, JLocal>> varStack = new Stack<Map<String,JLocal>>();
    private final Map<String, JParameter> params;
    private final Map<String, JLabel> labels = new HashMap<String, JLabel>();
    private final JClassType enclosingType;
    private final JMethodBody enclosingBody;
    
    public LocalStack(JClassType enclosingType, JMethodBody enclosingBody,
        Map<String, JParameter> params) {
      this.enclosingType = enclosingType;
      this.enclosingBody = enclosingBody;
      this.params = params;
    }
    
    public void addVar(String name, JLocal x) {
      Map<String, JLocal> peak = varStack.peek();
      assert !peak.containsKey(name);
      peak.put(name, x);
    }
    
    public void pushLabel(JLabel x) {
      assert !labels.containsKey(x.getName());
      labels.put(x.getName(), x);
    }
    
    public void popLabel(String name) {
      assert labels.containsKey(name);
      labels.remove(name);
    }
    
    public JLabel getLabel(String name) {
      if (!labels.containsKey(name)) {
        throw new InternalCompilerException(String.format("Failed to find %1s", name));
      }
      return labels.get(name);
    }
    
    public void pushBlock() {
      varStack.push(new HashMap<String, JLocal>());
    }
    
    public void popBlock() {
      varStack.pop();
    }
    
    public JVariableRef resolveLocal(String name) {
      for (int i = varStack.size()-1; i >= 0; i--) {
        JLocal local = varStack.get(i).get(name);
        if (local != null) {
          return new JLocalRef(UNKNOWN, local);
        }
      }
      JParameter param = params.get(name);
      if (param != null) {
        return new JParameterRef(UNKNOWN, param);
      }
      throw new InternalCompilerException(String.format("Failed to find %1s", name));
    }
    
    public JClassType getEnclosingType() {
      return enclosingType;
    }
    
    public JMethodBody getEnclosingBody() {
      return enclosingBody;
    }
    
  }

  public JExpressionStatement assignment(Assignment assignment, LocalStack local) {
    JExpression lhs = expression(assignment.lhs(), local);
    JExpression rhs = expression(assignment.rhs(), local);
    return JProgram.createAssignmentStmt(UNKNOWN, lhs, rhs);
  }
  
  public void block(Block block, JBlock jblock, LocalStack local) {
    local.pushBlock();
    for (Statement x : block.jstatements()) {
      final JStatement js;
      if (x instanceof ConstructorCall) {
        js = constructorCall((ConstructorCall)x, local).makeStatement();
      } else {
        js = methodStatement(x, local);
      }
      jblock.addStmt(js);
    }
    local.popBlock();
  }
  
  public void classDef(ClassDef def) {
    JClassType clazz = (JClassType) typeMap.get(def.name());
    List<Constructor> constructors = def.jconstructors();
    List<MethodDef> methods = def.jmethodDefs();
    try {
      for (Constructor i : constructors) {
        constructor(i, def, clazz);
      }
      for (MethodDef i : methods) {
        methodDef(i, clazz, def);
      }
    } catch (InternalCompilerException e) {
      e.addNode(clazz);
      throw e;
    }
  }
  
  public JConditional conditional(Conditional conditional, LocalStack local) {
    JExpression condition = expression(conditional.condition(), local);
    JExpression then = expression(conditional.then(), local);
    JExpression elsee = expression(conditional.elsee(), local);
    return new JConditional(UNKNOWN, type(conditional.typ()), condition, then, elsee);
  }
  
  public void constructor(Constructor constructor, ClassDef classDef,
      JClassType enclosingClass) {
    JMethod jc = findMethod(enclosingClass, constructor.signature(classDef.name()));
    Map<String, JParameter> params = new HashMap<String, JParameter>();
    for (JParameter x : jc.getParams()) {
      params.put(x.getName(), x);
    }
    JMethodBody body = (JMethodBody) jc.getBody();
    LocalStack local = new LocalStack(enclosingClass, body, params);
    JBlock jblock = body.getBlock();
    block(constructor.body(), jblock, local);
  }
  
  public JExpression expression(Expression expr, LocalStack local) {
    if (expr instanceof Literal) {
      return literal((Literal) expr);
    } else if (expr instanceof VarRef) {
      return varRef((VarRef) expr, local);
    } else if (expr instanceof ThisRef$) {
      return thisRef(local.getEnclosingType());
    } else if (expr instanceof MethodCall) {
      return methodCall((MethodCall) expr, local);
    } else if (expr instanceof StaticMethodCall) {
      return staticMethodCall((StaticMethodCall) expr, local);
    } else if (expr instanceof VarRef) {
      return varRef((VarRef) expr, local);
    } else if (expr instanceof NewCall) {
      return newCall((NewCall) expr, local);
    } else if (expr instanceof Conditional) {
      return conditional((Conditional) expr, local);
    } else if (expr instanceof Cast) {
      return cast((Cast) expr, local);
    } else if (expr instanceof BinaryOp) {
      return binaryOp((BinaryOp) expr, local);
    } else if (expr instanceof FieldRef) {
      return fieldRef((FieldRef) expr, local);
    } else if (expr instanceof StaticFieldRef) {
      return staticFieldRef((StaticFieldRef) expr, local);
    } else if (expr instanceof ArrayRef) {
      return arrayRef((ArrayRef) expr, local);
    } else if (expr instanceof NewArray) {
      return newArray((NewArray) expr, local);
    } else if (expr instanceof ArrayLength) {
      return arrayLength((ArrayLength) expr, local);
    } else if (expr instanceof InstanceOf) {
      return instanceOf((InstanceOf) expr, local);
    } else if (expr instanceof ClassOf) {
      return classOf((ClassOf) expr, local);
    } else if (expr instanceof ArrayInitializer) {
      return arrayInitializer((ArrayInitializer) expr, local);
    } else if (expr instanceof UnaryOp) {
      return unaryOp((UnaryOp) expr, local);
    } else {
      throw new RuntimeException("to be implemented handling of " + expr);
    }
  }
  
  private JExpression unaryOp(UnaryOp expr, LocalStack local) {
    JUnaryOperator op;
    if (expr instanceof UnaryMinus) {
      op = JUnaryOperator.NEG;
    } else if (expr instanceof Not) {
      op = JUnaryOperator.NOT;
    } else if (expr instanceof BitNot) {
      op = JUnaryOperator.BIT_NOT;
    } else {
      throw new InternalCompilerException("Unsupported AST node " + expr);
    }
    return new JPrefixOperation(UNKNOWN, op, expression(expr.expression(), local));
  }
  
  private JNewArray arrayInitializer(ArrayInitializer expr, LocalStack local) {
    Type type = expr.typ();
    int dims = 1;
    while (type instanceof Array) {
      type = ((Array) type).typ();
      dims++;
    }
    JArrayType arrayType = program.getTypeArray(type(type), dims);
    List<JExpression> initializers = new LinkedList<JExpression>();
    for (Expression e: expr.jelements()) {
      initializers.add(expression(e, local));
    }
    return JNewArray.createInitializers(program, UNKNOWN, arrayType, initializers);
  }
  
  private JClassLiteral classOf(ClassOf expr, LocalStack local) {
    return program.getLiteralClass(type(expr.ref()));
  }
  
  private JInstanceOf instanceOf(InstanceOf expr, LocalStack local) {
    JExpression on = expression(expr.on(), local);
    return new JInstanceOf(UNKNOWN, (JReferenceType)typeMap.get(expr.typ()), on);
  }
  
  private JFieldRef arrayLength(ArrayLength expr, LocalStack local) {
    JExpression on = expression(expr.on(), local);
    return new JFieldRef(UNKNOWN, on, program.getIndexedField("Array.length"), local.getEnclosingType());
  }
  
  private JNewArray newArray(NewArray expr, LocalStack local) {
    JArrayType typ = program.getTypeArray(type(expr.typ()), expr.jdims().size());
    List<JExpression> dims = new LinkedList<JExpression>();
    for (Option<Expression> i : expr.jdims()) {
      if (i.isDefined()) {
        dims.add(expression(i.get(), local));
      } else {
        dims.add(program.getLiteralAbsentArrayDimension());
      }
    }
    return JNewArray.createDims(program, UNKNOWN, typ, dims);
  }
  
  private JArrayRef arrayRef(ArrayRef expr, LocalStack local) {
    return new JArrayRef(UNKNOWN, expression(expr.on(), local), 
        expression(expr.index(), local));
  }
  
  private JFieldRef staticFieldRef(StaticFieldRef expr, LocalStack local) {
    JField field = typeMap.getField(expr.on().javaName(), expr.name());
    if (field == null) {
      throw new RuntimeException(String.format("Failed to obtain field %1s", expr));
    }
    return new JFieldRef(UNKNOWN, null, field, local.getEnclosingType());
  }

  private JFieldRef fieldRef(FieldRef expr, LocalStack local) {
    try {
      JExpression on = expression(expr.on(), local);
      //TODO FieldRef.onType should be of type Ref and not Type
      JClassType typ = (JClassType) typeMap.get(expr.onType());
      JField field = findField(typ.getFields(), expr.name());
      if (field == null) {
        throw new RuntimeException();
      }
      return new JFieldRef(UNKNOWN, on, field, local.getEnclosingType());
    } catch (Exception e) {
      throw new InternalCompilerException("Failed to obtain field " + expr);
    }
  }

  private JBinaryOperation binaryOp(BinaryOp op, LocalStack local) {
    JExpression lhs = expression(op.lhs(), local);
    JExpression rhs = expression(op.rhs(), local);
    JBinaryOperator jop;
    JType type;
    //TODO(grek): Most of types below are wrong. It looks like we'll need
    //to store type information for operators too. :-(
    if (op instanceof Equal) {
      jop = JBinaryOperator.EQ;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof Multiply) {
      jop = JBinaryOperator.MUL;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof Divide) {
      jop = JBinaryOperator.DIV;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof Modulus) {
      jop = JBinaryOperator.MOD;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof Minus) {
      jop = JBinaryOperator.SUB;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof Plus) {
      if (program.isJavaLangString(lhs.getType()) || program.isJavaLangString(rhs.getType())) {
        jop = JBinaryOperator.CONCAT;
        type = program.getTypeJavaLangString();
      } else {
        jop = JBinaryOperator.ADD;
        type = program.getTypePrimitiveInt();
      }
    } else if (op instanceof Greater) {
      jop = JBinaryOperator.GT;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof GreaterOrEqual) {
      jop = JBinaryOperator.GTE;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof Lesser) {
      jop = JBinaryOperator.LT;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof LesserOrEqual) {
      jop = JBinaryOperator.LTE;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof NotEqual) {
      jop = JBinaryOperator.NEQ;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof And) {
      jop = JBinaryOperator.AND;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof Or) {
      jop = JBinaryOperator.OR;
      type = program.getTypePrimitiveBoolean();
    } else if (op instanceof BitLShift) {
      jop = JBinaryOperator.SHL;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof BitRShift) {
      jop = JBinaryOperator.SHR;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof BitAnd) {
      jop = JBinaryOperator.BIT_AND;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof BitOr) {
      jop = JBinaryOperator.BIT_OR;
      type = program.getTypePrimitiveInt();
    } else if (op instanceof BitXor) {
      jop = JBinaryOperator.BIT_XOR;
      type = program.getTypePrimitiveInt();
    } else {
      throw new RuntimeException("Uknown symbol " + op.symbol());
    }
    return new JBinaryOperation(UNKNOWN, type, jop, lhs, rhs);
  }

  private JCastOperation cast(Cast cast, LocalStack local) {
    JExpression on = expression(cast.on(), local); 
    return new JCastOperation(UNKNOWN, type(cast.typ()), on);
  }

  public JIfStatement ifStmt(If statement, LocalStack local) {
    JExpression condition = expression(statement.condition(), local);
    
    final JBlock then = new JBlock(UNKNOWN); 
    block(statement.then(), then, local);
    JBlock elsee = null;
    if (statement.elsee().isDefined()) {
      elsee = new JBlock(UNKNOWN);
      block(statement.elsee().get(), elsee, local);
    }
    return new JIfStatement(UNKNOWN, condition, then, elsee);
  }
  
  public JExpression literal(Literal literal) {
    if (literal instanceof StringLiteral) {
      return program.getLiteralString(UNKNOWN, ((StringLiteral) literal).v());
    } else if (literal instanceof BooleanLiteral) {
      return program.getLiteralBoolean(((BooleanLiteral) literal).v());
    } else if (literal instanceof CharLiteral) {
      return program.getLiteralChar(((CharLiteral) literal).v());
    } else if (literal instanceof DoubleLiteral) {
      return program.getLiteralDouble(((DoubleLiteral) literal).v());
    } else if (literal instanceof FloatLiteral) {
      return program.getLiteralFloat(((FloatLiteral) literal).v());
    } else if (literal instanceof IntLiteral) {
      return program.getLiteralInt(((IntLiteral) literal).v());
    } else if (literal instanceof LongLiteral) {
      return program.getLiteralLong(((LongLiteral) literal).v());
    } else if (literal instanceof NullLiteral$) {
      return program.getLiteralNull();
    } else {
      throw new RuntimeException("to be implemented handling of " + literal);
    }
  }
  
  public JMethodCall methodCall(MethodCall call, LocalStack local) {
    JMethod method = typeMap.getMethod(call.signature().on().javaName(), 
        jsniSignature(call.signature()));
    if (method == null) {
      throw new InternalCompilerException("Failed to find method with signature " + call.signature());
    }
    try {
      JExpression on = expression(call.on(), local);
      
      List<JExpression> params = params(call.signature().jparamTypes(),
          call.jparams(), local);
      
      JMethodCall jcall = new JMethodCall(UNKNOWN, on, method);
      jcall.addArgs(params);
      return jcall;
    } catch (Exception e) {
      throw new InternalCompilerException("Error while compiling", e);
    }
  }

  public void methodDef(MethodDef def, JClassType enclosingClass, ClassDef classDef) {
    JMethod m = null;
    try {
      m = findMethod(enclosingClass, def.signature(classDef.name()));
      Map<String, JParameter> params = new HashMap<String, JParameter>();
      for (JParameter x : m.getParams()) {
        params.put(x.getName(), x);
      }
      if (def.body().isDefined()) {
        JMethodBody body = (JMethodBody) m.getBody();
        LocalStack local = new LocalStack(enclosingClass, body, params);
        local.pushBlock();
        JBlock block = body.getBlock();
        block(def.body().get(), block, local);
      }
    } catch (Exception e) {
      throw new InternalCompilerException(m, "Error while compiling", e);
    }
  }
  
  public JStatement methodStatement(Statement statement, LocalStack local) {
    if (statement instanceof VarDef) {
      return varDef((VarDef) statement, local);
    } else if (statement instanceof Assignment) {
      return assignment((Assignment) statement, local);
    } else if (statement instanceof Expression) {
      return expression((Expression) statement, local).makeStatement();
    } else if (statement instanceof If) {
      return ifStmt((If) statement, local);
    } else if (statement instanceof Return) {
      return returnStmt((Return) statement, local);
    } else if (statement instanceof Throw) {
      return throwStmt((Throw) statement, local);
    } else if (statement instanceof Try) {
      return tryStmt((Try) statement, local);
    } else if (statement instanceof While) {
      return whileStmt((While) statement, local);
    } else throw new RuntimeException("Unexpected case " + statement);
  }
  
  private JWhileStatement whileStmt(While statement, LocalStack local) {
    JExpression cond = expression(statement.condition(), local);
    JLabel label = null;
    if (statement.label().isDefined()) {
      label = new JLabel(UNKNOWN, statement.label().get());
      local.pushLabel(label);
    }
    JBlock block = new JBlock(UNKNOWN);
    block(statement.block(), block, local);
    if (label != null) {
      local.popLabel(label.getName());
    }
    return new JWhileStatement(UNKNOWN, cond, block);
  }
  
  private JTryStatement tryStmt(Try statement, LocalStack localStack) {
    JBlock block = new JBlock(UNKNOWN);
    block(statement.block(), block, localStack);
    List<JLocalRef> catchVars = new LinkedList<JLocalRef>();
    List<JBlock> catchBlocks = new LinkedList<JBlock>();
    //introduce block context for catch variables so they can be
    //discarded properly
    localStack.pushBlock();
    for (Tuple3<Ref, String, Block> x : statement.jcatches()) {
      JLocal local = JProgram.createLocal(UNKNOWN, x._2(), typeMap.get(x._1()),
          false, localStack.getEnclosingBody());
      localStack.addVar(x._2(), local);
      JLocalRef ref = new JLocalRef(UNKNOWN, local);
      JBlock catchBlock = new JBlock(UNKNOWN);
      block(x._3(), catchBlock, localStack);
      catchBlocks.add(catchBlock);
      catchVars.add(ref);
    }
    localStack.popBlock();
    JBlock finallyBlock = null;
    if (statement.finalizer().isDefined()) {
      finallyBlock = new JBlock(UNKNOWN);
      block(statement.finalizer().get(), finallyBlock, localStack);
    }
    return new JTryStatement(UNKNOWN, block, catchVars, catchBlocks, finallyBlock);
  }
  
  private JThrowStatement throwStmt(Throw statement, LocalStack local) {
    JExpression expression = expression(statement.expression(), local);
    return new JThrowStatement(UNKNOWN, expression);
  }

  private JReturnStatement returnStmt(Return statement, LocalStack local) {
    JExpression expression = null;
    if (statement.expression().isDefined()) {
      expression = expression(statement.expression().get(), local);
    }
    return new JReturnStatement(UNKNOWN, expression);
  }

  public JNewInstance newCall(NewCall call, LocalStack local) {
    JMethodCall methodCall = constructorCall(call.constructor(), local); 
    JConstructor constructor = (JConstructor) methodCall.getTarget();

    JNewInstance jnew = new JNewInstance(UNKNOWN, constructor, local.getEnclosingType());
    jnew.addArgs(methodCall.getArgs());
    return jnew;
  }
  
  public JMethodCall staticMethodCall(StaticMethodCall call, LocalStack local) {
    JMethod method = typeMap.getMethod(call.signature().on().javaName(), 
        jsniSignature(call.signature()));
    if (method == null) {
      throw new InternalCompilerException("Failed to find method with signature " + call.signature());
    }
    
    List<JExpression> params = params(call.signature().jparamTypes(),
        call.jparams(), local);
    
    JMethodCall jcall = new JMethodCall(UNKNOWN, null, method);
    jcall.addArgs(params);
    return jcall;
  }
  
  public JMethodCall constructorCall(ConstructorCall call, LocalStack local) {
    Signature signature = call.signature();
    JMethod method = typeMap.getMethod(signature.on().javaName(), jsniSignature(signature));
    List<JExpression> params = params(signature.jparamTypes(), call.jparams(), local);
    JMethodCall jcall = new JMethodCall(UNKNOWN, thisRef(local.getEnclosingType()), method);
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
  public JDeclarationStatement varDef(VarDef def, LocalStack localStack) {
    JLocal local = program.createLocal(UNKNOWN, def.name(), type(def.typ()),
        false, localStack.getEnclosingBody());
    localStack.addVar(def.name(), local);
    JLocalRef ref = new JLocalRef(UNKNOWN, local);
    JExpression expr = null;
    if (def.value().isDefined()) {
      expr = expression(def.value().get(), localStack);
    }
    return new JDeclarationStatement(UNKNOWN, ref, expr);
  }
  
  public JVariableRef varRef(VarRef ref, LocalStack local) {
    assert ref.name() != null;
    return local.resolveLocal(ref.name());
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
      LocalStack local) {
    assert paramTypes.size() == params.size();
    List<JExpression> result = new LinkedList<JExpression>();
    for (int i = 0; i < params.size(); i++) {
      JExpression expr = expression(params.get(i), local);
      result.add(expr);
    }
    return result;
  }
  
}
