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

    public static class Flags {

        private static final int IGNORE_NOOP = 1;

        private final int mask;

        public Flags() {
            this(0);
        }
        private Flags(final int mask) {
            this.mask = mask;
        }

        public Flags ignoreNoop() {
            return new Flags(mask | IGNORE_NOOP);
        }

        public boolean isIgnoreNoop() {
            return (mask & IGNORE_NOOP) == IGNORE_NOOP;
        }
    }

    @Programmatic
    MetamodelDto exportMetaModel(final Flags flags);

}
