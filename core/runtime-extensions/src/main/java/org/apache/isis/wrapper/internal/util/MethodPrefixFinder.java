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

package org.apache.isis.wrapper.internal.util;

import java.util.Arrays;
import java.util.LinkedHashSet;

public final class MethodPrefixFinder {

    // a Linked Hash Set is used to ensure that the ordering is preserved.
    public final static LinkedHashSet<String> ALL_PREFIXES = new LinkedHashSet<String>() {
        private static final long serialVersionUID = 1L;
        {
            // collection prefixes are added first because we want to
            // test validateAddTo and validateRemoveFrom before validate
            addAll(Arrays.asList(Constants.COLLECTION_PREFIXES));
            addAll(Arrays.asList(Constants.PROPERTY_PREFIXES));
            addAll(Arrays.asList(Constants.ACTION_PREFIXES));
        }
    };

    public String findPrefix(final String methodName) {
        for (final String prefix : ALL_PREFIXES) {
            if (methodName.startsWith(prefix)) {
                return prefix;
            }
        }
        return "";
    }

}
