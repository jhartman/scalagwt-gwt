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

import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.ast.JArrayType;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JNode;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JReferenceType;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.util.collect.HashMap;
import com.google.jribble.ast.Array;
import com.google.jribble.ast.Primitive;
import com.google.jribble.ast.Ref;
import com.google.jribble.ast.Type;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Contains maps into our Java nodes. Supports lookups from JDT nodes and from
 * JNodes that are from a separate AST.
 * 
 * TODO(spoon,grek) add a map for method parameters
 */
public class TypeMap {

  private static String methodDescriptor(String typeName, String jsniSignature) {
    return typeName + "." + jsniSignature;
  }

  /**
   * Maps Eclipse AST nodes to our JNodes.
   */
  private final Map<Binding, JNode> crossRefMap = new IdentityHashMap<Binding, JNode>();

  private Map<String, JDeclaredType> declaredTypesByName = new HashMap<String, JDeclaredType>();

  private Map<String, JField> fieldsByName = new HashMap<String, JField>();

  private Map<String, JMethod> methodsByName = new HashMap<String, JMethod>();
  
  private final Map<String, JPrimitiveType> primitive = new HashMap<String, JPrimitiveType>();

  /**
   * Centralizes creation and singleton management.
   */
  private final JProgram program;

  public TypeMap(JProgram program) {
    this.program = program;
    primitive.put(JPrimitiveType.BOOLEAN.getJsniSignatureName(), JPrimitiveType.BOOLEAN);
    primitive.put(JPrimitiveType.BYTE.getJsniSignatureName(), JPrimitiveType.BYTE);
    primitive.put(JPrimitiveType.CHAR.getJsniSignatureName(), JPrimitiveType.CHAR);
    primitive.put(JPrimitiveType.DOUBLE.getJsniSignatureName(), JPrimitiveType.DOUBLE);
    primitive.put(JPrimitiveType.FLOAT.getJsniSignatureName(), JPrimitiveType.FLOAT);
    primitive.put(JPrimitiveType.INT.getJsniSignatureName(), JPrimitiveType.INT);
    primitive.put(JPrimitiveType.LONG.getJsniSignatureName(), JPrimitiveType.LONG);
    primitive.put(JPrimitiveType.SHORT.getJsniSignatureName(), JPrimitiveType.SHORT);
  }

