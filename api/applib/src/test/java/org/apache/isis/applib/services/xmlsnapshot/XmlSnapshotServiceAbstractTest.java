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
package org.apache.isis.applib.services.xmlsnapshot;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.commons.internal.resources._Resources;

public class XmlSnapshotServiceAbstractTest {

    private XmlSnapshotServiceAbstract xmlSnapshotService;
    private String xmlStr;

    @Before
    public void setUp() throws Exception {
        xmlStr = _Resources.loadAsString(
                XmlSnapshotServiceAbstractTest.class, 
                "XmlSnapshotServiceAbstractTest.xml", 
                Charset.forName("UTF-8"));
        xmlSnapshotService = new XmlSnapshotServiceForUnitTesting();

    }


    @Test
    public void test() {

        Locale locale = Locale.getDefault();

        Locale[] locales = new Locale[]{Locale.getDefault(), lookupLocale("en", "US"), lookupLocale("en", "GB"), lookupLocale("es", "ES")};
        for (Locale eachLocal : locales) {
            try {

                Locale.setDefault(eachLocal);

                Document xmlDoc = xmlSnapshotService.asDocument(xmlStr);
                Element rootEl = xmlDoc.getDocumentElement();

                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someString", String.class), is("OXF"));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someLocalDate", LocalDate.class), is(new LocalDate(2013,4,1)));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someBigDecimal", BigDecimal.class), is(new BigDecimal("123456789012345678901234567890.12345678")));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someBigInteger", BigInteger.class), is(new BigInteger("12345678901234567890123456789012345678")));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someInteger", Integer.class), is(Integer.valueOf(123456789)));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someLong", Long.class), is(Long.valueOf(1234567890123456789L)));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someShort", Short.class), is(Short.valueOf((short)12345)));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someByte", Byte.class), is(Byte.valueOf((byte)123)));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someBoolean", Boolean.class), is(Boolean.TRUE));
                assertThat(
                        xmlSnapshotService.getChildElementValue(rootEl, "app:someBoolean2", Boolean.class), is(Boolean.FALSE));
            } finally {
                Locale.setDefault(locale);
            }
        }
    }

    private static Locale lookupLocale(String language, String country) {
        Locale[] availableLocales = Locale.getAvailableLocales();
        for (Locale locale : availableLocales) {
            if(locale.getCountry().equals(country) && locale.getLanguage().equals(language)) {
                return locale;
            }
        }
        throw new IllegalArgumentException("no such locale:" + language + "_" + country);
    }


    static class XmlSnapshotServiceForUnitTesting extends XmlSnapshotServiceAbstract {

        @Override
        public Snapshot snapshotFor(Object domainObject) {
            throw new RuntimeException();
        }

        @Override
        public Builder builderFor(Object domainObject) {
            throw new RuntimeException();
        }
    }

}
