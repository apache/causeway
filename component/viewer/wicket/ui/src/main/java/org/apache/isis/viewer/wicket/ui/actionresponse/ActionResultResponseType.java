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

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.request.IRequestHandler;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.model.models.VoidModel;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.isis.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.isis.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

public enum ActionResultResponseType {
    OBJECT {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final ObjectAdapter actualAdapter = determineActualAdapter(resultAdapter);
            return toEntityPage(model, actualAdapter, null);
        }

        @Override
        public ActionResultResponse interpretResult(final ActionModel model, ObjectAdapter targetAdapter, ConcurrencyException ex) {
            return toEntityPage(model, targetAdapter, ex);
        }

        private ObjectAdapter determineActualAdapter(final ObjectAdapter resultAdapter) {
            if (resultAdapter.getSpecification().isNotCollection()) {
                return resultAdapter;
            } else {
                // will only be a single element
                final List<Object> pojoList = asList(resultAdapter);
                final Object pojo = pojoList.get(0);
                return adapterFor(pojo);
            }
        }
        private ObjectAdapter adapterFor(final Object pojo) {
            return IsisContext.getPersistenceSession().getAdapterManager().adapterFor(pojo);
        }

        private ActionResultResponse toEntityPage(final ActionModel model, final ObjectAdapter actualAdapter, ConcurrencyException exIfAny) {
            // this will not preserve the URL (because pageParameters are not copied over)
            // but trying to preserve them seems to cause the 302 redirect to be swallowed somehow
            final EntityPage entityPage = new EntityPage(actualAdapter, exIfAny);
            return ActionResultResponse.toPage(this, entityPage);
        }

    },
    COLLECTION {
        @Override
        public ActionResultResponse interpretResult(final ActionModel actionModel, final ObjectAdapter resultAdapter) {
            final EntityCollectionModel collectionModel = EntityCollectionModel.createStandalone(resultAdapter);
            collectionModel.setActionHint(actionModel);
            return ActionResultResponse.toPage(this, new StandaloneCollectionPage(collectionModel));
        }
    },
    VALUE {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            ValueModel valueModel = new ValueModel(resultAdapter);
            final ValuePage valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(this, valuePage);
        }
    },
    VALUE_CLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getObject();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(this, handler);
        }
    },
    VALUE_BLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getObject();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(this, handler);
        }

    },
    VALUE_URL {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getObject();
            IRequestHandler handler = ActionModel.redirectHandler(value);
            return ActionResultResponse.withHandler(this, handler);
        }

    },
    VOID {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final VoidModel voidModel = new VoidModel();
            voidModel.setActionHint(model);
            return ActionResultResponse.toPage(this, new VoidReturnPage(voidModel));
        }
    };

    public abstract ActionResultResponse interpretResult(ActionModel model, ObjectAdapter resultAdapter);

    /**
     * Only overridden for {@link ActionResultResponseType#OBJECT object}
     */
    public ActionResultResponse interpretResult(ActionModel model, ObjectAdapter targetAdapter, ConcurrencyException ex) {
        throw new UnsupportedOperationException("Cannot render concurrency exception for any result type other than OBJECT");
    }

    // //////////////////////////////////////

    public static ActionResultResponse determineAndInterpretResult(final ActionModel model, ObjectAdapter resultAdapter) {
        ActionResultResponseType arrt = determineFor(resultAdapter);
        return arrt.interpretResult(model, resultAdapter);
    }

    private static ActionResultResponseType determineFor(final ObjectAdapter resultAdapter) {
        if(resultAdapter == null) {
            return ActionResultResponseType.VOID;
        }
        final ObjectSpecification resultSpec = resultAdapter.getSpecification();
        if (resultSpec.isNotCollection()) {
            if (resultSpec.getFacet(ValueFacet.class) != null) {
                
                final Object value = resultAdapter.getObject();
                if(value instanceof Clob) {
                    return ActionResultResponseType.VALUE_CLOB;
                } 
                if(value instanceof Blob) {
                    return ActionResultResponseType.VALUE_BLOB;
                } 
                if(value instanceof java.net.URL) {
                    return ActionResultResponseType.VALUE_URL;
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
        final Collection<Object> coll = (Collection<Object>) resultAdapter.getObject();
        return coll instanceof List
                ? (List<Object>)coll
                : Lists.<Object>newArrayList(coll);
    }


}