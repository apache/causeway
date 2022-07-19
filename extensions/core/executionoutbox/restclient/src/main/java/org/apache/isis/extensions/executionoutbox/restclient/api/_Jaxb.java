package org.apache.isis.extensions.executionoutbox.restclient.api;

import java.io.CharArrayWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;



/**
 * Helper methods for converting {@link javax.xml.bind.annotation.XmlRootElement}-annotated class to-and-from XML.  Intended primarily for
 * test use only (the {@link JAXBContext} is not cached).
 *
 * <p>
 * For example usage, see <a href="https://github.com/isisaddons/isis-module-publishmq">Isis addons' publishmq module</a> (non-ASF)
 * </p>
 */
class _Jaxb {

    private _Jaxb(){}

    static <T> T fromXml(
            final Reader reader,
            final Class<T> dtoClass) {
        Unmarshaller un = null;
        try {
            un = jaxbContextFor(dtoClass).createUnmarshaller();
            return (T) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> String toXml(final T dto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(dto, caw);
        return caw.toString();
    }

    static <T> void toXml(final T dto, final Writer writer) {
        Marshaller m = null;
        try {
            final Class<?> aClass = dto.getClass();
            m = jaxbContextFor(aClass).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(dto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Class<?>, JAXBContext> jaxbContextByClass = new ConcurrentHashMap<>();

    private static <T> JAXBContext jaxbContextFor(final Class<T> dtoClass)  {
        JAXBContext jaxbContext = jaxbContextByClass.get(dtoClass);
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(dtoClass);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            jaxbContextByClass.put(dtoClass, jaxbContext);
        }
        return jaxbContext;
    }
}
