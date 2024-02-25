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
package org.apache.causeway.viewer.wicket.ui.actionresponse;

import java.net.URL;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycle;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.applib.value.OpenUrlStrategy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.security.authentication.logout.LogoutMenu.LoginRedirect;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.model.models.VoidModel;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.causeway.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.causeway.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.causeway.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

public enum ActionResultResponseType {
    OBJECT {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final @NonNull ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            determineScalarAdapter(actionModel.getMetaModelContext(), resultAdapter); // intercepts collections
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
                final @NonNull ManagedObject resultAdapter,
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
                final @NonNull ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final var valueModel = ValueModel.of(actionModel.getAction(), resultAdapter);
            valueModel.setActionHint(actionModel);
            final var valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(ValuePage.class, valuePage);
        }
    },
    VALUE_CLOB {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final @NonNull ManagedObject resultAdapter,
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
                final @NonNull ManagedObject resultAdapter,
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
                final @NonNull ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            return ActionResultResponse
                    .openUrlInBrowser(target, localResPath.getEffectivePath(webAppContextPath::prependContextPath), localResPath.getOpenUrlStrategy());
        }
    },
    VALUE_LOCALRESPATH_NOAJAX {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final @NonNull ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            // open URL server-side redirect
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            IRequestHandler handler = _RedirectHandler.redirectHandler(localResPath, localResPath.getOpenUrlStrategy(), webAppContextPath);
            return ActionResultResponse.withHandler(handler);
        }
    },
    VALUE_URL_AJAX {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final @NonNull ManagedObject resultAdapter,
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
                final @NonNull ManagedObject resultAdapter,
                final Can<ManagedObject> args) {
            // open URL server-side redirect
            final Object value = resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            IRequestHandler handler = _RedirectHandler.redirectHandler(value, OpenUrlStrategy.NEW_WINDOW, webAppContextPath); // default behavior
            return ActionResultResponse.withHandler(handler);
        }
    },
    /** render the 'empty page' */
    VOID_AS_EMPTY {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final @Nullable ManagedObject resultAdapter, // arg is not used
                final Can<ManagedObject> args) {
            final VoidModel voidModel = new VoidModel();
            voidModel.setActionHint(actionModel);
            return ActionResultResponse.toPage(VoidReturnPage.class, new VoidReturnPage(voidModel));
        }
    },
    /**
     * Issues a (current) page reload.
     */
    VOID_AS_RELOAD {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final @Nullable ManagedObject resultAdapter, // arg is not used
                final Can<ManagedObject> args) {
            val currentPage = PageRequestHandlerTracker.getLastHandler(RequestCycle.get()).getPage();
            val pageClass = currentPage.getClass();
            return ActionResultResponse.toPage(PageRedirectRequest.forPage(pageClass, _Casts.uncheckedCast(currentPage)));
        }
    },
    SIGN_IN {
        @Override
        public ActionResultResponse interpretResult(
                final ActionModel actionModel,
                final AjaxRequestTarget target,
                final @Nullable ManagedObject resultAdapter, // arg is not used
                final Can<ManagedObject> args) {
            val signInPage = actionModel.getMetaModelContext()
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
            final @Nullable ManagedObject resultAdapter,
            final Can<ManagedObject> args) {

        /*
         * XXX won't implement CAUSEWAY-3372 (reload on void action result)
         * because we found a counter example, where we don't want this behavior, that is:
         * @Action
         * public void delete() {
         *     repositoryService.removeAndFlush(this);
         * }
         */
        val mapAbsentResultTo = /*model.getAction().getReturnType().isVoid()
                ? ActionResultResponseType.VOID_AS_RELOAD : */
                ActionResultResponseType.VOID_AS_EMPTY;

        val typeAndAdapter = determineFor(resultAdapter, mapAbsentResultTo, targetIfAny);
        return typeAndAdapter.type // mapped to 'mapAbsentResultTo' if adapter is unspecified or null
                .interpretResult(model, targetIfAny, typeAndAdapter.resultAdapter, args);
    }

    public static ActionResultResponse toEntityPage(final @NonNull ManagedObject entityOrViewmodel) {
        return ActionResultResponse.toPage(EntityPage.class, entityOrViewmodel.refreshBookmark().orElseThrow());
    }

    // -- HELPER

    private static ManagedObject determineScalarAdapter(
            final @NonNull MetaModelContext commonContext,
            final @NonNull ManagedObject resultAdapter) {

        if (resultAdapter.getSpecification().isSingular()) {
            return resultAdapter;
        } else {
            // will only be a single element
            final Object pojo = _NullSafe
                    .streamAutodetect(resultAdapter.getPojo())
                    .findFirst()
                    .orElseThrow(_Exceptions::noSuchElement);

            final var scalarAdapter = commonContext.getObjectManager().adapt(pojo);
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
            final ActionResultResponseType mapAbsentResultTo,
            final AjaxRequestTarget targetIfAny) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(resultAdapter)) {
            // triage based on whether action return type is 'void'
            return TypeAndAdapter.of(mapAbsentResultTo, resultAdapter);
        }

        val resultSpec = resultAdapter.getSpecification();
        if (!(resultAdapter instanceof PackedManagedObject)) {

            // scalar ...

            _Assert.assertTrue(resultSpec.isSingular());

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
                val firstElement = unpacked.getFirstElseFail();
                // recursively unwrap
                return determineFor(firstElement, ActionResultResponseType.VOID_AS_EMPTY, targetIfAny);
            default:
                return TypeAndAdapter.of(ActionResultResponseType.COLLECTION, resultAdapter);
            }
        }
    }

}