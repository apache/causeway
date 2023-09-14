/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.commons.semantics;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableCollection;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.collections._Collections;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.semantics.CollectionSemantics.InvocationHandlingPolicy;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

/**
 * Supported collection types, including arrays.
 * Order matters, as class substitution is processed on first matching type.
 * <p>
 * Plural <i>Action Parameter</i> types cannot be more special than what we offer here.
 */
@RequiredArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
public enum CollectionSemantics {
    ARRAY(Array.class, MethodSets.EMPTY){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return _Arrays.toArray(_Casts.uncheckedCast(plural), elementType);
        }
    },
    VECTOR(Vector.class, MethodSets.LIST){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return new Vector(plural);
        }
    },
    LIST(List.class, MethodSets.LIST){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return Collections.unmodifiableList(plural);
        }
    },
    SORTED_SET(SortedSet.class, MethodSets.COLLECTION){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return _Collections.asUnmodifiableSortedSet(plural);
        }
    },
    SET(Set.class, MethodSets.COLLECTION){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return _Collections.asUnmodifiableSet(plural);
        }
    },
    COLLECTION(Collection.class, MethodSets.COLLECTION){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return Collections.unmodifiableCollection(plural);
        }
    },
    CAN(Can.class, MethodSets.CAN){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return Can.ofCollection(plural);
        }
    },
    IMMUTABLE_COLLECTION(ImmutableCollection.class, MethodSets.COLLECTION){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            return CAN.asContainerType(elementType, plural);
        }
    },
    /**
     * not supported, however holds a usable {@link InvocationHandlingPolicy}
     */
    MAP(Map.class, MethodSets.MAP){
        @Override public Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> plural) {
            throw new UnsupportedOperationException("Map is not a collection");
        }
    }
    ;

    public static interface InvocationHandlingPolicy {
        /**
         * Methods which will trigger CollectionMethodEvent(s)
         * on invocation.
         */
        List<Method> getIntercepted();
        /**
         * Methods which will cause an {@link UnsupportedOperationException}
         * on invocation.
         */
        List<Method> getVetoed();
    }

    public boolean isArray() {return this == ARRAY;}
    public boolean isVector() {return this == VECTOR;}
    public boolean isList() {return this == LIST;}
    public boolean isSortedSet() {return this == SORTED_SET;}
    public boolean isSet() {return this == SET;}
    public boolean isCollection() {return this == COLLECTION;}
    public boolean isCan() {return this == CAN;}
    public boolean isImmutableCollection() {return this == IMMUTABLE_COLLECTION;}
    public boolean isMap() {return this == MAP;}
    //
    public boolean isSetAny() {return isSet() || isSortedSet(); }
    @Getter private final Class<?> containerType;
    @Getter private final InvocationHandlingPolicy invocationHandlingPolicy;

    private static final ImmutableEnumSet<CollectionSemantics> all =
            ImmutableEnumSet.allOf(CollectionSemantics.class);
    @Getter @Accessors(fluent = true)
    private static final ImmutableEnumSet<CollectionSemantics> typeSubstitutors = all.remove(ARRAY);

    public static Optional<CollectionSemantics> valueOf(final @Nullable Class<?> type) {
        if(type==null) return Optional.empty();
        return type.isArray()
                ? Optional.of(CollectionSemantics.ARRAY)
                : all.stream()
                    .filter(collType->collType.getContainerType().isAssignableFrom(type))
                    .filter(t->!t.isMap()) // not supported
                    .findFirst();
    }

    public static CollectionSemantics valueOfElseFail(final @Nullable Class<?> type) {
        return valueOf(type).orElseThrow(()->_Exceptions.illegalArgument(
                        "failed to lookup CollectionSemantics for type %s", type));
    }

    public Object unmodifiableCopyOf(
            final Class<?> elementType, final @NonNull Iterable<?> nonScalar) {
        // defensive copy
        return asContainerType(elementType,
                _NullSafe.stream(nonScalar).collect(Collectors.toList()));
    }

    protected abstract Object asContainerType(
            final Class<?> elementType, final @NonNull List<?> nonScalar);
}

//TODO perhaps needs an update to reflect Java 7->11 Language changes
@RequiredArgsConstructor
enum MethodSets implements InvocationHandlingPolicy {
  EMPTY(List.of(), List.of()),
  COLLECTION(
          // intercepted ...
          List.of(
                  getMethod(Collection.class, "contains", Object.class),
                  getMethod(Collection.class, "size"),
                  getMethod(Collection.class, "isEmpty")
          ),
          // vetoed ...
          List.of(
                  getMethod(Collection.class, "add", Object.class),
                  getMethod(Collection.class, "remove", Object.class),
                  getMethod(Collection.class, "addAll", Collection.class),
                  getMethod(Collection.class, "removeAll", Collection.class),
                  getMethod(Collection.class, "retainAll", Collection.class),
                  getMethod(Collection.class, "clear")
          )),
  LIST(
          // intercepted ...
          _Lists.concat(
                  COLLECTION.intercepted,
                  List.of(
                          getMethod(List.class, "get", int.class)
                  )
          ),
          // vetoed ...
          _Lists.concat(
                  COLLECTION.vetoed,
                  List.of(
                  )
          )),
  CAN(
          // intercepted ...
          _Lists.concat(
                  COLLECTION.intercepted,
                  List.of(
                          getMethod(Can.class, "get", int.class),
                          getMethod(Can.class, "getElseFail", int.class),
                          getMethod(Can.class, "getFirst"),
                          getMethod(Can.class, "getFirstElseFail"),
                          getMethod(Can.class, "getLast"),
                          getMethod(Can.class, "getLastElseFail")
                  )
          ),
          // vetoed ...
          _Lists.concat(
                  COLLECTION.vetoed,
                  List.of(
                  )
          )),
  MAP(
          // intercepted ...
          List.of(
                  getMethod(Map.class, "containsKey", Object.class),
                  getMethod(Map.class, "containsValue", Object.class),
                  getMethod(Map.class, "size"),
                  getMethod(Map.class, "isEmpty")
          ),
          // vetoed ...
          List.of(
                  getMethod(Map.class, "put", Object.class, Object.class),
                  getMethod(Map.class, "remove", Object.class),
                  getMethod(Map.class, "putAll", Map.class),
                  getMethod(Map.class, "clear")
          ))
  ;
  @Getter private final List<Method> intercepted;
  @Getter private final List<Method> vetoed;
  // -- HELPER
  @SneakyThrows
  private static Method getMethod(
          final Class<?> cls,
          final String methodName,
          final Class<?>... parameterClass) {
      return cls.getMethod(methodName, parameterClass);
  }
}
