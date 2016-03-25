/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layoutmetadata;

public interface LayoutMetadataReader2 extends LayoutMetadataReader {

    class Support {

        public static Support entitiesOnly() {
            return new Support(false,false,false,false,false,false,false);
        }

        private final boolean interfaces;
        private final boolean anonymous;
        private final boolean synthetic;
        private final boolean array;
        private final boolean enums;
        private final boolean applibValueTypes;
        private final boolean services;

        public Support(final boolean interfaces, final boolean anonymous, final boolean synthetic, final boolean array, final boolean enums, final boolean applibValueTypes, final boolean services) {
            this.interfaces = interfaces;
            this.anonymous = anonymous;
            this.synthetic = synthetic;
            this.array = array;
            this.enums = enums;
            this.applibValueTypes = applibValueTypes;
            this.services = services;
        }

        /**
         * Whether this implementation can provide metadata for interface types.
         */
        public boolean interfaces() {
            return interfaces;
        }

        /**
         * Whether this implementation can provide metadata for anonymous classes.
         */
        public boolean anonymous() {
            return anonymous;
        }

        /**
         * Whether this implementation can provide metadata for synthetic types.
         */
        public boolean synthetic() {
            return synthetic;
        }

        /**
         * Whether this implementation can provide metadata for arrays.
         */
        public boolean array() {
            return array;
        }

        /**
         * Whether this implementation can provide metadata for enums.
         */
        public boolean enums() {
            return enums;
        }

        /**
         * Whether this implementation can provide metadata for applib value types.
         */
        public boolean applibValueTypes() {
            return applibValueTypes;
        }

        /**
         * Whether this implementation can provide metadata for domain services.
         */
        public boolean services() {
            return services;
        }
    }

    /**
     * What types of classes are supported by this implementation.
     * @return
     */
    Support support();
}
