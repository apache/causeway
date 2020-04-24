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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

public class InteractionFailureHandler {

    public static void onFailure(final InteractionResponse failure) {
        
        if(failure==null || failure.isSuccess()) {
            return;
        }
        
        switch(failure.getVeto()) {
        case NOT_FOUND:
        case HIDDEN:
            throw RestfulObjectsApplicationException
            .createWithMessage(RestfulResponse.HttpStatusCode.NOT_FOUND,
                    failure.getFailureMessage());
        case UNAUTHORIZED:
        case FORBIDDEN:
            throw RestfulObjectsApplicationException
            .createWithMessage(RestfulResponse.HttpStatusCode.FORBIDDEN,
                    failure.getFailureMessage());
        }
    }
    
}
