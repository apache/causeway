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
package org.apache.isis.viewer.wicket.ui.actionresponse;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.model.models.VoidModel;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.isis.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.isis.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

import lombok.val;

public enum ActionResultResponseType {
    OBJECT {
        @Override
        public ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter) {
            val commonContext = model.getCommonContext();
            val actualAdapter = determineActualAdapter(commonContext, resultAdapter); // intercepts collections
            return toEntityPage(model, actualAdapter);
        }

        @Override
        public ActionResultResponse interpretResult(ActionModel model, ManagedObject targetAdapter) {
            final ActionResultResponse actionResultResponse = toEntityPage(model, targetAdapter);
            return actionResultResponse;
        }
    },
    COLLECTION {
        @Override
        public ActionResultResponse interpretResult(ActionModel actionModel, AjaxRequestTarget target, ManagedObject resultAdapter) {
            val collectionModel = EntityCollectionModel.createStandalone(resultAdapter, actionModel);
            // take a copy of the actionModel, because the original can get mutated (specifically: its arguments cleared)
            val actionModelCopy = actionModel.copy();
            collectionModel.setActionHint(actionModelCopy);
            return ActionResultResponse.toPage(new StandaloneCollectionPage(collectionModel));
        }
    },
    VALUE {
        @Override
        public ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter) {
            val commonContext = model.getCommonContext();
            ValueModel valueModel = new ValueModel(commonContext, resultAdapter);
            valueModel.setActionHint(model);
            final ValuePage valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(valuePage);
        }
    },
    VALUE_CLOB {
        @Override
        public ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_BLOB {
        @Override
        public ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_URL_AJAX {
        @Override
        public ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter) {
            final URL url = (URL)resultAdapter.getPojo();
            return ActionResultResponse.openUrlInBrowser(target, url);
        }

    },
    VALUE_URL_NOAJAX {
        @Override
        public ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter) {
            // open URL server-side redirect
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = ActionModel.redirectHandler(value);
            return ActionResultResponse.withHandler(handler);
        }

    },
    VOID {
        @Override
        public ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter) {
            val commonContext = model.getCommonContext();
            final VoidModel voidModel = new VoidModel(commonContext);
            voidModel.setActionHint(model);
            return ActionResultResponse.toPage(new VoidReturnPage(voidModel));
        }
    };

    public abstract ActionResultResponse interpretResult(ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter);

    /**
     * Only overridden for {@link ActionResultResponseType#OBJECT object}
     */
    public ActionResultResponse interpretResult(ActionModel model, ManagedObject targetAdapter) {
        throw new UnsupportedOperationException("Cannot render concurrency exception for any result type other than OBJECT");
    }

    private static ManagedObject determineActualAdapter(
            IsisAppCommonContext commonContext, 
            ManagedObject resultAdapter) {

        if (resultAdapter.getSpecification().isNotCollection()) {
            return resultAdapter;
        } else {
            // will only be a single element
            final List<Object> pojoList = asList(resultAdapter);
            final Object pojo = pojoList.get(0);

            //XXX lombok issue, cannot use val here
            final ManagedObject actualAdapter = commonContext.getPojoToAdapter().apply(pojo);

            return actualAdapter;
        }
    }

    private static ActionResultResponse toEntityPage(
            final ActionModel model, 
            final ManagedObject actualAdapter) {

        // this will not preserve the URL (because pageParameters are not copied over)
        // but trying to preserve them seems to cause the 302 redirect to be swallowed somehow
        final EntityPage entityPage = new EntityPage(model.getCommonContext(), actualAdapter);

        return ActionResultResponse.toPage(entityPage);
    }


    // //////////////////////////////////////

    public static ActionResultResponse determineAndInterpretResult(
            final ActionModel model,
            final AjaxRequestTarget targetIfAny,
            final ManagedObject resultAdapter) {
        
        ActionResultResponseType arrt = determineFor(resultAdapter, targetIfAny);
        return arrt.interpretResult(model, targetIfAny, resultAdapter);
    }

    private static ActionResultResponseType determineFor(
            final ManagedObject resultAdapter,
            final AjaxRequestTarget targetIfAny) {
        
        if(resultAdapter == null) {
            return ActionResultResponseType.VOID;
        }
        final ObjectSpecification resultSpec = resultAdapter.getSpecification();
        if (resultSpec.isNotCollection()) {
            if (resultSpec.getFacet(ValueFacet.class) != null) {

                final Object value = resultAdapter.getPojo();
                if(value instanceof Clob) {
                    return ActionResultResponseType.VALUE_CLOB;
                }
                if(value instanceof Blob) {
                    return ActionResultResponseType.VALUE_BLOB;
                }
                if(value instanceof java.net.URL) {
                    return targetIfAny != null? ActionResultResponseType.VALUE_URL_AJAX: ActionResultResponseType.VALUE_URL_NOAJAX;
                }
                // else
                return ActionResultResponseType.VALUE;
            } else {
                return ActionResultResponseType.OBJECT;
            }
        } else {
            final List<Object> pojoList = asList(resultAdapter);
            switch (pojoList.size()) {
            case 1:
                return ActionResultResponseType.OBJECT;
            default:
                return ActionResultResponseType.COLLECTION;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(final ManagedObject resultAdapter) {
        final Collection<Object> coll = (Collection<Object>) resultAdapter.getPojo();
        return coll instanceof List
                ? (List<Object>)coll
                        : _Lists.<Object>newArrayList(coll);
    }


}