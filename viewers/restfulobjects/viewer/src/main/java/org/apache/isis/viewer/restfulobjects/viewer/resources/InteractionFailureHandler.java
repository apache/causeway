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

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.spec.interaction.InteractionVeto;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

public class InteractionFailureHandler {

    public static RestfulObjectsApplicationException onFailure(@Nullable final InteractionVeto failure) {
        
        if(failure==null) {
            return RestfulObjectsApplicationException
                    .createWithMessage(RestfulResponse.HttpStatusCode.INTERNAL_SERVER_ERROR,
                            "unexpected empty failure holder");
        }
        
        switch(failure.getVetoType()) {
        case NOT_FOUND:
        case HIDDEN:
            return RestfulObjectsApplicationException
            .createWithMessage(RestfulResponse.HttpStatusCode.NOT_FOUND,
                    failure.getReason());
        case READONLY:
        case INVALID:
            return RestfulObjectsApplicationException
            .createWithMessage(RestfulResponse.HttpStatusCode.FORBIDDEN,
                    failure.getReason());
        }
        
        return RestfulObjectsApplicationException
                .createWithMessage(RestfulResponse.HttpStatusCode.INTERNAL_SERVER_ERROR,
                        "unmatched veto type " + failure.getVetoType());
        
    }
    
    
//    public static void onFailure(@Nullable final InteractionVeto failure) {
//        
//        if(failure==null) {
//            return;
//        }
//        
//        switch(failure.getVetoType()) {
//        case NOT_FOUND:
//        case HIDDEN:
//            throw RestfulObjectsApplicationException
//            .createWithMessage(RestfulResponse.HttpStatusCode.NOT_FOUND,
//                    failure.getReason());
//        case READONLY:
//        case INVALID:
//            throw RestfulObjectsApplicationException
//            .createWithMessage(RestfulResponse.HttpStatusCode.FORBIDDEN,
//                    failure.getReason());
//        }
//        
//    }
        
        

    
}
