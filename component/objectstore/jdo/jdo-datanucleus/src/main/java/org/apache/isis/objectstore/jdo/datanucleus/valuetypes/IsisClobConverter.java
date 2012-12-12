package org.apache.isis.objectstore.jdo.datanucleus.valuetypes;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.progmodel.facets.value.clobs.ClobValueSemanticsProvider;
import org.datanucleus.store.types.converters.TypeConverter;

public class IsisClobConverter implements TypeConverter<Clob, String>{

    private static final long serialVersionUID = 1L;
    private EncoderDecoder<Clob> encoderDecoder;

    public IsisClobConverter() {
        encoderDecoder = new ClobValueSemanticsProvider().getEncoderDecoder();
    }
    
    @Override
    public String toDatastoreType(Clob memberValue) {
        return encoderDecoder.toEncodedString(memberValue);
    }

    @Override
    public Clob toMemberType(String datastoreValue) {
        return encoderDecoder.fromEncodedString(datastoreValue);
    }

}
