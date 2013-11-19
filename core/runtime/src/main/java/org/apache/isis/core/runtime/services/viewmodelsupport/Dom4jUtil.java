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
package org.apache.isis.core.runtime.services.viewmodelsupport;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.joda.time.LocalDate;

import org.apache.isis.core.commons.exceptions.IsisException;

class Dom4jUtil {
    
    private Dom4jUtil(){}

    static void addChild(final Element el, final String name, final Object value) {
        if(value != null) {
            el.addElement(name).setText(value.toString());
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T getChild(final Element el, final String name, final Class<T> cls) {
        final Element child = el.element(name);
        if(child == null) { 
            return null;
        }
        final String str = child.getText();
        if (cls == String.class) {
            return (T) str;
        } else if (cls == Boolean.class) {
            return (T) new Boolean(str);
        } else if(cls == Byte.class) {
            return (T) new Byte(str);
        }else if(cls == Short.class) {
            return (T) new Short(str);
        }else if(cls == Integer.class) {
            return (T) new Integer(str);
        }else  if(cls == Long.class) {
            return (T) new Long(str);
        }else if(cls == Float.class) {
            return (T) new Float(str);
        }else if(cls == Double.class) {
            return (T) new Double(str);
        }else if(cls == BigDecimal.class) {
            return (T) new BigDecimal(str);
        }else if(cls == BigInteger.class) {
            return (T) new BigInteger(str);
        }else if(cls == LocalDate.class) { 
            return (T) new LocalDate(str);
        }else {
            throw new IllegalArgumentException("unsupported class '" + cls + "'");
        }
    }

    static Document parse(final String xmlStr) {
        try {
            final SAXReader saxReader = new SAXReader();
            Document doc = saxReader.read(new StringReader(xmlStr));
            return doc;
        } catch (DocumentException e) {
            throw new IsisException(e);
        }
    }

    static String asString(final Document doc) {
        XMLWriter writer = null;
        final StringWriter sw = new StringWriter();
        try {
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            writer = new XMLWriter(sw, outputFormat);
            writer.write(doc);
            return sw.toString();
        } catch (IOException e) {
            throw new IsisException(e);
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

}
