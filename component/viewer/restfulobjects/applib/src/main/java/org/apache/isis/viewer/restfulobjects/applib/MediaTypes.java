package org.apache.isis.viewer.restfulobjects.applib;

import javax.ws.rs.core.MediaType;

import com.google.common.base.Function;

/**
 * Convert between {@link com.google.common.net.MediaType guava MediaType} and {@link javax.ws.rs.core.MediaType jax-rs MediaType}.
 */
final class MediaTypes {
    
    private MediaTypes(){}

    private static final Function<javax.ws.rs.core.MediaType, ? extends com.google.common.net.MediaType> JAXRS_TO_GUAVA = new Function<javax.ws.rs.core.MediaType, com.google.common.net.MediaType>() {

        @Override
        public com.google.common.net.MediaType apply(javax.ws.rs.core.MediaType input) {
            return MediaTypes.jaxRsToGuava(input);
        }
    };

    private static final Function<com.google.common.net.MediaType, MediaType> GUAVA_TO_JAXRS = new Function<com.google.common.net.MediaType, MediaType>() {

        @Override
        public MediaType apply(com.google.common.net.MediaType input) {
            return MediaTypes.guavaToJaxRs(input);
        }
    };


    private static com.google.common.net.MediaType jaxRsToGuava(javax.ws.rs.core.MediaType jaxRsMediaType) {
        return com.google.common.net.MediaType.parse(jaxRsMediaType.toString());
    }
    
    private static javax.ws.rs.core.MediaType guavaToJaxRs(com.google.common.net.MediaType guavaMediaType) {
        return javax.ws.rs.core.MediaType.valueOf(guavaMediaType.toString());
    }

}
