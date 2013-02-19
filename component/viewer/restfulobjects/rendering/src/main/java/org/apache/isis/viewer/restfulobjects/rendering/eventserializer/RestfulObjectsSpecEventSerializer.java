package org.apache.isis.viewer.restfulobjects.rendering.eventserializer;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventSerializer;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

@Hidden
public class RestfulObjectsSpecEventSerializer implements EventSerializer {

    private final static JsonMapper jsonMapper = JsonMapper.instance();

    private final static String BASE_URL_KEY = "isis.viewer.restfulobjects.RestfulObjectsSpecEventSerializer.baseUrl";
    private static final String BASE_URL_DEFAULT = "http://localhost:8080/restful/";

    private String baseUrl;

    @PostConstruct
    public void init(Map<String,String> props) {
        final String baseUrlFromConfig = props.get(BASE_URL_KEY);
        baseUrl = baseUrlFromConfig != null? baseUrlFromConfig: BASE_URL_DEFAULT;
    }

    @PreDestroy
    public void shutdown() {
    }

    @Programmatic
    @Override
    public Object serialize(EventMetadata metadata, EventPayload payload) {
        final RendererContext rendererContext = new EventSerializerRendererContext(baseUrl, Where.OBJECT_FORMS);

        final JsonRepresentation payloadRepr = asPayloadRepr(rendererContext, payload);
        final JsonRepresentation eventRepr = asEventRepr(metadata, payloadRepr);

        return jsonFor(eventRepr);
    }

    JsonRepresentation asEventRepr(EventMetadata metadata, final JsonRepresentation payloadRepr) {
        final JsonRepresentation eventRepr = JsonRepresentation.newMap();
        final JsonRepresentation metadataRepr = JsonRepresentation.newMap();
        eventRepr.mapPut("metadata", metadataRepr);
        metadataRepr.mapPut("id", metadata.getId());
        metadataRepr.mapPut("transactionId", metadata.getTransactionId());
        metadataRepr.mapPut("sequence", metadata.getSequence());
        metadataRepr.mapPut("eventType", metadata.getEventType());
        metadataRepr.mapPut("user", metadata.getUser());
        metadataRepr.mapPut("timestamp", metadata.getTimestamp());
        eventRepr.mapPut("payload", payloadRepr);
        return eventRepr;
    }

    JsonRepresentation asPayloadRepr(final RendererContext rendererContext, EventPayload payload) {
        final DomainObjectReprRenderer renderer = new DomainObjectReprRenderer(rendererContext, null, JsonRepresentation.newMap());
        final ObjectAdapter objectAdapter = rendererContext.getAdapterManager().adapterFor(payload);
        renderer.with(objectAdapter).asEventSerialization();
        return renderer.render();
    }

    String jsonFor(final Object object) {
        try {
            return getJsonMapper().write(object);
        } catch (final JsonGenerationException e) {
            throw new RuntimeException(e);
        } catch (final JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    JsonMapper getJsonMapper() {
        return jsonMapper;
    }


}
