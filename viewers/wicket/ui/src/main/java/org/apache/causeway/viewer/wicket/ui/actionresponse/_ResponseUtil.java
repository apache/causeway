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

import org.jspecify.annotations.Nullable;

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
import org.apache.causeway.viewer.wicket.model.models.FormExecutor.ActionResultResponseType;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModelStandalone;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.model.models.VoidModel;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.causeway.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.causeway.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

import org.jspecify.annotations.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
class _ResponseUtil {

    ActionResultResponse determineAndInterpretResult(
            final @NonNull ActionModel actionModel,
            final @Nullable AjaxRequestTarget ajaxTarget,
            final @Nullable ManagedObject resultAdapterIfAny) {

        var actionResultModel = ActionResultModel.determineFor(actionModel, resultAdapterIfAny, ajaxTarget);
        var response = actionResultResponse(actionModel, ajaxTarget, actionResultModel);

        //TODO[causeway-viewer-wicket-ui-3815] handling only if we have a page redirect, other cases ignored (eg. download action)
        return response.pageRedirect()!=null
            ? switch (actionModel.columnActionModifier()) {
                // identity op
                case NONE -> response;
                // force full page reload
                case FORCE_STAY_ON_PAGE -> new ActionResultResponse(
                    ActionResultResponseHandlingStrategy.OPEN_URL_IN_SAME_BROWSER_WINDOW,
                    null, null, ajaxTarget, response.pageRedirect().toUrl()); // page redirect should point to current page
                // open result page in new browser tab/win
                case FORCE_NEW_BROWSER_WINDOW -> new ActionResultResponse(
                    ActionResultResponseHandlingStrategy.OPEN_URL_IN_NEW_BROWSER_WINDOW,
                    null, null, ajaxTarget, response.pageRedirect().toUrl()); // page redirect should point to action result
                }
            : response;
    }

    // -- HELPER

    private ActionResultResponse actionResultResponse(
            final @NonNull ActionModel actionModel,
            final @Nullable AjaxRequestTarget ajaxTarget,
            final @NonNull ActionResultModel actionResultModel) {

        final ActionResultResponseType responseType = actionResultModel.responseType();
        final ManagedObject resultAdapter = actionResultModel.resultAdapter();

        switch(responseType) {
        case COLLECTION: {
            _Assert.assertTrue(resultAdapter instanceof PackedManagedObject);
            var collectionModel = CollectionModelStandalone
                    .forActionModel((PackedManagedObject)resultAdapter, actionModel);
            var pageRedirectRequest = PageRedirectRequest.forPage(
                    StandaloneCollectionPage.class, new StandaloneCollectionPage(collectionModel));
            return ActionResultResponse.toPage(pageRedirectRequest);
        }
        case OBJECT: {
            determineScalarAdapter(actionModel.getMetaModelContext(), resultAdapter); // intercepts collections
            return ActionResultResponse.toDomainObjectPage(resultAdapter);
        }
        case SIGN_IN: {
            var signInPage = actionModel.getMetaModelContext()
                    .lookupServiceElseFail(PageClassRegistry.class)
                    .getPageClass(PageType.SIGN_IN);
            var pageRedirectRequest = PageRedirectRequest.forPageClass(signInPage);
            return ActionResultResponse.toPage(pageRedirectRequest);
        }
        case VALUE: {
            var valueModel = ValueModel.of(actionModel.getAction(), resultAdapter);
            valueModel.setActionHint(actionModel);
            var valuePage = new ValuePage(valueModel);
            var pageRedirectRequest = PageRedirectRequest.forPage(ValuePage.class, valuePage);
            return ActionResultResponse.toPage(pageRedirectRequest);
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
            var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            return ActionResultResponse
                    .openUrlInBrowser(ajaxTarget, localResPath.getEffectivePath(webAppContextPath::prependContextPath), localResPath.getOpenUrlStrategy());
        }
        case VALUE_LOCALRESPATH_NOAJAX: {
            // open URL server-side redirect
            final LocalResourcePath localResPath = (LocalResourcePath)resultAdapter.getPojo();
            var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
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
            var webAppContextPath = actionModel.getMetaModelContext().getWebAppContextPath();
            IRequestHandler handler = _RedirectHandler.redirectHandler(value, OpenUrlStrategy.NEW_WINDOW, webAppContextPath); // default behavior
            return ActionResultResponse.withHandler(handler);
        }
        case VOID_AS_EMPTY: {
            var pageRedirectRequest = PageRedirectRequest
                .forPage(VoidReturnPage.class, new VoidReturnPage(new VoidModel(), actionModel.getFriendlyName()));
            return ActionResultResponse.toPage(pageRedirectRequest);
        }
        case RELOAD: {
            var currentPage = PageRequestHandlerTracker.getLastHandler(RequestCycle.get()).getPage();
            var pageClass = currentPage.getClass();
            var pageRedirectRequest = PageRedirectRequest.forPage(pageClass, _Casts.uncheckedCast(currentPage));
            return ActionResultResponse.toPage(pageRedirectRequest);
        }
        default:
            throw _Exceptions.unmatchedCase(responseType);
        }
    }

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
