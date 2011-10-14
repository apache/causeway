package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.junit.Test;

import com.google.common.base.Charsets;

public class JsonRepresentationTest_urlEncoding {

    @Test
    public void test() throws UnsupportedEncodingException {
        Link l = new Link().withRel("object").withHref("http://localhost:8080/objects/ABC:123").withMethod(Method.GET);
        
        String expectedValue = "%7B%22method%22%3A%22GET%22%2C%22rel%22%3A%22object%22%2C%22href%22%3A%22http%3A%2F%2Flocalhost%3A8080%2Fobjects%2FABC%3A123%22%7D";
        @SuppressWarnings("unused")
        String decoded = URLDecoder.decode(expectedValue, Charsets.UTF_8.name());
        
        assertThat(l.asUrlEncoded(), is(expectedValue));
        
    }
}
