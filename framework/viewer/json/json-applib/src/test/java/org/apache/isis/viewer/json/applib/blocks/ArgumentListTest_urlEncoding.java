package org.apache.isis.viewer.json.applib.blocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.Test;

import com.google.common.base.Charsets;

public class ArgumentListTest_urlEncoding {

    @Test
    public void test() throws UnsupportedEncodingException {
        ArgumentList al = new ArgumentList();
        al.add("foo");
        al.add(1);
        al.add(new Link().withRel("object").withHref("http://localhost/objects/ABC:123"));
        
        String expectedValue = "%5B%7B%22value%22%3A%22foo%22%7D%2C%7B%22value%22%3A1%7D%2C%7B%22value%22%3A%7B%22method%22%3A%22GET%22%2C%22rel%22%3A%22object%22%2C%22href%22%3A%22http%3A%2F%2Flocalhost%2Fobjects%2FABC%3A123%22%7D%7D%5D";
        @SuppressWarnings("unused")
        String decoded = URLDecoder.decode(expectedValue, Charsets.UTF_8.name());
        
        assertThat(al.asUrlEncoded(), is(expectedValue));
    }
}
