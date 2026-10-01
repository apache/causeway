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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.util.Optional;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderService;

/** Covers the reference-parameter path, including its existing load-failure handling. */
class ObjectActionArgHelperReferenceTest {
    private final MetaModelContext mmc = mock(MetaModelContext.class, RETURNS_DEEP_STUBS);
    private final IResourceContext context = new IResourceContext() {
        @Override public MetaModelContext getMetaModelContext() { return mmc; }
        @Override public String restfulUrlFor(String url) { throw new UnsupportedOperationException(); }
        @Override public String applicationUrlFor(String url) { throw new UnsupportedOperationException(); }
        @Override public List<MediaType> getAcceptableMediaTypes() { throw new UnsupportedOperationException(); }
        @Override public InteractionInitiatedBy getInteractionInitiatedBy() { throw new UnsupportedOperationException(); }
        @Override public Where getWhere() { throw new UnsupportedOperationException(); }
        @Override public ObjectAdapterLinkTo getObjectAdapterLinkTo() { throw new UnsupportedOperationException(); }
        @Override public List<List<String>> getFollowLinks() { throw new UnsupportedOperationException(); }
        @Override public boolean isValidateOnly() { throw new UnsupportedOperationException(); }
        @Override public boolean canEagerlyRender(ManagedObject object) { throw new UnsupportedOperationException(); }
        @Override public RepresentationService.Intent getIntent() { throw new UnsupportedOperationException(); }
    };
    private final ObjectManager objects = mock(ObjectManager.class);
    private final ObjectAction action = mock(ObjectAction.class);
    private final ObjectActionParameter parameter = mock(ObjectActionParameter.class);
    private final ObjectSpecification baseSpec = mock(ObjectSpecification.class);
    private final Bookmark bookmark = Bookmark.forLogicalTypeNameAndIdentifier("test.PaymentBatchOrchestration", "42");

    private JsonRepresentation arguments() {
        when(mmc.getObjectManager()).thenReturn(objects);
        when(mmc.getServiceRegistry().lookupServiceElseFail(JsonValueEncoderService.class))
                .thenReturn(mock(JsonValueEncoderService.class));
        when(action.getParameters()).thenReturn(Can.ofSingleton(parameter));
        when(action.getParameterById("orchestration")).thenReturn(parameter);
        when(parameter.getId()).thenReturn("orchestration");
        when(parameter.getElementType()).thenReturn(baseSpec);
        final JsonRepresentation reference = JsonRepresentation.newMap();
        reference.mapPutString("href", "http://localhost/restful/objects/test.PaymentBatchOrchestration/42");
        final JsonRepresentation argument = JsonRepresentation.newMap();
        argument.mapPutJsonRepresentation("value", reference);
        final JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPutJsonRepresentation("orchestration", argument);
        return args;
    }

    @Test
    void loadsConcreteBookmarkForBaseTypeParameterBeforeInvocation() {
        final ManagedObject concrete = mock(ManagedObject.class);
        when(objects.loadObject(bookmark)).thenReturn(Optional.of(concrete));
        final var parsed = ObjectActionArgHelper.parseArguments(context, action, arguments()).getElseFail(0);
        assertTrue(parsed.isSuccess(), () -> parsed.getFailure().map(Object::toString).orElse(""));
        assertSame(concrete, parsed.getSuccessElseFail());
        verify(objects).loadObject(bookmark);
        // Parsing only resolves references; callback invocation follows in _DomainResourceHelper.
        verifyNoMoreInteractions(objects);
    }

    @Test
    void loadFailureBecomesParameterVeto() {
        when(objects.loadObject(bookmark)).thenThrow(new IllegalStateException("provider lock failure"));
        final var parsed = ObjectActionArgHelper.parseArguments(context, action, arguments()).getElseFail(0);
        assertTrue(parsed.isFailure());
        assertTrue(parsed.getSuccess().isEmpty());
        verify(objects).loadObject(bookmark);
    }
}
