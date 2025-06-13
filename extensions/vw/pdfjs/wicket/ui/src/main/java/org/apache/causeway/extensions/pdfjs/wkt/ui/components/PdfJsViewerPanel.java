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
package org.apache.causeway.extensions.pdfjs.wkt.ui.components;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.extensions.pdfjs.applib.config.PdfJsConfig;
import org.apache.causeway.extensions.pdfjs.applib.config.Scale;
import org.apache.causeway.extensions.pdfjs.applib.spi.PdfJsViewerAdvisor;
import org.apache.causeway.extensions.pdfjs.metamodel.facet.PdfJsViewerFacet;
import org.apache.causeway.extensions.pdfjs.wkt.integration.components.PdfJsPanel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 *
 */
class PdfJsViewerPanel
extends ScalarPanelAbstractLegacy
implements IRequestListener {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "regularFrame";
    private static final String ID_SCALAR_IF_COMPACT = "compactFrame";

    // regular frame
    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_FEEDBACK = "feedback";
    private static final String ID_DOWNLOAD = "download";

    // compact frame
    private static final String ID_FILE_NAME_IF_COMPACT = "compactFrame-fileName";
    private static final String ID_DOWNLOAD_IF_COMPACT = "compactFrame-download";

    AbstractDefaultAjaxBehavior updatePageNum;
    AbstractDefaultAjaxBehavior updateScale;
    AbstractDefaultAjaxBehavior updateHeight;

    PdfJsViewerPanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel);
    }

    interface Updater{
        void update(PdfJsViewerAdvisor advisor, final PdfJsViewerAdvisor.InstanceKey instanceKey);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        // so we have a callback URL

        updatePageNum = new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void respond(final AjaxRequestTarget _target)
            {
                String newPageNum = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("pageNum").toString();
                try {
                    final int pageNum = Integer.parseInt(newPageNum);
                    final Updater updater = (advisor, renderKey) -> advisor.pageNumChangedTo(renderKey, pageNum);
                    updateAdvisors(updater);
                } catch(Exception ex) {
                    // ignore
                }
            }
        };

        updateScale = new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void respond(final AjaxRequestTarget _target)
            {
                String newScale = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("scale").toString();
                try {
                    final Scale scale = Scale.forValue(newScale);
                    final Updater updater = (advisor, renderKey) -> advisor.scaleChangedTo(renderKey, scale);
                    updateAdvisors(updater);
                } catch(Exception ex) {
                    // ignore
                }

            }
        };

        updateHeight = new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void respond(final AjaxRequestTarget _target)
            {
                String newHeight = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("height").toString();
                try {
                    final int height = Integer.parseInt(newHeight);
                    final Updater updater = (advisor, renderKey) -> advisor.heightChangedTo(renderKey, height);
                    updateAdvisors(updater);
                } catch(Exception ex) {
                    // ignore
                }
            }

        };

        add(updatePageNum, updateScale, updateHeight);
    }

    private void updateAdvisors(final Updater updater) {
        var instanceKey = buildKey();
        getServiceRegistry().select(PdfJsViewerAdvisor.class)
        .forEach(advisor -> updater.update(advisor, instanceKey));
    }

    private PdfJsViewerAdvisor.InstanceKey buildKey() {
        return getServiceRegistry().lookupService(UserService.class)
                .map(this::toInstanceKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Could not locate UserService"));
    }

    private PdfJsViewerAdvisor.InstanceKey toInstanceKey(final UserService userService) {
        String userName = userService.currentUserNameElseNobody();

        var attributeModel = getModel();
        var propertyId = attributeModel.getIdentifier();
        var bookmark = attributeModel.getParentUiModel().getOwnerBookmark();
        var logicalTypeName = bookmark.logicalTypeName();
        var identifier = bookmark.identifier();

        return new PdfJsViewerAdvisor.InstanceKey(logicalTypeName, identifier, propertyId, userName);
    }

    @Override
    protected MarkupContainer createRegularFrame() {
        var blob = getBlob();
        if (blob == null
                || _NullSafe.isEmpty(blob.bytes())) { 
            return createShallowRegularFrame();
        }

        var attributeModel = attributeModel();

        var regularFrame = new WebMarkupContainer(ID_SCALAR_IF_REGULAR);

        var documentUrl = urlFor(
                new ListenerRequestHandler(
                        new PageAndComponentProvider(getPage(), this)))
                // adds a hash to the URL, such that browser caching works as desired 
                + "&md5=" + blob.md5Hex();
        var pdfJsConfig =
                attributeModel.getMetaModel().lookupFacet(PdfJsViewerFacet.class)
                .map(pdfJsViewerFacet->pdfJsViewerFacet.configFor(buildKey()))
                .orElseGet(PdfJsConfig::new)
                .withDocumentUrl(documentUrl);

        var pdfJsPanel = new PdfJsPanel(ID_SCALAR_VALUE, pdfJsConfig);

        var prevPageButton = createToolbarComponent("prevPage", pdfJsPanel);
        var nextPageButton = createToolbarComponent("nextPage", pdfJsPanel);
        var currentZoomSelect = createToolbarComponent("currentZoom", pdfJsPanel);
        var currentPageLabel = createToolbarComponent("currentPage", pdfJsPanel);
        var totalPagesLabel = createToolbarComponent("totalPages", pdfJsPanel);

        var currentHeightSelect = createToolbarComponent("currentHeight", pdfJsPanel);
        var printButton = createToolbarComponent("print", pdfJsPanel);

        var downloadResourceLink = Wkt.downloadLinkNoCache(ID_DOWNLOAD, asBlobResource(blob));

        regularFrame.addOrReplace(
                pdfJsPanel, prevPageButton, nextPageButton, currentPageLabel, totalPagesLabel,
                currentZoomSelect, currentHeightSelect, printButton, downloadResourceLink,
                new NotificationPanel(ID_FEEDBACK,
                        pdfJsPanel,
                        new ComponentFeedbackMessageFilter(pdfJsPanel)));

        return regularFrame;
    }

    @Override
    protected MarkupContainer createShallowRegularFrame() {
        var shallowRegularFrame = new WebMarkupContainer(ID_SCALAR_IF_REGULAR);
        WktComponents.permanentlyHide(shallowRegularFrame,
                ID_SCALAR_NAME, ID_SCALAR_VALUE, ID_FEEDBACK, ID_DOWNLOAD);
        return shallowRegularFrame;
    }

    @Override
    protected Component createCompactFrame() {
        var blob = getBlob();
        if (blob == null) {
            return createShallowCompactFrame();
        }
        var compactFrame = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);
        var downloadLink = Wkt.add(compactFrame, Wkt.downloadLinkNoCache(ID_DOWNLOAD_IF_COMPACT, asBlobResource(blob)));
        Wkt.labelAdd(downloadLink, ID_FILE_NAME_IF_COMPACT, blob.name());
        return compactFrame;
    }

    @Override
    protected Component createShallowCompactFrame() {
        var shallowCompactFrame = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);
        WktComponents.permanentlyHide(shallowCompactFrame,
                ID_DOWNLOAD_IF_COMPACT, ID_FILE_NAME_IF_COMPACT);
        return shallowCompactFrame;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(PdfJsViewerCssReference.asHeaderItem());
        response.render(PdfJsViewerJsReference.asHeaderItem());

        var script = PdfJsViewerCallbacksReference.instance().asString(Map.of(
                "pageNumCallbackUrl", updatePageNum.getCallbackUrl(),
                "scaleCallbackUrl", updateScale.getCallbackUrl(),
                "heightCallbackUrl", updateHeight.getCallbackUrl()));

        response.render(JavaScriptHeaderItem.forScript(script, "pdfJsViewerCallbacks"));
    }

    /**
     * from migration notes (https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+8.0):
     * "If you implemented IResourceListener previously, you have to override IRequestListener#rendersPage() now to return false."
     */
    @Override
    public boolean rendersPage() {
        return false;
    }

    /**
     * per migration notes (https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+8.0)
     * Assume this replaces IResourceListener#onResourceRequested()
     */
    @Override
    public void onRequest() {
        var blob = getBlob();
        if (blob == null) {
            throw new AbortWithHttpErrorCodeException(404);
        }
        getRequestCycle().scheduleRequestHandlerAfterCurrent(
                new ResourceRequestHandler(asBlobResourceNoCache(blob), null));
    }

    // -- HELPER
    
    private Blob getBlob() {
        return (Blob) MmUnwrapUtils.single(attributeModel().getObject());
    }

    private static ByteArrayResource asBlobResource(final @NonNull Blob blob) {
        return new ByteArrayResource(blob.mimeType().getBaseType(), blob.bytes(), blob.name());
    }

    private static ByteArrayResource asBlobResourceNoCache(final @NonNull Blob blob) {
        final byte[] bytes = blob.bytes();
        return new ByteArrayResource("application/pdf", bytes) {
            private static final long serialVersionUID = 1L;
            @Override protected void configureResponse(
                    final ResourceResponse response, final Attributes attributes) {
                super.configureResponse(response, attributes);
                response.disableCaching();
            }
        };
    }

    private MarkupContainer createToolbarComponent(final String id, final PdfJsPanel pdfJsPanel) {
        return new WebMarkupContainer(id) {
            private static final long serialVersionUID = 1L;
            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("data-canvas-id", pdfJsPanel.getCanvasId());
            }
        };
    }

}
