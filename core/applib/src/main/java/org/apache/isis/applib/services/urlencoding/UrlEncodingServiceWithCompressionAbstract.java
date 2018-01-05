package org.apache.isis.applib.services.urlencoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;

/**
 * to use, subclass and annotated with:
 * <pre>
 * &#064;DomainService(nature=DOMAIN, menuOrder="100")
 * </pre>
 */
public abstract class UrlEncodingServiceWithCompressionAbstract implements UrlEncodingService {

    @Override
    public String encode(final String str) {
        try {
            final byte[] compressed = compress(str);
            return base64Encoder.encodeToBase64(compressed);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String decode(final String str) {
        final byte[] bytes = base64Encoder.decodeBase64(str);
        try {
            return decompress(bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed;
    }

    private static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            baos.write(data, 0, bytesRead);
        }
        gis.close();
        return baos.toString("UTF-8");
    }

    @Inject
    UrlEncodingServiceUsingBaseEncoding base64Encoder;

}
