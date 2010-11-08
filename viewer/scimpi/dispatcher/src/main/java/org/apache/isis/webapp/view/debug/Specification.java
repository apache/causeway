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


package org.apache.isis.webapp.view.debug;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.processor.Request;


public class Specification extends AbstractElementProcessor {

    public void process(Request request) {
        if (request.isRequested("always") || request.getContext().getDebug() == RequestContext.Debug.ON) {
            request.appendHtml("<div class=\"debug\">");
            request.appendHtml("<pre>");
            
            String id = request.getOptionalProperty("object");
            ObjectAdapter object = request.getContext().getMappedObjectOrResult(id);
            ObjectSpecification specification = object.getSpecification();
            request.appendHtml(specification.getSingularName() + " (" + specification.getFullName() + ") \n");
            ObjectAssociation[] fields = specification.getAssociations();
            for (int i = 0; i < fields.length; i++) {
                request.appendHtml("    " + fields[i].getName() + " (" + fields[i].getSpecification().getSingularName() + ") \n" );
                
            }
            
            request.appendHtml("</pre>");
            request.appendHtml("</div>");
        }
    }

    public String getName() {
        return "specification";
    }

}

