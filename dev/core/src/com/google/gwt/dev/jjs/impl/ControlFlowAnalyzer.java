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

import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JAbsentArrayDimension;
import com.google.gwt.dev.jjs.ast.JArrayType;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JCastOperation;
import com.google.gwt.dev.jjs.ast.JClassLiteral;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JInterfaceType;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JModVisitor;
import com.google.gwt.dev.jjs.ast.JNewArray;
import com.google.gwt.dev.jjs.ast.JNewInstance;
import com.google.gwt.dev.jjs.ast.JNode;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JStringLiteral;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVariable;
import com.google.gwt.dev.jjs.ast.JVariableRef;
import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.jjs.ast.js.JsniFieldRef;
import com.google.gwt.dev.jjs.ast.js.JsniMethodBody;
import com.google.gwt.dev.jjs.ast.js.JsniMethodRef;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class finds out what code in a program is live based on starting
 * execution at a specified location.
 */
public class ControlFlowAnalyzer {

  /**
   * Marks as "referenced" any types, methods, and fields that are reachable.
   * Also marks as "instantiable" any the classes and interfaces that can
   * possibly be instantiated.
   * 
   * TODO(later): make RescueVisitor use less stack?
   */
  private class RescueVisitor extends JVisitor {
    @Override
    public boolean visit(JArrayType type, Context ctx) {
      assert (referencedTypes.contains(type));
      boolean isInstantiated = instantiatedTypes.contains(type);

      JType leafType = type.getLeafType();
      int dims = type.getDims();

      // Rescue my super array type
      if (leafType instanceof JReferenceType) {
        JReferenceType rLeafType = (JReferenceType) leafType;
        if (rLeafType.extnds != null) {
          JArrayType superArray = program.getTypeArray(rLeafType.extnds, dims);
          rescue(superArray, true, isInstantiated);
        }

        for (int i = 0; i < rLeafType.implments.size(); ++i) {
          JInterfaceType intfType = rLeafType.implments.get(i);
          JArrayType intfArray = program.getTypeArray(intfType, dims);
          rescue(intfArray, true, isInstantiated);
        }
      }

      // Rescue the base Array type
      rescue(program.getIndexedType("Array"), true, isInstantiated);
      return false;
    }

