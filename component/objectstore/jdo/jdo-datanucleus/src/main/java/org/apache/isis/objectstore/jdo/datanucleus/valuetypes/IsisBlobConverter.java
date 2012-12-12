package org.apache.isis.objectstore.jdo.datanucleus.valuetypes;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.progmodel.facets.value.blobs.BlobValueSemanticsProvider;
import org.datanucleus.store.types.converters.TypeConverter;

public class IsisBlobConverter implements TypeConverter<Blob, String>{

    private static final long serialVersionUID = 1L;
    private EncoderDecoder<Blob> encoderDecoder;

    public IsisBlobConverter() {
        encoderDecoder = new BlobValueSemanticsProvider().getEncoderDecoder();
    }
    
    @Override
    public String toDatastoreType(Blob memberValue) {
        return encoderDecoder.toEncodedString(memberValue);
    }

    @Override
    public Blob toMemberType(String datastoreValue) {
        return encoderDecoder.fromEncodedString(datastoreValue);
    }

}
