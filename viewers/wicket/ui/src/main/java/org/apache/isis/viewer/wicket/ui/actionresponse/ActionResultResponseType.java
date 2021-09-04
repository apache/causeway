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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.OpenUrlStrategy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
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

import lombok.SneakyThrows;
import lombok.val;

public enum ActionResultResponseType {
    OBJECT {
        @Override
        public ActionResultResponse interpretResult(final ActionModel actionModel, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            val commonContext = actionModel.getCommonContext();
            val actualAdapter = determineScalarAdapter(commonContext, resultAdapter); // intercepts collections
            return toEntityPage(actionModel, actualAdapter);
        }

        @Override
        public ActionResultResponse interpretResult(final ActionModel actionModel, final ManagedObject targetAdapter) {
            final ActionResultResponse actionResultResponse = toEntityPage(actionModel, targetAdapter);
            return actionResultResponse;
        }
    },
    COLLECTION {
        @Override
        public ActionResultResponse interpretResult(final ActionModel actionModel, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            val collectionModel = EntityCollectionModel.createStandalone(resultAdapter, actionModel);
            return ActionResultResponse.toPage(new StandaloneCollectionPage(collectionModel));
        }
    },
    VALUE {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            val commonContext = model.getCommonContext();
            ValueModel valueModel = new ValueModel(commonContext, resultAdapter);
            valueModel.setActionHint(model);
            final ValuePage valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(valuePage);
        }
    },
    VALUE_CLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = model.downloadHandler(value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_BLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler = model.downloadHandler(value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_LOCALRESPATH_AJAX {
        @Override @SneakyThrows
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            val webAppContextPath = model.getCommonContext().getWebAppContextPath();
            return ActionResultResponse
                    .openUrlInBrowser(target, localResPath.getEffectivePath(webAppContextPath::prependContextPath), localResPath.getOpenUrlStrategy());
        }
    },
    VALUE_LOCALRESPATH_NOAJAX {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            // open URL server-side redirect
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            val webAppContextPath = model.getCommonContext().getWebAppContextPath();
            IRequestHandler handler = ActionModel.redirectHandler(localResPath, localResPath.getOpenUrlStrategy(), webAppContextPath);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_URL_AJAX {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            final URL url = (URL)resultAdapter.getPojo();
            return ActionResultResponse
                    .openUrlInBrowser(target, url.toString(), OpenUrlStrategy.NEW_WINDOW); // default behavior
        }
    },
    VALUE_URL_NOAJAX {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
            // open URL server-side redirect
            final Object value = resultAdapter.getPojo();
            val webAppContextPath = model.getCommonContext().getWebAppContextPath();
            IRequestHandler handler = ActionModel.redirectHandler(value, OpenUrlStrategy.NEW_WINDOW, webAppContextPath); // default behavior
            return ActionResultResponse.withHandler(handler);
        }
    },
    VOID {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final AjaxRequestTarget target, final ManagedObject resultAdapter) {
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
    public ActionResultResponse interpretResult(final ActionModel model, final ManagedObject targetAdapter) {
        throw new UnsupportedOperationException("Cannot render concurrency exception for any result type other than OBJECT");
    }

    private static ManagedObject determineScalarAdapter(
            final IsisAppCommonContext commonContext,
            final ManagedObject resultAdapter) {

        if (resultAdapter.getSpecification().isNotCollection()) {
            return resultAdapter;
        } else {
            // will only be a single element
            final Object pojo = _NullSafe
                    .streamAutodetect(resultAdapter.getPojo())
                    .findFirst()
                    .orElseThrow(_Exceptions::noSuchElement);

            final var scalarAdapter = commonContext.getPojoToAdapter().apply(pojo);
            return scalarAdapter;
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
                if(value instanceof LocalResourcePath) {
                    return targetIfAny != null
                            ? ActionResultResponseType.VALUE_LOCALRESPATH_AJAX
                            : ActionResultResponseType.VALUE_LOCALRESPATH_NOAJAX;
                }
                if(value instanceof java.net.URL) {
                    return targetIfAny != null
                            ? ActionResultResponseType.VALUE_URL_AJAX
                            : ActionResultResponseType.VALUE_URL_NOAJAX;
                }
                // else
                return ActionResultResponseType.VALUE;
            } else {
                return ActionResultResponseType.OBJECT;
            }
        } else {

            final int cardinality = (int)_NullSafe.streamAutodetect(resultAdapter.getPojo()).count();
            switch (cardinality) {
            case 1:
                return ActionResultResponseType.OBJECT;
            default:
                return ActionResultResponseType.COLLECTION;
            }
        }
    }

}