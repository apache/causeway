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


package org.apache.isis.webapp.view.value;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.ForbiddenException;
import org.apache.isis.webapp.processor.Request;

/**
 * 
 */
public class TitleString extends AbstractElementProcessor {

    public void process(Request request) {
        String id = request.getOptionalProperty(OBJECT);
        String fieldName = request.getOptionalProperty(FIELD);
        ObjectAdapter object = request.getContext().getMappedObjectOrResult(id);
        if (fieldName == null) {
            request.appendHtml(object.titleString());
        } else {
            ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) {
                throw new ForbiddenException("Field " + fieldName + " in " + object + " is not visible");
            }
            ObjectAdapter fieldReference = field.get(object);
            if (fieldReference != null) {
                request.appendHtml(fieldReference.titleString());
            }
        }
    }
    
    public String getName() {
        return "title-string";
    }

}

