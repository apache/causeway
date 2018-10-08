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
package org.apache.isis.applib.services.metamodel;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

public interface MetaModelService6 extends MetaModelService5 {

    public static class Config {

        private static final int IGNORE_NOOP_FACETS = 1;
        private static final int IGNORE_INTERFACES = 2;
        private static final int IGNORE_ABSTRACT_CLASSES = 4;
        private static final int IGNORE_BUILT_IN_VALUE_TYPES = 8;

        private final int mask;
        private final String packagePrefix;

        public Config() {
            this(0, null);
        }
        private Config(final int mask, final String packagePrefix) {
            this.mask = mask;
            this.packagePrefix = packagePrefix;
        }
        private Config(final int mask) {
            this(mask, null);
        }


        public Config withIgnoreNoop() {
            return newConfigWith(IGNORE_NOOP_FACETS);
        }

        public Config withIgnoreInterfaces() {
            return newConfigWith(IGNORE_INTERFACES);
        }
        public Config withIgnoreAbstractClasses() {
            return newConfigWith(IGNORE_ABSTRACT_CLASSES);
        }
        public Config withIgnoreBuiltInValueTypes() {
            return newConfigWith(IGNORE_BUILT_IN_VALUE_TYPES);
        }

        private Config newConfigWith(final int x) {
            return new Config(mask | x, packagePrefix);
        }

        public Config withPackagePrefix(final String packagePrefix) {
            return new Config(mask, packagePrefix);
        }

        public boolean isIgnoreNoop() {
            return hasFlag(IGNORE_NOOP_FACETS);
        }

        public boolean isIgnoreInterfaces() {
            return hasFlag(IGNORE_INTERFACES);
        }

        public boolean isIgnoreAbstractClasses() {
            return hasFlag(IGNORE_ABSTRACT_CLASSES);
        }
        public boolean isIgnoreBuiltInValueTypes() {
            return hasFlag(IGNORE_BUILT_IN_VALUE_TYPES);
        }

        public String getPackagePrefix() {
            return packagePrefix;
        }

        private boolean hasFlag(final int x) {
            return (mask & x) == x;
        }

    }

    @Programmatic
    MetamodelDto exportMetaModel(final Config config);

}
