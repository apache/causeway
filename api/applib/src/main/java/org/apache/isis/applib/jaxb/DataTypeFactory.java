package org.apache.isis.applib.jaxb;

import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DataTypeFactory {

    /*
     * Gets an instance of DatatypeFactory and passes it to the factory argument. (thread-safe)
     */
    public static XMLGregorianCalendar withTypeFactoryDo(
            Function<DatatypeFactory, XMLGregorianCalendar> factory) {

        final DatatypeFactory dataTypeFactory;

        try {

            dataTypeFactory = DatatypeFactory.newInstance();

        } catch (DatatypeConfigurationException e) {

            System.err.println("Within "+ JavaTimeXMLGregorianCalendarMarshalling.class.getName()+": "+
                    "Exception in call to DatatypeFactory.newInstance()" + e);
            return null;

        }

        return factory.apply(dataTypeFactory);
    }

}
