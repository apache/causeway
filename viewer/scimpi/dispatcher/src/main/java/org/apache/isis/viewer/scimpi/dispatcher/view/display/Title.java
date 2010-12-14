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


package org.apache.isis.viewer.scimpi.dispatcher.view.display;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;


public class Title extends AbstractElementProcessor {

    public void process(Request request) {
        String id = request.getOptionalProperty(OBJECT);
        String fieldName = request.getOptionalProperty(FIELD);
        boolean isIconShowing = request.isRequested(SHOW_ICON, true);
        String className = request.getOptionalProperty(CLASS);
        className = className == null ? "title-icon" : className;
        ObjectAdapter object = MethodsUtils.findObject(request.getContext(), id);
        if (fieldName != null) {
            ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) {
                throw new ForbiddenException("Field " + fieldName + " in " + object + " is not visible");
            }
            object = field.get(object);
        }
        
        if (object != null) {
            request.appendHtml("<span class=\"object\">");
            IsisContext.getPersistenceSession().resolveImmediately(object);
            if (isIconShowing) {
                String iconPath = request.getContext().imagePath(object);
                request.appendHtml("<img class=\"" + className + "\" src=\"" + iconPath + "\" />");
            }
            request.appendHtml(object.titleString());
            request.appendHtml("</span>");
        }
        request.closeEmpty();
    }
    
    public String getName() {
        return "title";
    }

}

