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
package org.apache.isis.viewer.restfulobjects.rendering.eventserializer;

import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.apache.isis.applib.annotation.DomainService;
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

/**
 * Serializes {@link org.apache.isis.applib.services.publish.EventMetadata event metadata} and corresponding
 * {@link org.apache.isis.applib.services.publish.EventPayload payload} into a JSON format corresponding to the
 * domain object representation specified by the Restful Objects spec.
 *
 * <p>
 * This implementation has no UI, has no side-effects, and there are no other implementations of the service API, and
 * so it annotated with {@link org.apache.isis.applib.annotation.DomainService}.  This class is implemented in the
 * <tt>o.a.i.module:isis-module-publishingeventserializer-ro</tt> module.  If that module is included in the classpath,
 * it this means that this service is automatically registered; no further configuration is required.
 */
@DomainService
public class RestfulObjectsSpecEventSerializer implements EventSerializer {

    private final static String BASE_URL_KEY = "isis.viewer.restfulobjects.RestfulObjectsSpecEventSerializer.baseUrl";
    private static final String BASE_URL_DEFAULT = "http://localhost:8080/restful/";

    //region > init, shutdown
    private String baseUrl;

    @Programmatic
    @PostConstruct
    public void init(Map<String,String> props) {
        final String baseUrlFromConfig = props.get(BASE_URL_KEY);
        baseUrl = baseUrlFromConfig != null? baseUrlFromConfig: BASE_URL_DEFAULT;
    }

    @Programmatic
    @PreDestroy
    public void shutdown() {
    }
    //endregion

    //region > serialize (API)
    @Programmatic
    @Override
    public Object serialize(EventMetadata metadata, EventPayload payload) {
        final RendererContext rendererContext = new EventSerializerRendererContext(baseUrl, Where.OBJECT_FORMS);

        final JsonRepresentation payloadRepr = asPayloadRepr(rendererContext, payload);
        final JsonRepresentation eventRepr = asEventRepr(metadata, payloadRepr);

        return jsonFor(eventRepr);
    }
    //endregion

    //region > supporting methods
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

    private final static JsonMapper jsonMapper = JsonMapper.instance();

    JsonMapper getJsonMapper() {
        return jsonMapper;
    }
    //endregion

}
