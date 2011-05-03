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
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

/**
 * <swf:selected name="selected" object="${action}" equals="${subaction}" />
 */
public class SelectedObject extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        final String name = request.getOptionalProperty(NAME, "selected");
        final String objectId = request.getRequiredProperty(OBJECT);
        final String equalsId = request.getOptionalProperty("equals");
        final String title = request.getOptionalProperty(BUTTON_TITLE);

        final ObjectAdapter object = request.getContext().getMappedObjectOrResult(objectId);
        final ObjectAdapter other = request.getContext().getMappedObjectOrResult(equalsId);
        if (object == other || object.equals(title)) {
            // TODO title is not being used!
            request.getContext().addVariable(ID, " id=\"" + name + "\" ", Scope.INTERACTION);
        } else {
            request.getContext().addVariable(ID, "", Scope.INTERACTION);
        }
        request.closeEmpty();
    }

    @Override
    public String getName() {
        return "selected";
    }

}
