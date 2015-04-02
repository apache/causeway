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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;

public class Title extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        final String id = request.getOptionalProperty(OBJECT);
        final String fieldName = request.getOptionalProperty(FIELD);
        final int truncateTo = Integer.valueOf(request.getOptionalProperty(TRUNCATE, "0")).intValue();
        final boolean isIconShowing = request.isRequested(SHOW_ICON, showIconByDefault());
        String className = request.getOptionalProperty(CLASS);
        className = className == null ? "title-icon" : className;
        ObjectAdapter object = MethodsUtils.findObject(request.getContext(), id);
        if (fieldName != null) {
            final ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
            if (field.isVisible(IsisContext.getAuthenticationSession(), object, Where.ANYWHERE).isVetoed()) {
                throw new ForbiddenException(field, ForbiddenException.VISIBLE);
            }
            object = field.get(object);
        }

        if (object != null) {
            request.appendHtml("<span class=\"object\">");
            IsisContext.getPersistenceSession().resolveImmediately(object);
            if (isIconShowing) {
                final String iconPath = request.getContext().imagePath(object);
                request.appendHtml("<img class=\"" + className + "\" src=\"" + iconPath + "\" />");
            }
            request.appendTruncated(object.titleString(), truncateTo);
            request.appendHtml("</span>");
        }
        request.closeEmpty();
    }

    @Override
    public String getName() {
        return "title";
    }

}
