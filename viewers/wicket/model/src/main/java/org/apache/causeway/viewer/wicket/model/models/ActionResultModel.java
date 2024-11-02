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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.security.authentication.logout.LogoutMenu.LoginRedirect;
import org.apache.causeway.viewer.wicket.model.models.FormExecutor.ActionResultResponseType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@AllArgsConstructor @Getter @Accessors(fluent=true) //java record candidate
public class ActionResultModel {
    private final ActionResultResponseType responseType;
    private final ManagedObject resultAdapter;

    public static ActionResultModel determineFor(
            @NonNull final ActionModel actionModel,
            final ManagedObject resultAdapter,
            final AjaxRequestTarget targetIfAny) {

        if(actionModel.getColumnActionModifier().isForceStayOnPage()) {
            return new ActionResultModel(ActionResultResponseType.RELOAD, resultAdapter);
        }

        /*
         * won't implement CAUSEWAY-3372 (reload on void action result)
         * because we found a counter example, where we don't want this behavior, that is:
         * @Action
         * public void delete() {
         *     repositoryService.removeAndFlush(this);
         * }
         */
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            // triage based on whether action return type is 'void'
            return new ActionResultModel(ActionResultResponseType.VOID_AS_EMPTY, resultAdapter);
        }

        var resultSpec = resultAdapter.getSpecification();
        if (!(resultAdapter instanceof PackedManagedObject)) {

            // scalar ...

            _Assert.assertTrue(resultSpec.isSingular());

            if(LoginRedirect.LOGICAL_TYPE_NAME.equals(resultSpec.getLogicalTypeName())) {
                return new ActionResultModel(ActionResultResponseType.SIGN_IN, resultAdapter);
            }

            if (resultSpec.isValue()) {

                final Object value = resultAdapter.getPojo();
                if(value instanceof Clob) {
                    return new ActionResultModel(ActionResultResponseType.VALUE_CLOB, resultAdapter);
                }
                if(value instanceof Blob) {
                    return new ActionResultModel(ActionResultResponseType.VALUE_BLOB, resultAdapter);
                }
                if(value instanceof LocalResourcePath) {
                    return targetIfAny != null
                            ? new ActionResultModel(ActionResultResponseType.VALUE_LOCALRESPATH_AJAX, resultAdapter)
                            : new ActionResultModel(ActionResultResponseType.VALUE_LOCALRESPATH_NOAJAX, resultAdapter);
                }
                if(value instanceof java.net.URL) {
                    return targetIfAny != null
                            ? new ActionResultModel(ActionResultResponseType.VALUE_URL_AJAX, resultAdapter)
                            : new ActionResultModel(ActionResultResponseType.VALUE_URL_NOAJAX, resultAdapter);
                }
                // else
                return new ActionResultModel(ActionResultResponseType.VALUE, resultAdapter);
            } else {
                return new ActionResultModel(ActionResultResponseType.OBJECT, resultAdapter);
            }
        } else {
            // non-scalar ...

            var packedAdapter = (PackedManagedObject) resultAdapter;
            var unpacked = packedAdapter.unpack();

            final int cardinality = unpacked.size();
            switch (cardinality) {
            case 1:
                var firstElement = unpacked.getFirstElseFail();
                // recursively unwrap
                return determineFor(actionModel, firstElement, targetIfAny);
            default:
                return new ActionResultModel(ActionResultResponseType.COLLECTION, resultAdapter);
            }
        }
    }

}