    @Override
    public boolean visit(JBinaryOperation x, Context ctx) {
      // special string concat handling
      if ((x.getOp() == JBinaryOperator.ADD || x.getOp() == JBinaryOperator.ASG_ADD)
          && x.getType() == program.getTypeJavaLangString()) {
        rescueByConcat(x.getLhs().getType());
        rescueByConcat(x.getRhs().getType());
      } else if (x.getOp() == JBinaryOperator.ASG) {
        // Don't rescue variables that are merely assigned to and never read
        boolean doSkip = false;
        JExpression lhs = x.getLhs();
        if (lhs.hasSideEffects() || isVolatileField(lhs)) {
          // If the lhs has side effects, skipping it would lose the side
          // effect.
          // If the lhs is volatile, also keep it. This behavior provides a
          // useful
          // idiom for test cases to prevent code from being pruned.
        } else if (lhs instanceof JLocalRef) {
          // locals are ok to skip
          doSkip = true;
        } else if (lhs instanceof JParameterRef) {
          // parameters are ok to skip
          doSkip = true;
        } else if (lhs instanceof JFieldRef) {
          // fields must rescue the qualifier
          doSkip = true;
          JFieldRef fieldRef = (JFieldRef) lhs;
          JExpression instance = fieldRef.getInstance();
          if (instance != null) {
            accept(instance);
          }
        }

        if (doSkip) {
          accept(x.getRhs());
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean visit(JCastOperation x, Context ctx) {
      // Rescue any JavaScriptObject type that is the target of a cast.
      JType targetType = x.getCastType();
      if (program.isJavaScriptObject(targetType)) {
        rescue((JReferenceType) targetType, true, true);
      }
      return true;
    }

    @Override
    public boolean visit(JClassLiteral x, Context ctx) {
      /*
       * Rescue just slightly less than what would normally be rescued for a
       * field reference to the literal's field. Rescue the field itself, and
       * its initializer, but do NOT rescue the whole enclosing class. That
       * would pull in the clinit of that class, which has initializers for all
       * the class literals, which in turn have all of the strings of all of the
       * class names.
       */
      JField field = x.getField();
      rescue(field);
      accept(field.getInitializer());
      referencedTypes.add(field.getEnclosingType());
      liveFieldsAndMethods.add(field.getEnclosingType().methods.get(0));
      return true;
    }

    @Override
    public boolean visit(JClassType type, Context ctx) {
      assert (referencedTypes.contains(type));
      boolean isInstantiated = instantiatedTypes.contains(type);

      // Rescue my super type
      rescue(type.extnds, true, isInstantiated);

      // Rescue my clinit (it won't ever be explicitly referenced)
      rescue(type.methods.get(0));

      // JLS 12.4.1: don't rescue my super interfaces just because I'm rescued.
      // However, if I'm instantiated, let's mark them as instantiated.
      for (int i = 0; i < type.implments.size(); ++i) {
        JInterfaceType intfType = type.implments.get(i);
        rescue(intfType, false, isInstantiated);
      }

      rescueMethodsIfInstantiable(type);

      return false;
    }

    @Override
    public boolean visit(JDeclarationStatement x, Context ctx) {
      /*
       * A declaration by itself doesn't rescue a local (even if it has an
       * initializer). Writes don't count, only reads.
       */
      if (x.getInitializer() != null) {
        accept(x.getInitializer());
      }

      // If the lhs is a field ref, we have to visit its qualifier.
      JVariableRef variableRef = x.getVariableRef();
      if (variableRef instanceof JFieldRef) {
        JFieldRef fieldRef = (JFieldRef) variableRef;
        JExpression instance = fieldRef.getInstance();
        if (instance != null) {
          accept(instance);
        }
      }
      return false;
    }

    @Override
    public boolean visit(JFieldRef ref, Context ctx) {
      JField target = ref.getField();

      // JLS 12.4.1: references to static, non-final, or
      // non-compile-time-constant fields rescue the enclosing class.
      // JDT already folds in compile-time constants as literals, so we must
      // rescue the enclosing types for any static fields that make it here.
      if (target.isStatic()) {
        rescue(target.getEnclosingType(), true, false);
      }
      rescue(target);
      return true;
    }

    @Override
    public boolean visit(JInterfaceType type, Context ctx) {
      boolean isReferenced = referencedTypes.contains(type);
      boolean isInstantiated = instantiatedTypes.contains(type);
      assert (isReferenced || isInstantiated);

      // Rescue my clinit (it won't ever be explicitly referenced
      rescue(type.methods.get(0));

      // JLS 12.4.1: don't rescue my super interfaces just because I'm rescued.
      // However, if I'm instantiated, let's mark them as instantiated.
      if (isInstantiated) {
        for (int i = 0; i < type.implments.size(); ++i) {
          JInterfaceType intfType = type.implments.get(i);
          rescue(intfType, false, true);
        }
      }

      // visit any field initializers
      for (int i = 0; i < type.fields.size(); ++i) {
        JField it = type.fields.get(i);
        accept(it);
      }

      rescueMethodsIfInstantiable(type);

      return false;
    }

    @Override
    public boolean visit(JLocalRef ref, Context ctx) {
      JLocal target = ref.getLocal();
      rescue(target);
      return true;
    }

    @Override
    public boolean visit(final JMethod x, Context ctx) {
      JReferenceType enclosingType = x.getEnclosingType();
      if (program.isJavaScriptObject(enclosingType)) {
        // Calls to JavaScriptObject types rescue those types.
        boolean instance = !x.isStatic() || program.isStaticImpl(x);
        rescue(enclosingType, true, instance);
      } else if (x.isStatic()) {
        // JLS 12.4.1: references to static methods rescue the enclosing class
        rescue(enclosingType, true, false);
      }

      if (x.isNative()) {
        // Manually rescue native parameter references
        final JsniMethodBody body = (JsniMethodBody) x.getBody();
        final JsFunction func = body.getFunc();

        new JsVisitor() {
          @Override
          public void endVisit(JsNameRef nameRef, JsContext<JsExpression> ctx) {
            JsName ident = nameRef.getName();

            if (ident != null) {
              // If we're referencing a parameter, rescue the associated
              // JParameter
              int index = func.getParameters().indexOf(ident.getStaticRef());
              if (index != -1) {
                rescue(x.params.get(index));
              }
            }
          }
        }.accept(func);
      }

      return true;
    }

    @Override
    public boolean visit(JMethodCall call, Context ctx) {
      JMethod method = call.getTarget();
      if (method.isStatic()
          || program.isJavaScriptObject(method.getEnclosingType())
          || instantiatedTypes.contains(method.getEnclosingType())) {
        rescue(method);
      } else {
        // It's a virtual method whose class is not instantiable
        if (!liveFieldsAndMethods.contains(method)) {
          methodsLiveExceptForInstantiability.add(method);
        }
      }

      return true;
    }

    @Override
    public boolean visit(JNewArray newArray, Context ctx) {
      // rescue and instantiate the array type
      JArrayType arrayType = newArray.getArrayType();
      if (newArray.dims != null) {
        // rescue my type and all the implicitly nested types (with fewer dims)
        int nDims = arrayType.getDims();
        JType leafType = arrayType.getLeafType();
        assert (newArray.dims.size() == nDims);
        for (int i = 0; i < nDims; ++i) {
          if (newArray.dims.get(i) instanceof JAbsentArrayDimension) {
            break;
          }
          rescue(program.getTypeArray(leafType, nDims - i), true, true);
        }
      } else {
        // just rescue my own specific type
        rescue(arrayType, true, true);
      }
      return true;
    }

    @Override
    public boolean visit(JNewInstance newInstance, Context ctx) {
      // rescue and instantiate the target class!
      rescue(newInstance.getClassType(), true, true);
      return true;
    }

    @Override
    public boolean visit(JParameterRef x, Context ctx) {
      // rescue the parameter for future pruning purposes
      rescue(x.getParameter());
      return true;
    }

    @Override
    public boolean visit(JsniFieldRef x, Context ctx) {
      /*
       * SPECIAL: this could be an assignment that passes a value from
       * JavaScript into Java.
       */
      if (x.isLvalue()) {
        maybeRescueJavaScriptObjectPassingIntoJava(x.getField().getType());
      }
      // JsniFieldRef rescues as JFieldRef
      return visit((JFieldRef) x, ctx);
    }

    @Override
    public boolean visit(JsniMethodBody body, Context ctx) {
      liveStrings.addAll(body.getUsedStrings());
      return true;
    }

    @Override
    public boolean visit(JsniMethodRef x, Context ctx) {
      /*
       * SPECIAL: each argument of the call passes a value from JavaScript into
       * Java.
       */
      ArrayList<JParameter> params = x.getTarget().params;
      for (int i = 0, c = params.size(); i < c; ++i) {
        JParameter param = params.get(i);
        maybeRescueJavaScriptObjectPassingIntoJava(param.getType());

        /*
         * Because we're not currently tracking methods through JSNI, we need to
         * assume that it's not safe to prune parameters of a method referenced
         * as such.
         * 
         * A better solution would be to perform basic escape analysis to ensure
         * that the function reference never escapes, or at minimum, ensure that
         * the method is immediately called after retrieving the method
         * reference.
         */
        rescue(param);
      }
      // JsniMethodRef rescues as JMethodCall
      return visit((JMethodCall) x, ctx);
    }

    @Override
    public boolean visit(JStringLiteral literal, Context ctx) {
      liveStrings.add(literal.getValue());

      // rescue and instantiate java.lang.String
      rescue(program.getTypeJavaLangString(), true, true);
      return true;
    }

    private boolean isVolatileField(JExpression x) {
      if (x instanceof JFieldRef) {
        JFieldRef xFieldRef = (JFieldRef) x;
        if (xFieldRef.getField().isVolatile()) {
          return true;
        }
      }

      return false;
    }

    /**
     * Subclasses of JavaScriptObject are never instantiated directly. They are
     * created "magically" when a JSNI method passes a reference to an existing
     * JS object into Java code. If any point in the program can pass a value
     * from JS into Java which could potentially be cast to JavaScriptObject, we
     * must rescue JavaScriptObject.
     * 
     * @param type The type of the value passing from Java to JavaScript.
     * @see com.google.gwt.core.client.JavaScriptObject
     */
    private void maybeRescueJavaScriptObjectPassingIntoJava(JType type) {
      boolean doIt = false;
      if (program.isJavaScriptObject(type)
          || type == program.getTypeJavaLangString()) {
        doIt = true;
      } else if (type instanceof JArrayType) {
        /*
         * Hackish: in our own JRE we sometimes create "not quite baked" arrays
         * in JavaScript for expediency.
         */
        JArrayType arrayType = (JArrayType) type;
        JType elementType = arrayType.getElementType();
        if (elementType instanceof JPrimitiveType
            || elementType == program.getTypeJavaLangString()
            || program.isJavaScriptObject(elementType)) {
          doIt = true;
        }
      }
      if (doIt) {
        rescue((JReferenceType) type, true, true);
      }
    }

    private boolean rescue(JMethod method) {
      if (method != null) {
        if (!liveFieldsAndMethods.contains(method)) {
          liveFieldsAndMethods.add(method);
          methodsLiveExceptForInstantiability.remove(method);

          accept(method);

          if (method.isNative()) {
            /*
             * SPECIAL: returning from this method passes a value from
             * JavaScript into Java.
             */
            maybeRescueJavaScriptObjectPassingIntoJava(method.getType());
          }

          rescueOverridingMethods(method);

          return true;
        }
      }
      return false;
    }

    private void rescue(JReferenceType type, boolean isReferenced,
        boolean isInstantiated) {
      if (type != null) {

        boolean doVisit = false;
        if (isInstantiated && !instantiatedTypes.contains(type)) {
          instantiatedTypes.add(type);
          doVisit = true;
        }

        if (isReferenced && !referencedTypes.contains(type)) {
          referencedTypes.add(type);
          doVisit = true;
        }

        if (doVisit) {
          accept(type);
        }
      }
    }

    private void rescue(JVariable var) {
      if (var != null) {
        liveFieldsAndMethods.add(var);
      }
    }

    /**
     * Handle special rescues needed implicitly to support concat.
     */
    private void rescueByConcat(JType type) {
      JClassType stringType = program.getTypeJavaLangString();
      JPrimitiveType charType = program.getTypePrimitiveChar();
      if (type instanceof JReferenceType && type != stringType
          && type != program.getTypeNull()) {
        /*
         * Any reference types (except String, which works by default) that take
         * part in a concat must rescue java.lang.Object.toString().
         * 
         * TODO: can we narrow the focus by walking up the type hierarchy or
         * doing explicit toString calls?
         */
        JMethod toStringMethod = program.getIndexedMethod("Object.toString");
        rescue(toStringMethod);
      } else if (type == charType) {
        /*
         * Characters must rescue String.valueOf(char)
         */
        if (stringValueOfChar == null) {
          for (int i = 0; i < stringType.methods.size(); ++i) {
            JMethod meth = stringType.methods.get(i);
            if (meth.getName().equals("valueOf")) {
              List<JType> params = meth.getOriginalParamTypes();
              if (params.size() == 1) {
                if (params.get(0) == charType) {
                  stringValueOfChar = meth;
                  break;
                }
              }
            }
          }
          assert (stringValueOfChar != null);
        }
        rescue(stringValueOfChar);
      }
    }

    /**
     * If the type is instantiable, rescue any of its virtual methods that a
     * previously seen method call could call.
     */
    private void rescueMethodsIfInstantiable(JReferenceType type) {
      if (instantiatedTypes.contains(type)) {
        for (JMethod method : type.methods) {
          if (!method.isStatic()) {
            if (methodsLiveExceptForInstantiability.contains(method)) {
              rescue(method);
              continue;
            }
          }
        }
      }
    }

    /**
     * Assume that <code>method</code> is live. Rescue any overriding methods
     * that might be called if <code>method</code> is called through virtual
     * dispatch.
     */
    private void rescueOverridingMethods(JMethod method) {
      if (!method.isStatic()) {

        List<JMethod> overriders = methodsThatOverrideMe.get(method);
        if (overriders != null) {
          for (JMethod overrider : overriders) {
            if (liveFieldsAndMethods.contains(overrider)) {
              // The override is already alive, do nothing.
            } else if (instantiatedTypes.contains(overrider.getEnclosingType())) {
              // The enclosing class is alive, make my override reachable.
              rescue(overrider);
            } else {
              // The enclosing class is not yet alive, put override in limbo.
              methodsLiveExceptForInstantiability.add(overrider);
            }
          }
        }
      }
    }
  }

  private Set<JReferenceType> instantiatedTypes = new HashSet<JReferenceType>();
  private Set<JNode> liveFieldsAndMethods = new HashSet<JNode>();
  private Set<String> liveStrings = new HashSet<String>();

  /**
   * Schrodinger's methods... aka "limbo". :) These are instance methods that
   * seem to be reachable, only their enclosing type is uninstantiable. We place
   * these methods into purgatory until/unless the enclosing type is found to be
   * instantiable.
   */
  private Set<JMethod> methodsLiveExceptForInstantiability = new HashSet<JMethod>();

  /**
   * A precomputed map of all instance methods onto a set of methods that
   * override each key method.
   */
  private Map<JMethod, List<JMethod>> methodsThatOverrideMe;

  private final JProgram program;
  private Set<JReferenceType> referencedTypes = new HashSet<JReferenceType>();
  private final RescueVisitor rescuer = new RescueVisitor();
  private JMethod stringValueOfChar = null;

  public ControlFlowAnalyzer(ControlFlowAnalyzer cfa) {
    program = cfa.program;
    instantiatedTypes = new HashSet<JReferenceType>(cfa.instantiatedTypes);
    liveFieldsAndMethods = new HashSet<JNode>(cfa.liveFieldsAndMethods);
    referencedTypes = new HashSet<JReferenceType>(cfa.referencedTypes);
    stringValueOfChar = cfa.stringValueOfChar;
    liveStrings = new HashSet<String>(cfa.liveStrings);
    methodsLiveExceptForInstantiability = new HashSet<JMethod>(
        cfa.methodsLiveExceptForInstantiability);
    methodsThatOverrideMe = cfa.methodsThatOverrideMe;
  }

  public ControlFlowAnalyzer(JProgram program) {
    this.program = program;
    buildMethodsOverriding();
  }

  /**
   * Return the complete set of types that have been instantiated.
   */
  public Set<JReferenceType> getInstantiatedTypes() {
    return instantiatedTypes;
  }

  /**
   * Return all methods that could be executed, and all variables that could be
   * read, based on the given entry points so far.
   */
  public Set<? extends JNode> getLiveFieldsAndMethods() {
    return liveFieldsAndMethods;
  }

  public Set<String> getLiveStrings() {
    return liveStrings;
  }

  /**
   * Return the complete set of types that have been referenced.
   */
  public Set<? extends JReferenceType> getReferencedTypes() {
    return referencedTypes;
  }

  /**
   * Traverse all code executed by <code>expr</code>.
   */
  public void traverseFrom(JExpression expr) {
    rescuer.accept(expr);
  }

  /**
   * Assume <code>method</code> is live, and find out what else might execute.
   */
  public void traverseFrom(JMethod method) {
    rescuer.rescue(method);
  }

  /**
   * Trace all code needed by class literal constructor expressions except for
   * the string literals they include. At the time of writing, these would
   * include the factory methods for class literals.
   */
  public void traverseFromClassLiteralFactories() {
    class ReplaceStringLiterals extends JModVisitor {
      @Override
      public void endVisit(JStringLiteral stringLiteral, Context ctx) {
        ctx.replaceMe(program.getLiteralNull());
      }
    }

    final JModVisitor stringLiteralReplacer = new ReplaceStringLiterals();
    final CloneExpressionVisitor cloner = new CloneExpressionVisitor(program);

    class ClassLitTraverser extends JVisitor {
      @Override
      public void endVisit(JClassLiteral classLiteral, Context ctx) {
        JExpression initializer = classLiteral.getField().getInitializer();
        JExpression initializerWithoutStrings = stringLiteralReplacer.accept(cloner.cloneExpression(initializer));
        rescuer.accept(initializerWithoutStrings);
      }
    }

    (new ClassLitTraverser()).accept(program);
  }

  public void traverseFromLeftoversFragmentHasLoaded() {
    if (program.entryMethods.size() > 1) {
      traverseFrom(program.getIndexedMethod("AsyncFragmentLoader.leftoversFragmentHasLoaded"));
    }
  }

  public void traverseFromReferenceTo(JReferenceType type) {
    rescuer.rescue(type, true, false);
  }

  private void buildMethodsOverriding() {
    methodsThatOverrideMe = new HashMap<JMethod, List<JMethod>>();
    for (JReferenceType type : program.getDeclaredTypes()) {
      for (JMethod method : type.methods) {
        for (JMethod overridden : program.typeOracle.getAllOverrides(method)) {
          List<JMethod> overs = methodsThatOverrideMe.get(overridden);
          if (overs == null) {
            overs = new ArrayList<JMethod>();
            methodsThatOverrideMe.put(overridden, overs);
          }
          overs.add(method);
        }
      }
    }
  }
}