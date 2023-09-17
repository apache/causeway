package org.apache.causeway.commons.io;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.lang.Nullable;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Utilities for {@link URL}s.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class UrlUtils {

    // -- DECODE

    /**
     * @see URLDecoder
     */
    @SneakyThrows
    public static String urlDecodeUtf8(final @Nullable String input) {
        return input!=null
             ? URLDecoder.decode(input, StandardCharsets.UTF_8)
             : input;
    }

    // -- ENCODE

    /**
     * @see URLEncoder
     */
    @SneakyThrows
    public static String urlEncodeUtf8(final @Nullable String input) {
        return input!=null
                ? URLEncoder.encode(input, StandardCharsets.UTF_8)
                : input;
    }

    // -- VALIDITY

//XXX non optimized, so not published
//    /**
//     * Whether given url string is URL safe.
//     * @implNote this check is potentially expensive,
//     *      as it generates stacktraces for the non-happy cases
//     */
//    public boolean isUrlSafe(final String url) {
//        try {
//            new URL(url);
//            return true;
//        } catch (MalformedURLException e) {
//            return false;
//        }
//    }

}
