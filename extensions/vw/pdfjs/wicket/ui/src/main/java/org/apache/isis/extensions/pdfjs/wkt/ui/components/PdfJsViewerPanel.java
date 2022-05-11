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
package org.apache.isis.extensions.pdfjs.wkt.ui.components;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;

import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.extensions.pdfjs.applib.config.PdfJsConfig;
import org.apache.isis.extensions.pdfjs.applib.config.Scale;
import org.apache.isis.extensions.pdfjs.applib.spi.PdfJsViewerAdvisor;
import org.apache.isis.extensions.pdfjs.metamodel.facet.PdfJsViewerFacet;
import org.apache.isis.extensions.pdfjs.wkt.integration.components.PdfJsPanel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 *
 */
class PdfJsViewerPanel
extends ScalarPanelAbstractLegacy
implements IRequestListener {

    private static final long serialVersionUID = 1L;

    AbstractDefaultAjaxBehavior updatePageNum;
    AbstractDefaultAjaxBehavior updateScale;
    AbstractDefaultAjaxBehavior updateHeight;

    PdfJsViewerPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    /**
     * from migration notes (https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+8.0):
     * "If you implemented IResourceListener previously, you have to override IRequestListener#rendersPage() now to return false."
     */
    @Override
    public boolean rendersPage() {
        return false;
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
                    final Updater updater = new Updater() {
                        @Override
                        public void update(
                                final PdfJsViewerAdvisor advisor,
                                final PdfJsViewerAdvisor.InstanceKey renderKey) {
                            advisor.pageNumChangedTo(renderKey, pageNum);
                        }
                    };
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
                    final Updater updater = new Updater() {
                        @Override
                        public void update(
                                final PdfJsViewerAdvisor advisor,
                                final PdfJsViewerAdvisor.InstanceKey renderKey) {
                            advisor.scaleChangedTo(renderKey, scale);
                        }
                    };
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
                    final Updater updater = new Updater() {
                        @Override
                        public void update(
                                final PdfJsViewerAdvisor advisor,
                                final PdfJsViewerAdvisor.InstanceKey renderKey) {
                            advisor.heightChangedTo(renderKey, height);
                        }
                    };
                    updateAdvisors(updater);
                } catch(Exception ex) {
                    // ignore
                }
            }

        };

        add(updatePageNum, updateScale, updateHeight);
    }

    private void updateAdvisors(final Updater updater) {
        val instanceKey = buildKey();
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

        val scalarModel = getModel();
        val propertyId = scalarModel.getIdentifier();
        val bookmark = scalarModel.getParentUiModel().getOwnerBookmark();
        val logicalTypeName = bookmark.getLogicalTypeName();
        val identifier = bookmark.getIdentifier();

        return new PdfJsViewerAdvisor.InstanceKey(logicalTypeName, identifier, propertyId, userName);
    }


    @Override
    protected MarkupContainer createRegularFrame() {

        val scalarModel = scalarModel();

        MarkupContainer containerIfRegular = new WebMarkupContainer(ID_SCALAR_IF_REGULAR);

        final ManagedObject adapter = scalarModel.getObject();
        val blob = getBlob();

        if (adapter != null
                && blob != null) {

            val pdfJsConfig =
                    scalarModel.lookupFacet(PdfJsViewerFacet.class)
                    .map(pdfJsViewerFacet->pdfJsViewerFacet.configFor(buildKey()))
                    .orElseGet(PdfJsConfig::new)
                    .withDocumentUrl(urlFor(
                            new ListenerRequestHandler(
                                    new PageAndComponentProvider(getPage(), this))));

            val pdfJsPanel = new PdfJsPanel(ID_SCALAR_VALUE, pdfJsConfig);

            val prevPageButton = createComponent("prevPage", pdfJsPanel);
            val nextPageButton = createComponent("nextPage", pdfJsPanel);
            val currentZoomSelect = createComponent("currentZoom", pdfJsPanel);
            val currentPageLabel = createComponent("currentPage", pdfJsPanel);
            val totalPagesLabel = createComponent("totalPages", pdfJsPanel);

            val currentHeightSelect = createComponent("currentHeight", pdfJsPanel);
            val printButton = createComponent("print", pdfJsPanel);

            //MarkupContainer downloadButton = createComponent("download", config);

            val byteArrayResource = new ByteArrayResource(blob.getMimeType().getBaseType(), blob.getBytes(), blob.getName());
            val downloadResourceLink = new ResourceLink<>("download", byteArrayResource);

            containerIfRegular.addOrReplace(
                    pdfJsPanel, prevPageButton, nextPageButton, currentPageLabel, totalPagesLabel,
                    currentZoomSelect, currentHeightSelect, printButton, downloadResourceLink);


            //            Label fileNameIfCompact = new Label("fileNameIfCompact", blob.getName());
            //            downloadLink.add(fileNameIfCompact);


            containerIfRegular.addOrReplace(new NotificationPanel(ID_FEEDBACK, pdfJsPanel, new ComponentFeedbackMessageFilter(pdfJsPanel)));
        } else {
            permanentlyHide(ID_SCALAR_VALUE, ID_FEEDBACK);
        }

        return containerIfRegular;
    }

    @Override
    protected Component createCompactFrame() {
        final Blob blob = getBlob();
        if (blob == null) {
            return null;
        }
        val containerIfCompact = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);

        final IResource bar = new ByteArrayResource(blob.getMimeType().getBaseType(), blob.getBytes(), blob.getName());
        final ResourceLink<Void> downloadLink = new ResourceLink<>(ID_DOWNLOAD_IF_COMPACT, bar);
        containerIfCompact.add(downloadLink);

        Label fileNameIfCompact = new Label(ID_FILE_NAME_IF_COMPACT, blob.getName());
        downloadLink.add(fileNameIfCompact);

        return containerIfCompact;
    }

    private MarkupContainer createComponent(final String id, final PdfJsPanel pdfJsPanel) {
        return new WebMarkupContainer(id) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("data-canvas-id", pdfJsPanel.getCanvasId());
            }
        };
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(PdfJsViewerCssReference.asHeaderItem());
        response.render(PdfJsViewerJsReference.asHeaderItem());

        val script = PdfJsViewerCallbacksReference.instance().asString(Map.of(
                "pageNumCallbackUrl", updatePageNum.getCallbackUrl(),
                "scaleCallbackUrl", updateScale.getCallbackUrl(),
                "heightCallbackUrl", updateHeight.getCallbackUrl()));

        response.render(JavaScriptHeaderItem.forScript(script, "pdfJsViewerCallbacks"));
    }

    /**
     * per migration notes (https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+8.0)
     * Assume this replaces IResourceListener#onResourceRequested()
     */
    @Override
    public void onRequest() {
        Blob pdfBlob = getBlob();
        if (pdfBlob != null) {
            final byte[] bytes = pdfBlob.getBytes();
            final ByteArrayResource resource = new ByteArrayResource("application/pdf", bytes) {
                private static final long serialVersionUID = 1L;

                @Override protected void configureResponse(
                        final ResourceResponse response, final Attributes attributes) {
                    super.configureResponse(response, attributes);
                    response.disableCaching();
                }
            };
            final ResourceRequestHandler handler = new ResourceRequestHandler(resource, null);
            getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
        } else {
            throw new AbortWithHttpErrorCodeException(404);
        }
    }

    private Blob getBlob() {
        final ManagedObject adapter = getModel().getObject();
        return adapter != null
                ? (Blob) adapter.getPojo()
                : null;
    }

}
