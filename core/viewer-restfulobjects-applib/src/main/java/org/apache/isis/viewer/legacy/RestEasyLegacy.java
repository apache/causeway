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
package org.apache.isis.viewer.legacy;

import java.lang.reflect.Method;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Compatibility layer, legacy of deprecated resteasy client API.
 *
 */
public class RestEasyLegacy {

    public static String getEntityAsStringFrom(Response response) {

        final Object result = response.getEntity();

        if(result == null)
            return null;

        if(result instanceof String) {
            return (String) result;
        }

        // TODO [andi-huber] just a wild guess
        return response.readEntity(String.class);

        // legacy code ...
        // final ClientResponse<?> clientResponse = (ClientResponse<?>) response;
        // return clientResponse.getEntity(String.class);
    }

    public static void setReturnTypeToString(Response response) {

        // TODO [andi-huber] why is this needed at all?

        // legacy code ...
        // final BaseClientResponse<String> restEasyResponse = (BaseClientResponse<String>) response;
        // restEasyResponse.setReturnType(String.class);

        System.err.println("WARN RestEasyLegacy - setReturnTypeToString(Response) not implemented!");

    }

    @SuppressWarnings("unchecked")
    public static <T> T proxy(WebTarget webTarget, Class<T> clazz) {

        // legacy of
        // final org.jboss.resteasy.client.jaxrs.ResteasyWebTarget target =
        //		(org.jboss.resteasy.client.jaxrs.ResteasyWebTarget) webTarget;
        // return target.proxy(clazz);

        try {
            // [andi-huber]	resort to reflection, since we want to get rid of resteasy legacy
            // first step is to remove compile time dependencies

            final Method proxyMethod =
                    webTarget.getClass().getMethod("proxy", new Class<?>[]{Class.class});
            return (T) proxyMethod.invoke(webTarget, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new NotSupportedException("proxy not supported by this JAX-RS implementation");

    }

}
