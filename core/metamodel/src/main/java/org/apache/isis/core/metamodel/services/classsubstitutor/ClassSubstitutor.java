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

package org.apache.isis.core.metamodel.services.classsubstitutor;

import java.io.Serializable;

import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.Value;

/**
 * Provides capability to translate or ignore classes.
 */
public interface ClassSubstitutor {
    
    /**
     * Captures 3 possible directives:
     * <ul>
     * <li>do not replace the class (NOP)</li>
     * <li>ignore the class, that is do not introspect ever</li>
     * <li>replace the class with another (non-null)</li>
     * </ul> 
     * @since 2.0
     */
    @Value(staticConstructor = "of")
    static class Substitution implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final Substitution NOP = Substitution.of(Type.DO_NOT_REPLACE, null);
        private static final Substitution IGNORE = Substitution.of(Type.IGNORE_CLASS, null);
        
        private static enum Type {
            DO_NOT_REPLACE,
            IGNORE_CLASS,
            REPLACE_WITH_OTHER_CLASS,
        }
        
        @NonNull Type type;        
        @Nullable Class<?> replacement;

        /**
         * for framework internal use only, not required for ClassSubstitutor implementations!
         * (forces a class to be never replaced)
         */
        public static Substitution dontReplaceClass() {
            return NOP;
        }
        
        public static Substitution ignoreClass() {
            return IGNORE;
        }
        
        public static Substitution replaceClass(@lombok.NonNull @org.springframework.lang.NonNull Class<?> cls) {
            return of(Type.REPLACE_WITH_OTHER_CLASS, cls);
        }
        
        /**
         * @return whether the replacement is an identity operation (do nothing) 
         */
        public boolean isDoNotReplace() {
            return type == Type.DO_NOT_REPLACE;
        }
        
        /**
         * @return whether to ignore the class (never introspect)
         */
        public boolean isIgnore() {
            return type == Type.IGNORE_CLASS;
        }

        /**
         * @return whether to replace the class with registered replacement
         */
        public boolean isReplace() {
            return type == Type.REPLACE_WITH_OTHER_CLASS;            
        }

        public Class<?> replace(Class<?> cls) {
            if(isIgnore()) {
                return null;
            }
            return isReplace() ? getReplacement() : cls;
        }
        
    }
    
    // -- INTERFACE
    
    /**
     * @param cls
     * @return Substitution for given {@code cls} if any
     * or Substitution.ignore(), when {@code cls} shall be ignored 
     * @implNote most likely do not return Substitution.dontReplaceClass(), return {@code null} instead
     * @since 2.0
     */
    @Nullable
    Substitution getSubstitution(@lombok.NonNull @org.springframework.lang.NonNull Class<?> cls);
     
    
}
