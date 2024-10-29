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
package org.apache.causeway.core.metamodel.util;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WhereContexts {

    /**
     * Utility, that detects the collection variant (standalone or parented),
     * based on the feature {@link Identifier} of the originating feature,
     * that is, the feature is either a plural member or a plural action result.
     */
    public Where collectionVariant(final Identifier featureId) {
        var whereContext = featureId.getType().isAction()
                    ? Where.STANDALONE_TABLES
                    : Where.PARENTED_TABLES;
        return whereContext;
    }

}
