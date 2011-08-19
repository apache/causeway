package org.apache.isis.viewer.json.applib.blocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.Test;

import com.google.common.base.Charsets;

public class LinkTest_urlEncoding {

    @Test
    public void test() throws UnsupportedEncodingException {
        Link l = new Link();
        l.setRel("object");
        l.setHref("http://localhost:8080/objects/ABC|123");
        l.setMethod(Method.GET);
        
        String expectedValue = "%FE%FF%00%7B%00%22rel%FE%FF%00%22%00%3A%00%22object%FE%FF%00%22%00%2C%00%22href%FE%FF%00%22%00%3A%00%22http%FE%FF%00%3A%00%2F%00%2Flocalhost%FE%FF%00%3A8080%FE%FF%00%2Fobjects%FE%FF%00%2FABC%FE%FF%00%7C123%FE%FF%00%22%00%2C%00%22method%FE%FF%00%22%00%3A%00%22GET%FE%FF%00%22%00%7D";
        @SuppressWarnings("unused")
        String decoded = URLDecoder.decode(expectedValue, Charsets.UTF_16.name());
        
        assertThat(l.asUrlEncoded(), is(expectedValue));
        
    }
}
