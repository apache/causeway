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
package org.apache.causeway.viewer.wicket.model.models.test;

import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintStore;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

class UiObjectWkt_hintsTest {

    MarkupContainer mockParent;
    Component mockComponent1;
    Component mockComponent2;

    UiObjectWkt target;
    MetaModelContext metaModelContext;
    HintStore_forTesting hintStore;

    @RequiredArgsConstructor
    static class WicketAppStup
    extends org.apache.wicket.Application
    implements HasMetaModelContext {
        @Getter final MetaModelContext metaModelContext;
        @Override public String getApplicationKey() {
            return null; }
        @Override public RuntimeConfigurationType getConfigurationType() {
            return null; }
        @Override public Class<? extends Page> getHomePage() {
            return null; }
        @Override public Session newSession(final Request request, final Response response) {
            return null; }
    }

    static class HintStore_forTesting implements HintStore {
        private final Map<String, String> map = _Maps.newHashMap();
        @Override public String get(final Bookmark bookmark, final String hintKey) {
            return map.get(hintKey);}
        @Override public void set(final Bookmark bookmark, final String hintKey, final String value) {
            map.put(hintKey, value); }
        @Override public void remove(final Bookmark bookmark, final String hintKey) {
            map.remove(hintKey); }
        @Override public void removeAll(final Bookmark bookmark) {
            map.clear(); }
        @Override public Set<String> findHintKeys(final Bookmark bookmark) {
            return map.keySet(); }
        @Override public String toString() {
            return map.toString(); }
    }

    @BeforeEach
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .singleton(hintStore = new HintStore_forTesting())
                .build();

        ThreadContext.setApplication(new WicketAppStup(metaModelContext));

        target = UiObjectWkt.ofBookmark(metaModelContext,
                Bookmark.forLogicalTypeNameAndIdentifier("hi", "there"));

        mockParent = Mockito.mock(MarkupContainer.class);
        mockComponent1 = Mockito.mock(Component.class);
        mockComponent2 = Mockito.mock(Component.class);

        Mockito.when(mockParent.getId()).thenReturn("parent");
        Mockito.when(mockComponent1.getId()).thenReturn("id1");
        Mockito.when(mockComponent2.getId()).thenReturn("id2");

        mockComponent1.setParent(mockParent);
        mockComponent2.setParent(mockParent);
    }

    @Test
    public void empty() throws Exception {
        assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
    }

    @Test
    public void single() throws Exception {
        target.setHint(mockComponent1, "key1", "value1");
        assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
    }

    @Test
    public void clear() throws Exception {
        target.setHint(mockComponent1, "key1", "value1");
        assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
        target.clearHint(mockComponent1, "key1");
        assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
    }

    @Test
    public void setToNull() throws Exception {
        target.setHint(mockComponent1, "key1", "value1");
        assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
        target.setHint(mockComponent1, "key1", null);
        assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
    }

    @Test
    public void multipleKeys() throws Exception {
        target.setHint(mockComponent1, "key1", "value1");
        target.setHint(mockComponent1, "key2", "value2");
        assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
        assertThat(target.getHint(mockComponent1, "key2"), is("value2"));
    }

    @Test
    public void multipleComponents() throws Exception {
        target.setHint(mockComponent1, "key", "valueA");
        target.setHint(mockComponent2, "key", "valueB");
        assertThat(target.getHint(mockComponent1, "key"), is("valueA"));
        assertThat(target.getHint(mockComponent2, "key"), is("valueB"));
    }

    @Test
    public void smoke() throws Exception {
        target.setHint(mockComponent1, "X", "11");
        target.setHint(mockComponent1, "A", "12");
        target.setHint(mockComponent1, "B", "13");
        target.setHint(mockComponent1, "C", "14");

        target.setHint(mockComponent2, "X", "21");
        target.setHint(mockComponent2, "P", "22");
        target.setHint(mockComponent2, "Q", "23");
        target.setHint(mockComponent2, "R", "24");

        assertThat(target.getHint(mockComponent1, "X"), is("11"));
        assertThat(target.getHint(mockComponent2, "X"), is("21"));
        assertThat(target.getHint(mockComponent1, "B"), is("13"));
        assertThat(target.getHint(mockComponent2, "R"), is("24"));
    }

}
