/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.memento;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.memento.MementoService;

/**
 * This service provides a mechanism by which a serializable memento of arbitrary state can be created.  Most
 * commonly this is in support of implementing the {@link org.apache.isis.applib.ViewModel} interface.
 *
 * <p>
 * This implementation has no UI and there are no other implementations of the service API, and so it annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  Because this class is implemented in core, this means
 * that it is automatically registered and available for use; no further configuration is required.
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class MementoServiceDefault implements MementoService {

    static class MementoDefault implements Memento {

        private final boolean noEncoding;
        private final Document doc;

        MementoDefault(boolean noEncoding) {
            this(DocumentHelper.createDocument(), noEncoding);
            doc.addElement("memento");
        }

        MementoDefault(Document doc, boolean noEncoding) {
            this.doc = doc;
            this.noEncoding = noEncoding;
        }
        
        @Override
        public Memento set(String name, Object value) {
            final Element el = doc.getRootElement();
            Dom4jUtil.addChild(el, name, value);
            return this;
        }

        @Override
        public <T> T get(String name, Class<T> cls) {
            final Element el = doc.getRootElement();
            return Dom4jUtil.getChild(el, name, cls);
        }

        @Override
        public String asString() {
            final String xmlStr = Dom4jUtil.asString(doc);
            return encode(xmlStr);
        }

        protected String encode(final String xmlStr) {
            return noEncoding ? xmlStr : base64UrlEncode(xmlStr);
        }

        private static final Function<Element, String> ELEMENT_NAME = new Function<Element, String>(){
            @Override
            public String apply(final Element input) {
                return input.getName();
            }
        };

        @Override
        public Set<String> keySet() {
            Element element = doc.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> elements = element.elements();
            return Sets.newLinkedHashSet(Iterables.transform(elements, ELEMENT_NAME));
        }

        // //////////////////////////////////////

        @Override
        public String toString() {
            return Dom4jUtil.asString(doc);
        }

    }

    // //////////////////////////////////////

    private boolean noEncoding;
    
    public MementoServiceDefault() {
        this.noEncoding = false;
    }

    /**
     * Not public API.
     */
    @Programmatic
    public MementoServiceDefault withNoEncoding() {
        this.noEncoding = true;
        return this;
    }
    
    // //////////////////////////////////////

    @Programmatic
    @Override
    public Memento create() {
        return new MementoDefault(noEncoding);
    }


    @Programmatic
    @Override
    public Memento parse(String str) {
        String xmlStr;
        if (noEncoding) {
            xmlStr = str;
        } else {
            xmlStr = base64UrlDecode(str);
        }
        final Document doc = Dom4jUtil.parse(xmlStr);
        return new MementoDefault(doc, noEncoding);
    }

    @Programmatic
    @Override
    public boolean canSet(final Object input) {
        return input != null ? Dom4jUtil.isSupportedClass(input.getClass()) : true;
    }

    // //////////////////////////////////////

    private static String base64UrlDecode(String str) {
        final byte[] bytes = BaseEncoding.base64Url().decode(str);
        return new String(bytes, Charset.forName("UTF-8"));
    }
    
    private static String base64UrlEncode(final String xmlStr) {
        byte[] bytes = xmlStr.getBytes(Charset.forName("UTF-8"));
        return BaseEncoding.base64Url().encode(bytes);
    }

}
