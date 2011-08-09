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

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.util.HttpStatusCode.Range;
import org.apache.isis.viewer.json.applib.util.JsonResponse;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.collect.Lists;

public class RepresentationWalker {

    static class Step {
        private final String key;
        private final Link link;
        private final JsonRepresentation body;
        private final JsonResponse<? extends JsonRepresentation> response;
        private String error;
        private Exception exception;

        public Step(String key, Link link, JsonRepresentation body, JsonResponse<? extends JsonRepresentation> response, String error, Exception exception) {
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
        JsonResponse<JsonRepresentation> jsonResp = JsonResponse.of(response, JsonRepresentation.class);

        addStep(null, null, null, jsonResp, null, null);
    }

    private Step addStep(String key, Link link, JsonRepresentation body, JsonResponse<JsonRepresentation> jsonResp, String error, Exception ex) {
        Step step = new Step(key, link, body, jsonResp, error, ex);
        steps.add(0, step);
        if(error != null) {
            if(jsonResp.getStatus().getRange() != Range.SUCCESS) {
                step.error = "response status code: " + jsonResp.getStatus();
            }
        }
        return step;
    }

    public void walk(String key) {
        Step previousStep = previousStep();
        if(previousStep.error!=null) {
            return;
        }
        
        JsonResponse<? extends JsonRepresentation> jsonResponse = previousStep.response;
        JsonRepresentation entity;
        try {
            entity = jsonResponse.getEntity();
        } catch (Exception e) {
            addStep(key, null, null, null, "exception: " + e.getMessage(), e);
            return;
        }
        
        Link link;
        try {
            link = entity.getLink(key);
        } catch (Exception e) {
            addStep(key, null, null, null, "exception: " + e.getMessage(), e);
            return;
        }
        if(link == null) {
            addStep(key, null, null, null, "no such link '" + key + "'", null);
            return;
        }
        
        Response response;
        try {
            response = restfulClient.follow(link);
        } catch (Exception e) {
            addStep(key, link, null, null, "failed to follow link: " + e.getMessage(), e);
            return;
        }
        
        addStep(key, link, null, JsonResponse.of(response, JsonRepresentation.class), null, null);
    }

    public void walkXpath(String linkXpath) {
        JsonRepresentation entity;
        try {
            entity = getEntity();
        } catch (Exception e) {
            Step previousStep = previousStep();
            previousStep.error = "exception: " + e.getMessage();
            previousStep.exception = e;
            return;
        }

        if(entity == null) {
            return;
        }
        
        JsonRepresentation matching;
        try {
            matching = entity.xpath(linkXpath);
            
        } catch (Exception e) {
            addStep(linkXpath, null, null, null, "exception: " + e.getMessage(), e);
            return;
        }

        if (matching == null) {
            addStep(linkXpath, null, null, null, "no such link '" + linkXpath + "'", null);
            return;
        }

        Link link = matching.asLink();
        if(link.getHref() == null) {
            addStep(linkXpath, link, null, null, "key does not identify a link '" + linkXpath + "'", null);
            return;
        }
        
        Response response;
        try {
            response = restfulClient.follow(link);
        } catch (Exception e) {
            addStep(linkXpath, link, null, null, "failed to follow link: " + e.getMessage(), e);
            return;
        }
        
        addStep(linkXpath, link, null, JsonResponse.of(response, JsonRepresentation.class), null, null);
    }

    public JsonRepresentation getEntity() throws JsonParseException, JsonMappingException, IOException {
        Step previousStep = previousStep();
        if (previousStep.response == null || previousStep.error != null) {
            return null;
        }
        return previousStep.response.getEntity();
    }

    private Step previousStep() {
        return steps.get(0);
    }


}
