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
package org.apache.isis.core.metamodel.spec;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * @deprecated - use {@link ObjectAdapter.Util} instead
 */
@Deprecated
public final class ObjectAdapterUtils {

    private ObjectAdapterUtils() {
    }

    /**
     * @deprecated - use {@link ObjectAdapter.Util} instead
     */
    @Deprecated
    public static Object unwrapObject(final ObjectAdapter adapter) {
        return ObjectAdapter.Util.unwrap(adapter);
    }

    /**
     * @deprecated - use {@link ObjectAdapter.Util} instead
     */
    @Deprecated
    public static String unwrapObjectAsString(final ObjectAdapter adapter) {
        return ObjectAdapter.Util.unwrapAsString(adapter);
    }


    /**
     * @deprecated - use {@link ObjectAdapter.Util} instead
     */
    @Deprecated
    public static List<Object> unwrapObjects(final List<ObjectAdapter> adapters) {
        return ObjectAdapter.Util.unwrap(adapters);
    }

}
