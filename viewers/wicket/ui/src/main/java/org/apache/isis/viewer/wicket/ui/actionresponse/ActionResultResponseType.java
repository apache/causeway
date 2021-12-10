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
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.security.authentication.logout.LogoutMenu.LoginRedirect;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.model.models.VoidModel;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.isis.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.isis.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

public enum ActionResultResponseType {
    OBJECT {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            determineScalarAdapter(actionModel.getCommonContext(), resultAdapter); // intercepts collections
            return toEntityPage(resultAdapter);
        }

        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final ManagedObject targetAdapter) {
            return toEntityPage(targetAdapter);
        }
    },
    COLLECTION {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            _Assert.assertTrue(resultAdapter instanceof PackedManagedObject);

            final var collectionModel = EntityCollectionModelStandalone
                    .forActionModel((PackedManagedObject)resultAdapter, actionModel, args);
            return ActionResultResponse.toPage(
                    StandaloneCollectionPage.class, new StandaloneCollectionPage(collectionModel));
        }
    },
    /**
     * Renders the value-type in its own <i>Standalone Value Page</i>.
     */
    VALUE {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final var commonContext = actionModel.getCommonContext();
            ValueModel valueModel = ValueModel.of(commonContext, resultAdapter);
            valueModel.setActionHint(actionModel);
            final ValuePage valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(ValuePage.class, valuePage);
        }
    },
    VALUE_CLOB {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler =
                    _DownloadHandler.downloadHandler(actionModel.getAction(), value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_BLOB {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler =
                    _DownloadHandler.downloadHandler(actionModel.getAction(), value);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_LOCALRESPATH_AJAX {
        @Override @SneakyThrows
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getCommonContext().getWebAppContextPath();
            return ActionResultResponse
                    .openUrlInBrowser(target, localResPath.getEffectivePath(webAppContextPath::prependContextPath), localResPath.getOpenUrlStrategy());
        }
    },
    VALUE_LOCALRESPATH_NOAJAX {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            // open URL server-side redirect
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getCommonContext().getWebAppContextPath();
            IRequestHandler handler = _RedirectHandler.redirectHandler(localResPath, localResPath.getOpenUrlStrategy(), webAppContextPath);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_URL_AJAX {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final URL url = (URL)resultAdapter.getPojo();
            return ActionResultResponse
                    .openUrlInBrowser(target, url.toString(), OpenUrlStrategy.NEW_WINDOW); // default behavior
        }
    },
    VALUE_URL_NOAJAX {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            // open URL server-side redirect
            final Object value = resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getCommonContext().getWebAppContextPath();
            IRequestHandler handler = _RedirectHandler.redirectHandler(value, OpenUrlStrategy.NEW_WINDOW, webAppContextPath); // default behavior
            return ActionResultResponse.withHandler(handler);
        }
    },
    VOID {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final var commonContext = actionModel.getCommonContext();
            final VoidModel voidModel = new VoidModel(commonContext);
            voidModel.setActionHint(actionModel);
            return ActionResultResponse.toPage(VoidReturnPage.class, new VoidReturnPage(voidModel));
        }
    },
    SIGN_IN {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            val signInPage = actionModel.getCommonContext()
                    .lookupServiceElseFail(PageClassRegistry.class)
                    .getPageClass(PageType.SIGN_IN);

            return ActionResultResponse.toPage(PageRedirectRequest.forPageClass(signInPage));
        }
    };

    public abstract ActionResultResponse interpretResult(
            ActionModel model, AjaxRequestTarget target, ManagedObject resultAdapter, Can<ManagedObject> args);

    /**
     * Only overridden for {@link ActionResultResponseType#OBJECT object}
     */
    public ActionResultResponse interpretResult(final ActionModel model, final ManagedObject targetAdapter) {
        throw new UnsupportedOperationException("Cannot render concurrency exception for any result type other than OBJECT");
    }

    // -- UTILITY

    public static ActionResultResponse determineAndInterpretResult(
            final ActionModel model,
            final AjaxRequestTarget targetIfAny,
            final ManagedObject resultAdapter,
            final Can<ManagedObject> args) {

        val typeAndAdapter = determineFor(resultAdapter, targetIfAny);
        return typeAndAdapter.type
                .interpretResult(model, targetIfAny, typeAndAdapter.resultAdapter, args);
    }

    public static ActionResultResponse toEntityPage(final ManagedObject entityOrViewmodel) {
        return ActionResultResponse.toPage(EntityPage.class, entityOrViewmodel.getBookmarkRefreshed().orElseThrow());
    }

    // -- HELPER

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

    @Value(staticConstructor = "of")
    private static class TypeAndAdapter {
        final ActionResultResponseType type;
        final ManagedObject resultAdapter;
    }

    private static TypeAndAdapter determineFor(
            final ManagedObject resultAdapter,
            final AjaxRequestTarget targetIfAny) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            return TypeAndAdapter.of(ActionResultResponseType.VOID, resultAdapter);
        }

        val resultSpec = resultAdapter.getSpecification();
        if (!(resultAdapter instanceof PackedManagedObject)) {

            // scalar ...

            _Assert.assertTrue(resultSpec.isNotCollection());

            if(LoginRedirect.LOGICAL_TYPE_NAME.equals(resultSpec.getLogicalTypeName())) {
                return TypeAndAdapter.of(ActionResultResponseType.SIGN_IN, resultAdapter);
            }

            if (resultSpec.isValue()) {

                final Object value = resultAdapter.getPojo();
                if(value instanceof Clob) {
                    return TypeAndAdapter.of(ActionResultResponseType.VALUE_CLOB, resultAdapter);
                }
                if(value instanceof Blob) {
                    return TypeAndAdapter.of(ActionResultResponseType.VALUE_BLOB, resultAdapter);
                }
                if(value instanceof LocalResourcePath) {
                    return targetIfAny != null
                            ? TypeAndAdapter.of(ActionResultResponseType.VALUE_LOCALRESPATH_AJAX, resultAdapter)
                            : TypeAndAdapter.of(ActionResultResponseType.VALUE_LOCALRESPATH_NOAJAX, resultAdapter);
                }
                if(value instanceof java.net.URL) {
                    return targetIfAny != null
                            ? TypeAndAdapter.of(ActionResultResponseType.VALUE_URL_AJAX, resultAdapter)
                            : TypeAndAdapter.of(ActionResultResponseType.VALUE_URL_NOAJAX, resultAdapter);
                }
                // else
                return TypeAndAdapter.of(ActionResultResponseType.VALUE, resultAdapter);
            } else {
                return TypeAndAdapter.of(ActionResultResponseType.OBJECT, resultAdapter);
            }
        } else {
            // non-scalar ...

            val packedAdapter = (PackedManagedObject) resultAdapter;
            val unpacked = packedAdapter.unpack();

            final int cardinality = unpacked.size();
            switch (cardinality) {
            case 1:
                val firstElement = unpacked.getFirstOrFail();
                // recursively unwrap
                return determineFor(firstElement, targetIfAny);
            default:
                return TypeAndAdapter.of(ActionResultResponseType.COLLECTION, resultAdapter);
            }
        }
    }

}