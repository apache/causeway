package org.apache.isis.viewer.wicket.ui.actionresponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _DownloadHandler {

    public IRequestHandler downloadHandler(
            final ObjectAction action,
            final Object value) {
        if(value instanceof Clob) {
            val clob = (Clob)value;
            return handlerFor(action, resourceStreamFor(clob), clob);
        }
        if(value instanceof Blob) {
            val blob = (Blob)value;
            return handlerFor(action, resourceStreamFor(blob), blob);
        }
        return null;
    }

    // -- HELPER

    private IResourceStream resourceStreamFor(final Blob blob) {
        final IResourceStream resourceStream = new AbstractResourceStream() {

            private static final long serialVersionUID = 1L;

            @Override
            public InputStream getInputStream() throws ResourceStreamNotFoundException {
                return new ByteArrayInputStream(blob.getBytes());
            }

            @Override
            public String getContentType() {
                return blob.getMimeType().toString();
            }

            @Override
            public void close() throws IOException {
            }
        };
        return resourceStream;
    }

    private IResourceStream resourceStreamFor(final Clob clob) {
        return new StringResourceStream(clob.getChars(), clob.getMimeType().toString());
    }

    private IRequestHandler handlerFor(
            final ObjectAction action,
            final IResourceStream resourceStream,
            final NamedWithMimeType namedWithMimeType) {
        val handler =
                new ResourceStreamRequestHandler(resourceStream, namedWithMimeType.getName());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);

        //ISIS-1619, prevent clients from caching the response content
        return action.getSemantics().isIdempotentOrCachable()
                ? handler
                : enforceNoCacheOnClientSide(handler);
    }

    // -- CLIENT SIDE CACHING ASPECTS ...

    private static IRequestHandler enforceNoCacheOnClientSide(final IRequestHandler downloadHandler){
        if(downloadHandler==null) {
            return downloadHandler;
        }
        if(downloadHandler instanceof ResourceStreamRequestHandler)
            ((ResourceStreamRequestHandler) downloadHandler)
            .setCacheDuration(org.apache.wicket.util.time.Duration.seconds(0));

        return downloadHandler;
    }

}
