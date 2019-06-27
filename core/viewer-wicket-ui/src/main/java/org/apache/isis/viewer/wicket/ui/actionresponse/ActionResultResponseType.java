/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.actionresponse;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
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
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            final ObjectAdapter actualAdapter = determineActualAdapter(resultAdapter); // intercepts collections
            return toEntityPage(model, actualAdapter, null);
        }

        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter targetAdapter, final ConcurrencyException ex) {
            final ActionResultResponse actionResultResponse = toEntityPage(model, targetAdapter, ex);
            return actionResultResponse;
        }
    },
    COLLECTION {
        @Override
        public ActionResultResponse interpretResult(final ActionModel actionModel, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            final EntityCollectionModel collectionModel = EntityCollectionModel.createStandalone(resultAdapter);
            // take a copy of the actionModel, because the original can get mutated (specifically: its arguments cleared)
            final ActionModel actionModelCopy = actionModel.copy();
            collectionModel.setActionHint(actionModelCopy);
            return ActionResultResponse.toPage(new StandaloneCollectionPage(collectionModel));
        }
    },
    VALUE {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            ValueModel valueModel = new ValueModel(resultAdapter);
            valueModel.setActionHint(model);
            final ValuePage valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(valuePage);
        }
    },
    VALUE_CLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_BLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_URL_AJAX {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            final URL url = (URL)resultAdapter.getPojo();
            return ActionResultResponse.openUrlInBrowser(target, url);
        }

    },
    VALUE_URL_NOAJAX {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            // open URL server-side redirect
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = ActionModel.redirectHandler(value);
            return ActionResultResponse.withHandler(handler);
        }

    },
    VOID {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ObjectAdapter resultAdapter) {
            final VoidModel voidModel = new VoidModel();
            voidModel.setActionHint(model);
            return ActionResultResponse.toPage(new VoidReturnPage(voidModel));
        }
    };

    public abstract ActionResultResponse interpretResult(ActionModel model, final AjaxRequestTarget target, ObjectAdapter resultAdapter);

    /**
     * Only overridden for {@link ActionResultResponseType#OBJECT object}
     */
    public ActionResultResponse interpretResult(ActionModel model, ObjectAdapter targetAdapter, ConcurrencyException ex) {
        throw new UnsupportedOperationException("Cannot render concurrency exception for any result type other than OBJECT");
    }

    private static ObjectAdapter determineActualAdapter(
            final ObjectAdapter resultAdapter) {

        if (resultAdapter.getSpecification().isNotCollection()) {
            return resultAdapter;
        } else {
            // will only be a single element
            final List<Object> pojoList = asList(resultAdapter);
            final Object pojo = pojoList.get(0);
            
            val pojoToAdapter = IsisContext.pojoToAdapter();
            val actualAdapter = pojoToAdapter.apply(pojo);
            
            return actualAdapter;
        }
    }

    private static ActionResultResponse toEntityPage(
    		final ActionModel model, 
    		final ObjectAdapter actualAdapter, 
    		final ConcurrencyException exIfAny) {
    	
        // this will not preserve the URL (because pageParameters are not copied over)
        // but trying to preserve them seems to cause the 302 redirect to be swallowed somehow
        final EntityPage entityPage =

                // disabling concurrency checking after the layout XML (grid) feature
                // was throwing an exception when rebuild grid after invoking action
                // not certain why that would be the case, but think it should be
                // safe to simply disable while recreating the page to re-render back to user.
                ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                        new Callable<EntityPage>() {
                            @Override public EntityPage call() throws Exception {
                                return new EntityPage(actualAdapter, exIfAny);
                            }
                        }
                        );

        return ActionResultResponse.toPage(entityPage);
    }


    // //////////////////////////////////////

    public static ActionResultResponse determineAndInterpretResult(
            final ActionModel model,
            final AjaxRequestTarget targetIfAny,
            final ObjectAdapter resultAdapter) {
        ActionResultResponseType arrt = determineFor(resultAdapter, targetIfAny);
        return arrt.interpretResult(model, targetIfAny, resultAdapter);
    }

    private static ActionResultResponseType determineFor(
            final ObjectAdapter resultAdapter,
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
    private static List<Object> asList(final ObjectAdapter resultAdapter) {
        final Collection<Object> coll = (Collection<Object>) resultAdapter.getPojo();
        return coll instanceof List
                ? (List<Object>)coll
                        : _Lists.<Object>newArrayList(coll);
    }


}