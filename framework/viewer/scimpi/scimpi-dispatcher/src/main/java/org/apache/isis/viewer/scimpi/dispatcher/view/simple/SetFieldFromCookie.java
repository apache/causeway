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

package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class SetFieldFromCookie extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        throw new NotYetImplementedException("3.1");
        /*
         * String name = request.getRequiredProperty(NAME); String cookieString
         * = request.getContext().getCookie(name); ObjectAdapter valueAdapter =
         * IsisContext.getObjectPersistor().createAdapterForValue(cookieString);
         * 
         * String objectId = request.getOptionalProperty(OBJECT); String
         * fieldName = request.getRequiredProperty(FIELD); ObjectAdapter object
         * = (ObjectAdapter)
         * request.getContext().getMappedObjectOrResult(objectId);
         * ObjectAssociation field =
         * object.getSpecification().getField(fieldName); if (field.isValue()) {
         * throw new ScimpiException("Can only set up a value field"); }
         * 
         * ((ValueAssociation) field).setValue(object, valueAdapter);
         */
    }

    @Override
    public String getName() {
        return "set-field-from-cookie";
    }
}
