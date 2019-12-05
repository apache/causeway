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
package org.apache.isis.viewer.restfulobjects.viewer;

import org.apache.isis.commons.internal.context._Plugin;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;

// TODO: v2: to remove.
public interface IsisJaxrsServerPlugin {

    // -- INTERFACE

    public Object newRestfulObjectsJaxbWriterForXml();

    public HttpStatusCode getFailureStatusCodeIfAny(Throwable ex);

    // -- LOOKUP

    public static IsisJaxrsServerPlugin get() {
        return _Plugin.getOrElse(IsisJaxrsServerPlugin.class,
                ambiguousPlugins->{
                    throw _Plugin.ambiguityNonRecoverable(IsisJaxrsServerPlugin.class, ambiguousPlugins);
                },
                ()->{
                    throw _Plugin.absenceNonRecoverable(IsisJaxrsServerPlugin.class);
                });
    }

}
