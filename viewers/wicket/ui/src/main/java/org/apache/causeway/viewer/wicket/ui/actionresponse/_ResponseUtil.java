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

import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.applib.value.OpenUrlStrategy;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ActionResultModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.causeway.viewer.wicket.model.models.FormExecutor.ActionResultResponseType;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.model.models.VoidModel;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.causeway.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.causeway.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
class _ResponseUtil {

    ActionResultResponse determineAndInterpretResult(
            @NonNull final ActionModel actionModel,
            @Nullable final AjaxRequestTarget ajaxTarget,
            @Nullable final ManagedObject resultAdapterIfAny) {

        var typeAndAdapter = ActionResultModel.determineFor(resultAdapterIfAny, ajaxTarget);
        final ActionResultResponseType responseType = typeAndAdapter.responseType();
        final ManagedObject resultAdapter = typeAndAdapter.resultAdapter();

        switch(responseType) {
        case COLLECTION: {
            _Assert.assertTrue(resultAdapter instanceof PackedManagedObject);

            final var collectionModel = EntityCollectionModelStandalone
                    .forActionModel((PackedManagedObject)resultAdapter, actionModel);
            return ActionResultResponse.toPage(
                    StandaloneCollectionPage.class, new StandaloneCollectionPage(collectionModel));
        }
        case OBJECT: {
            determineScalarAdapter(actionModel.getMetaModelContext(), resultAdapter); // intercepts collections
            return ActionResultResponse.toEntityPage(resultAdapter);
        }
        case SIGN_IN: {
            var signInPage = actionModel.getMetaModelContext()
                    .lookupServiceElseFail(PageClassRegistry.class)
                    .getPageClass(PageType.SIGN_IN);
            return ActionResultResponse.toPage(PageRedirectRequest.forPageClass(signInPage));
        }
        case VALUE: {
            final var valueModel = ValueModel.of(actionModel.getAction(), resultAdapter);
            valueModel.setActionHint(actionModel);
            final var valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(ValuePage.class, valuePage);
        }
        case VALUE_BLOB: {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler =
                    _DownloadHandler.downloadHandler(actionModel.getAction(), value);
            return ActionResultResponse.withHandler(handler);
        }
        case VALUE_CLOB: {
            final Object value = resultAdapter.getPojo();
            IRequestHandler handler =
                    _DownloadHandler.downloadHandler(actionModel.getAction(), value);
            return ActionResultResponse.withHandler(handler);
        }
        case VALUE_LOCALRESPATH_AJAX: {
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            return ActionResultResponse
                    .openUrlInBrowser(ajaxTarget, localResPath.getEffectivePath(webAppContextPath::prependContextPath), localResPath.getOpenUrlStrategy());
        }
        case VALUE_LOCALRESPATH_NOAJAX: {
            // open URL server-side redirect
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            IRequestHandler handler = _RedirectHandler.redirectHandler(localResPath, localResPath.getOpenUrlStrategy(), webAppContextPath);
            return ActionResultResponse.withHandler(handler);
        }
        case VALUE_URL_AJAX: {
            final URL url = (URL)resultAdapter.getPojo();
            return ActionResultResponse
                    .openUrlInBrowser(ajaxTarget, url.toString(), OpenUrlStrategy.NEW_WINDOW); // default behavior
        }
        case VALUE_URL_NOAJAX: {
            // open URL server-side redirect
            final Object value = resultAdapter.getPojo();
            final var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            IRequestHandler handler = _RedirectHandler.redirectHandler(value, OpenUrlStrategy.NEW_WINDOW, webAppContextPath); // default behavior
            return ActionResultResponse.withHandler(handler);
        }
        case VOID_AS_EMPTY: {
            final VoidModel voidModel = new VoidModel();
            voidModel.setActionHint(actionModel);
            return ActionResultResponse.toPage(VoidReturnPage.class, new VoidReturnPage(voidModel));
        }
        case VOID_AS_RELOAD: {
            var currentPage = PageRequestHandlerTracker.getLastHandler(RequestCycle.get()).getPage();
            var pageClass = currentPage.getClass();
            return ActionResultResponse.toPage(PageRedirectRequest.forPage(pageClass, _Casts.uncheckedCast(currentPage)));
        }
        default:
            throw _Exceptions.unmatchedCase(responseType);
        }
    }

    // -- HELPER

    private ManagedObject determineScalarAdapter(
            final @NonNull MetaModelContext mmc,
            final @NonNull ManagedObject resultAdapter) {

        if (resultAdapter.getSpecification().isSingular()) {
            return resultAdapter;
        } else {
            // will only be a single element
            final Object pojo = _NullSafe
                    .streamAutodetect(resultAdapter.getPojo())
                    .findFirst()
                    .orElseThrow(_Exceptions::noSuchElement);

            final var scalarAdapter = mmc.getObjectManager().adapt(pojo);
            return scalarAdapter;
        }
    }

}
