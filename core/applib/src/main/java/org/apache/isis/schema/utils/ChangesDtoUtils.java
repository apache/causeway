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
package org.apache.isis.schema.utils;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.chg.v1.ChangesDto;
import org.apache.isis.schema.chg.v1.ObjectsDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.OidsDto;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

public final class ChangesDtoUtils {

    //region > marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(ChangesDto.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return jaxbContext;
    }

    public static ChangesDto fromXml(final Reader reader) {
        try {
            final Unmarshaller un = getJaxbContext().createUnmarshaller();
            return (ChangesDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static ChangesDto fromXml(final String xml) {
        return fromXml(new StringReader(xml));
    }

    public static ChangesDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final ChangesDto changesDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(changesDto, caw);
        return caw.toString();
    }

    public static void toXml(final ChangesDto changesDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(changesDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion



    //region > debugging (dump)
    public static void dump(final ChangesDto changesDto, final PrintStream out) throws JAXBException {
        out.println(toXml(changesDto));
    }

    //endregion

}
