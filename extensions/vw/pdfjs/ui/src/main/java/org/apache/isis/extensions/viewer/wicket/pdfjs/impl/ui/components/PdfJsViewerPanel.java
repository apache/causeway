package org.apache.isis.extensions.viewer.wicket.pdfjs.impl.ui.components;

import java.io.IOException;
import java.net.URL;

import com.google.common.io.Resources;

import org.apache.commons.io.Charsets;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.time.Duration;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.PdfJsConfig;
import org.wicketstuff.pdfjs.PdfJsPanel;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.config.Scale;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.spi.PdfJsViewerAdvisor;
import org.apache.isis.extensions.viewer.wicket.pdfjs.impl.metamodel.facet.PdfJsViewerFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 *
 */
class PdfJsViewerPanel extends ScalarPanelAbstract implements IRequestListener {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_FEEDBACK = "feedback";

    AbstractDefaultAjaxBehavior updatePageNum;
    AbstractDefaultAjaxBehavior updateScale;
    AbstractDefaultAjaxBehavior updateHeight;

    String pdfJsViewerPanelCallbacksTemplateJs;

    private ServiceInjector serviceInjector;
    private ServiceRegistry serviceRegistry;

    PdfJsViewerPanel(String id, ScalarModel scalarModel) {
        super(id, scalarModel);

        final URL resource = Resources.getResource(PdfJsViewerPanel.class, "PdfJsViewerPanelCallbacks.template.js");
        try {
            pdfJsViewerPanelCallbacksTemplateJs = Resources.toString(resource, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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


        updatePageNum = new AbstractDefaultAjaxBehavior()
        {
            @Override
            protected void respond(AjaxRequestTarget _target)
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

        updateScale = new AbstractDefaultAjaxBehavior()
        {
            @Override
            protected void respond(AjaxRequestTarget _target)
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

        updateHeight = new AbstractDefaultAjaxBehavior()
        {
            @Override
            protected void respond(AjaxRequestTarget _target)
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
        serviceRegistry.select(PdfJsViewerAdvisor.class)
                .forEach(advisor -> updater.update(advisor, instanceKey));
    }

    private PdfJsViewerAdvisor.InstanceKey buildKey() {
        return serviceRegistry.lookupService(UserService.class)
            .map(this::toInstanceKey)
            .orElseThrow(() -> new IllegalStateException(
                                    "Could not locate UserService"));
    }

    private PdfJsViewerAdvisor.InstanceKey toInstanceKey(UserService userService) {
        String userName = userService.getUser().getName();

        val model = getModel();
        val propertyId = model.getPropertyMemento().getIdentifier();
        val bookmark = model.getParentEntityModel().getObjectAdapterMemento().asBookmarkIfSupported();
        val objectType = bookmark.getObjectType();
        val identifier = bookmark.getIdentifier();

        return new PdfJsViewerAdvisor.InstanceKey(objectType, identifier, propertyId, userName);
    }


    @Override
    protected MarkupContainer addComponentForRegular() {

        MarkupContainer containerIfRegular = new WebMarkupContainer("scalarIfRegular");
        addOrReplace(containerIfRegular);

        final ManagedObject adapter = scalarModel.getObject();
        if (adapter != null) {
            val pdfJsViewerFacet = scalarModel.getFacet(PdfJsViewerFacet.class);
            val instanceKey = buildKey();
            val pdfJsConfig = pdfJsViewerFacet != null
                    ? pdfJsViewerFacet.configFor(instanceKey)
                    : new PdfJsConfig();

            // Wicket 8 migration: previously this was urlFor(IResourceListener.INTERFACE, null);
            val urlStr = urlFor(
                    new ListenerRequestHandler(
                            new PageAndComponentProvider(getPage(), this)));
            pdfJsConfig.withDocumentUrl(urlStr);
            val pdfJsPanel = new PdfJsPanel(ID_SCALAR_VALUE, pdfJsConfig);

            val prevPageButton = createComponent("prevPage", pdfJsConfig);
            val nextPageButton = createComponent("nextPage", pdfJsConfig);
            val currentZoomSelect = createComponent("currentZoom", pdfJsConfig);
            val currentPageLabel = createComponent("currentPage", pdfJsConfig);
            val totalPagesLabel = createComponent("totalPages", pdfJsConfig);

            val currentHeightSelect = createComponent("currentHeight", pdfJsConfig);
            val printButton = createComponent("print", pdfJsConfig);

            //MarkupContainer downloadButton = createComponent("download", config);

            val blob = getBlob();
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
    protected Component addComponentForCompact() {
        final Blob blob = getBlob();
        if (blob == null) {
            return null;
        }
        val containerIfCompact = new WebMarkupContainer("scalarIfCompact");
        addOrReplace(containerIfCompact);

        final IResource bar = new ByteArrayResource(blob.getMimeType().getBaseType(), blob.getBytes(), blob.getName());
        final ResourceLink<Void> downloadLink = new ResourceLink<>("scalarIfCompactDownload", bar);
        containerIfCompact.add(downloadLink);

        Label fileNameIfCompact = new Label("fileNameIfCompact", blob.getName());
        downloadLink.add(fileNameIfCompact);

        return containerIfCompact;
    }

    private MarkupContainer createComponent(final String id, final PdfJsConfig config) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("data-canvas-id", config.getCanvasId());
            }
        };
    }

    @Override
    protected void addFormComponentBehavior(final Behavior behavior) {
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new CssResourceReference(PdfJsViewerPanel.class, "PdfJsViewerPanel.css")));
        response.render(JavaScriptHeaderItem.forReference(new PdfJsViewerReference()));

         renderFunctionsForUpdateCallbacks(response);
    }

    private void renderFunctionsForUpdateCallbacks(final IHeaderResponse response) {

        String script = pdfJsViewerPanelCallbacksTemplateJs
                .replace("__updatePageNum_getCallbackUrl()__", updatePageNum.getCallbackUrl())
                .replace("__updateScale_getCallbackUrl()__", updateScale.getCallbackUrl())
                .replace("__updateHeight_getCallbackUrl()__", updateHeight.getCallbackUrl());

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
                @Override protected void configureResponse(
                        final ResourceResponse response, final Attributes attributes) {
                    super.configureResponse(response, attributes);
                    response.setCacheDuration(Duration.NONE);
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