  public JNode get(Binding binding) {
    if (binding instanceof TypeVariableBinding) {
      TypeVariableBinding tvb = (TypeVariableBinding) binding;
      return get(tvb.erasure());
    } else if (binding instanceof ParameterizedTypeBinding) {
      ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) binding;
      return get(ptb.erasure());
    } else if (binding instanceof ParameterizedMethodBinding) {
      ParameterizedMethodBinding pmb = (ParameterizedMethodBinding) binding;
      return get(pmb.original());
    } else if (binding instanceof ParameterizedFieldBinding) {
      ParameterizedFieldBinding pfb = (ParameterizedFieldBinding) binding;
      return get(pfb.original());
    } else if (binding instanceof WildcardBinding) {
      WildcardBinding wcb = (WildcardBinding) binding;
      return get(wcb.erasure());
    }
    JNode result = internalGet(binding);
    if (result != null) {
      return result;
    } else if (binding instanceof BinaryTypeBinding) {
      BinaryTypeBinding binaryTypeBinding = (BinaryTypeBinding) binding;
      String qualifiedTypeName = new String(CharOperation.concatWith(binaryTypeBinding.compoundName, '.'));
      result = declaredTypesByName.get(qualifiedTypeName);
      if (result != null) {
        return result;
      }
    }
    InternalCompilerException ice = new InternalCompilerException(
        "Failed to get JNode");
    ice.addNode(binding.getClass().getName(), binding.toString(), null);
    throw ice;
  }

  public JDeclaredType get(JDeclaredType type) {
    return declaredTypesByName.get(type.getName());
  }

  public JMethod get(JMethod target) {
    String typeName = target.getEnclosingType().getName();
    String methodJsniSignature = target.getJsniSignature();
    return getMethod(typeName, methodJsniSignature);
  }

  public JPrimitiveType get(JPrimitiveType type) {
    // Primitives are interned, so just return it
    return type;
  }

  public JReferenceType get(JReferenceType type) {
    if (type instanceof JDeclaredType) {
      return get((JDeclaredType) type);
    }

    throw new UnknownNodeSubtype(type);
    // TODO(spoon, grek) fill in JArrayType and JNullType
  }

  public JType get(JType type) {
    if (type == null) {
      return type;
    }
    if (type instanceof JPrimitiveType) {
      return get((JPrimitiveType) type);
    }
    if (type instanceof JReferenceType) {
      return get((JReferenceType) type);
    }
    throw new UnknownNodeSubtype(type);
  }
  
  public JDeclaredType get(Ref ref) {
    String refName = ref.javaName();
    assert declaredTypesByName.containsKey(refName);
    return declaredTypesByName.get(refName);
  }
  
  public JType get(Type type) {
    if (type instanceof Primitive) {
      return primitive.get(((Primitive) type).name());
    } else if (type instanceof Ref) {
      return get((Ref) type);
    } else if (type instanceof com.google.jribble.ast.Void$) {
      return JPrimitiveType.VOID;
    } else if (type instanceof com.google.jribble.ast.Array) {
      com.google.jribble.ast.Array array = (Array) type;
      int dims = 1;
      while (array.typ() instanceof Array) {
        dims++;
        array = (Array) array.typ();
      }
      return program.getTypeArray(get(array.typ()), dims);
    } else {
      throw new InternalCompilerException("Unknown type " + type);
    }
  }

  public JMethod getMethod(String typeName, String methodJsniSignature) {
    return methodsByName.get(methodDescriptor(typeName, methodJsniSignature));
  }
  
  public JField getField(String typeName, String name) {
    return fieldsByName.get(fieldDescriptor(typeName, name));
  }

  public JProgram getProgram() {
    return program;
  }

  public void put(Binding binding, JNode to) {
    if (binding == null) {
      throw new InternalCompilerException("Trying to put null into typeMap.");
    }

    Object old = crossRefMap.put(binding, to);
    assert (old == null);
  }

  public void put(JDeclaredType type) {
    declaredTypesByName.put(type.getName(), type);
  }

  public void put(JField field) {
    fieldsByName.put(fieldDescriptor(field.getEnclosingType().getName(),
        field.getName()), field);
  }

  public void put(JMethod method) {
    methodsByName.put(methodDescriptor(method.getEnclosingType().getName(),
        method.getJsniSignature()), method);
  }

  public JNode tryGet(Binding binding) {
    return internalGet(binding);
  }

  private String fieldDescriptor(String typeName, String fieldName) {
    return typeName + "." + fieldName;
  }

  private JNode internalGet(Binding binding) {
    JNode cached = crossRefMap.get(binding);
    if (cached != null) {
      // Already seen this one.
      return cached;
    } else if (binding instanceof BaseTypeBinding) {
      BaseTypeBinding baseTypeBinding = (BaseTypeBinding) binding;
      // see org.eclipse.jdt.internal.compiler.lookup.TypeIds constants
      switch (baseTypeBinding.id) {
        case TypeIds.T_JavaLangObject:
          // here for consistency, should already be cached
          return program.getTypeJavaLangObject();
        case TypeIds.T_char:
          return program.getTypePrimitiveChar();
        case TypeIds.T_byte:
          return program.getTypePrimitiveByte();
        case TypeIds.T_short:
          return program.getTypePrimitiveShort();
        case TypeIds.T_boolean:
          return program.getTypePrimitiveBoolean();
        case TypeIds.T_void:
          return program.getTypeVoid();
        case TypeIds.T_long:
          return program.getTypePrimitiveLong();
        case TypeIds.T_double:
          return program.getTypePrimitiveDouble();
        case TypeIds.T_float:
          return program.getTypePrimitiveFloat();
        case TypeIds.T_int:
          return program.getTypePrimitiveInt();
        case TypeIds.T_JavaLangString:
          // here for consistency, should already be cached
          return program.getTypeJavaLangString();
        case TypeIds.T_null:
          return program.getTypeNull();
        case TypeIds.T_undefined:
        default:
          return null;
      }
    } else if (binding instanceof ArrayBinding) {
      ArrayBinding arrayBinding = (ArrayBinding) binding;

      // Compute the JType for the leaf type
      JType leafType = (JType) get(arrayBinding.leafComponentType);

      // Don't create a new JArrayType; use TypeMap to get the singleton
      // instance
      JArrayType arrayType = program.getTypeArray(leafType,
          arrayBinding.dimensions);

      return arrayType;
    } else {
      return null;
    }
  }
}
