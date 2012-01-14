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
package org.apache.isis.viewer.json.applib;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.collect.Lists;

public class RepresentationWalker {

    static class Step {
        private final String key;
        private final LinkRepresentation link;
        private final JsonRepresentation body;
        private final RestfulResponse<? extends JsonRepresentation> response;
        private String error;
        private Exception exception;

        public Step(String key, LinkRepresentation link, JsonRepresentation body, RestfulResponse<? extends JsonRepresentation> response, String error, Exception exception) {
            this.key = key;
            this.link = link;
            this.body = body;
            this.response = response;
            this.error = error;
            this.exception = exception;
        }

        @Override
        public String toString() {
            return "Step [key=" + key + ", link=" + (link != null? link.getHref(): "(null)") + ", error=" + error + "]";
        }
        
    }
    
    private final RestfulClient restfulClient;
    private final List<Step> steps = Lists.newLinkedList();

    public RepresentationWalker(RestfulClient restfulClient, Response response) {
        this.restfulClient = restfulClient;
        RestfulResponse<JsonRepresentation> jsonResp = RestfulResponse.of(response);

        addStep(null, null, null, jsonResp, null, null);
    }

    private Step addStep(String key, LinkRepresentation link, JsonRepresentation body, RestfulResponse<JsonRepresentation> jsonResp, String error, Exception ex) {
        Step step = new Step(key, link, body, jsonResp, error, ex);
        steps.add(0, step);
        if(error != null) {
            if(jsonResp.getStatus().getFamily() != Family.SUCCESSFUL) {
                step.error = "response status code: " + jsonResp.getStatus();
            }
        }
        return step;
    }

    public void walk(String path) {
        walk(path, null);
    }

    public void walk(String path, JsonRepresentation invokeBody) {
        Step previousStep = currentStep();
        if(previousStep.error!=null) {
            return;
        }
        
        RestfulResponse<? extends JsonRepresentation> jsonResponse = previousStep.response;
        JsonRepresentation entity;
        try {
            entity = jsonResponse.getEntity();
        } catch (Exception e) {
            addStep(path, null, null, null, "exception: " + e.getMessage(), e);
            return;
        }
        
        LinkRepresentation link;
        try {
            link = entity.getLink(path);
        } catch (Exception e) {
            addStep(path, null, null, null, "exception: " + e.getMessage(), e);
            return;
        }
        if(link == null) {
            addStep(path, null, null, null, "no such link '" + path + "'", null);
            return;
        }
        
        final RestfulResponse<JsonRepresentation> response;
        try {
            if(invokeBody != null) {
                response = restfulClient.follow(link, invokeBody);
            } else {
                response = restfulClient.follow(link);
            }
        } catch (Exception e) {
            addStep(path, link, null, null, "failed to follow link: " + e.getMessage(), e);
            return;
        }
        
        addStep(path, link, null, response, null, null);
    }

    /**
     * The entity returned from the previous walk.
     * 
     * <p>
     * Will return null if the previous walk returned an error.
     */
    public JsonRepresentation getEntity() throws JsonParseException, JsonMappingException, IOException {
        Step currentStep = currentStep();
        if (currentStep.response == null || currentStep.error != null) {
            return null;
        }
        return currentStep.response.getEntity();
    }

    /**
     * The response returned from the previous walk.
     * 
     * <p>
     * Once a walk/performed has been attempted, is guaranteed to return a non-null value.
     * (Conversely, will return <tt>null</tt> immediately after instantiation and prior 
     * to a walk being attempted/performed).  
     */
    public RestfulResponse<?> getResponse()  {
        Step currentStep = currentStep();
        return currentStep != null? currentStep.response: null;
    }

    /**
     * The error (if any) that occurred from the previous walk.
     */
    public String getError()  {
        Step currentStep = currentStep();
        return currentStep != null? currentStep.error: null;
    }

    /**
     * The exception (if any) that occurred from the previous walk.
     * 
     * <p>
     * Will only ever be populated if {@link #getError()} is non-null.
     */
    public Exception getException()  {
        Step currentStep = currentStep();
        return currentStep != null? currentStep.exception: null;
    }

    /**
     * The step that has just been walked.
     */
    private Step currentStep() {
        return steps.get(0);
    }

}
