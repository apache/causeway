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
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.processor.Request;
import org.apache.isis.webapp.util.MethodsUtils;


// TODO do the same for description and help, and for fields
public class ActionName extends AbstractElementProcessor {

    public void process(Request request) {
        String objectId = request.getOptionalProperty(OBJECT);
        String methodName = request.getRequiredProperty(METHOD);
        
        ObjectAdapter object = MethodsUtils.findObject(request.getContext(), objectId);
        ObjectAction action = MethodsUtils.findAction(object, methodName);

        request.appendHtml(action.getName());
    }
    
    public String getName() {
        return "action-name";
    }

}


