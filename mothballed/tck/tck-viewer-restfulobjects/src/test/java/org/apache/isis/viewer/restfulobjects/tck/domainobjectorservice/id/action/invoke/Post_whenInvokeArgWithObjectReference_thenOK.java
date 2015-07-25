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
package org.apache.isis.viewer.restfulobjects.tck.domainobjectorservice.id.action.invoke;

import org.jboss.resteasy.client.core.executors.URLConnectionClientExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.tck.dom.refs.ParentEntity;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectCollectionRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This test calls {@link ParentEntity#getChildren()} to retrieve the list of children,
 * then invokes {@link ParentEntity#removeChild(org.apache.isis.core.tck.dom.refs.ChildEntity)} to remove
 * one of the children (expoiting the fact that a list of {@link ParentEntity#choices0RemoveChild() choices} is
 * provided), then finally calls {@link ParentEntity#getChildren()} once more to confirm that one of the children has been removed.
 */
public class Post_whenInvokeArgWithObjectReference_thenOK {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    private DomainObjectResource objectResource;
    
    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient(new URLConnectionClientExecutor());
        objectResource = client.getDomainObjectResource();
    }

    /**
     * Tests change state, so discard such that will be recreated by next test.
     */
    @After
    public void tearDown() throws Exception {
        webServerRule.discardWebApp();
    }

    @Test
    public void usingClientFollow() throws Exception {

        // given
        RestfulResponse<ObjectCollectionRepresentation> childrenRestfulResponse = 
                RestfulResponse.ofT(objectResource.accessCollection("PRNT", "53", "children"));
        ObjectCollectionRepresentation childrenRepr = childrenRestfulResponse.getEntity();

        JsonRepresentation collValue = childrenRepr.getValue();

        final int numChildren = collValue.size();
        assertThat(numChildren, is(IsisMatchers.greaterThan(0)));

        final LinkRepresentation firstChildRepr = collValue.arrayIterator(LinkRepresentation.class).next();
        
        // when
        final RestfulResponse<ObjectActionRepresentation> removeChildRestfulResponse = 
                RestfulResponse.ofT(objectResource.actionPrompt("PRNT", "53", "removeChild"));
        final ObjectActionRepresentation removeChildRepr = removeChildRestfulResponse.getEntity();
        
        LinkRepresentation invokeLinkRepr = removeChildRepr.getLinkWithRel(Rel.INVOKE);
        JsonRepresentation args = invokeLinkRepr.getArguments();
        args.mapPut("childEntity.value", firstChildRepr);
        RestfulResponse<JsonRepresentation> invokeResp = client.follow(invokeLinkRepr, args);
        
        @SuppressWarnings("unused")
        JsonRepresentation invokeRepr = invokeResp.getEntity();
        final HttpStatusCode status = invokeResp.getStatus();
        assertThat(status, is(HttpStatusCode.OK));

        // then
        childrenRestfulResponse = 
                RestfulResponse.ofT(objectResource.accessCollection("PRNT", "53", "children"));
        childrenRepr = childrenRestfulResponse.getEntity();

        collValue = childrenRepr.getValue();
        assertThat(collValue.size(), is(numChildren-1));
    }

}
