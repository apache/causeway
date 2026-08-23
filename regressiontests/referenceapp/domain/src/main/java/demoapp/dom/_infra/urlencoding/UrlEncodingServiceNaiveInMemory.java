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
package demoapp.dom._infra.urlencoding;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.UnrecoverableException;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.hash._Hashes;
import org.apache.causeway.commons.internal.hash._Hashes.Algorithm;

import lombok.RequiredArgsConstructor;

/**
 * Encoding blobs for view models will exceed the length allowed for an HTTP header;
 * this service will instead substitute with a UUID.
 */
@Service
@Named("demo.UrlEncodingServiceInMemory")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("InMemory")
public class UrlEncodingServiceNaiveInMemory implements UrlEncodingService {

    // this is a memory leak, so don't do this in a real app...
    private final Map<String, String> map = new HashMap<>();

    // this is set with respect to the Spring's server.max-http-header-size option in the application.properties
    // note: we reserve 4K of the total header size for other header attributes
    private final int maxIdentifierSize = 12*1024; // 12K

    @Override
    public String encode(final byte[] bytes) {

        // web servers might have restrictions to header sizes of eg. max 4k or 8k
        // if the encodedString is reasonable small, we pass it through
        var encodedString = urlEncodingService.encode(bytes); // from the default urlEncodingService
        if(encodedString.length()<maxIdentifierSize) {
            return EncodingType.PASS_THROUGH.encode(encodedString);
        }

        // if the encodedString is not reasonable small, we calculate a hash,
        // then store the encodedString in a map using this hash as the key
        var hashBytes = _Hashes.digest(Algorithm.SHA512, bytes)
                .orElseThrow(()->_Exceptions.unrecoverable("failed to generate SHA-512 hash"));

        // the key is exposed for web use with URLs, which requires us to encode them base64 URL safe
        var base64Key = _Strings.ofBytes(_Bytes.asUrlBase64.apply(hashBytes), StandardCharsets.UTF_8);

        map.put(base64Key, encodedString);
        return EncodingType.HASH_KEYED_CACHE.encode(base64Key);
    }

    @Override
    public byte[] decode(final String prefixed) {
        var encodingType = EncodingType.parse(prefixed);
        var encodedStringOrBase64Key = encodingType.decode(prefixed);

        switch (encodingType) {
        case PASS_THROUGH: {
            var encodedString = encodedStringOrBase64Key;
            return urlEncodingService.decode(encodedString);
        }
        case HASH_KEYED_CACHE: {
            var base64Key = encodedStringOrBase64Key;
            var encodedString = map.get(base64Key);
            if(encodedString==null) {
                throw new UnrecoverableException("Cache miss on view model recreation attempt. "
                        + "(This cache is specific to the Reference App.)");
            }
            return urlEncodingService.decode(encodedString);
        }
        default:
            throw _Exceptions.unmatchedCase(encodingType);
        }
    }

    @Inject
    @Qualifier("Compression")
    private UrlEncodingService urlEncodingService;

    // -- HELPER

    /**
     * Puts one character in front of the input,
     * or removes one character from the front of the input,
     * such that we can differentiate, which EncodingType is to be applied.
     */
    @RequiredArgsConstructor
    private static enum EncodingType {
        PASS_THROUGH('P'),
        HASH_KEYED_CACHE('H');
        private final char prefix;
        public String encode(final String input) {
            return prefix + input;
        }
        public String decode(final String input) {
            return input.substring(1);
        }
        public static EncodingType parse(final String input) {
            if(_NullSafe.size(input)<1) {
                throw _Exceptions.unrecoverable("input required size underflow");
            }
            switch (input.charAt(0)) {
            case 'P': return EncodingType.PASS_THROUGH;
            case 'H': return EncodingType.HASH_KEYED_CACHE;
            default: throw _Exceptions.unmatchedCase(input.charAt(0));
            }
        }
    }

}
