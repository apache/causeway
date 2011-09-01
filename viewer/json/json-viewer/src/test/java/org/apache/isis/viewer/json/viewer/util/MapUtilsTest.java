package org.apache.isis.viewer.json.viewer.util;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;


public class MapUtilsTest {

	@Test
	public void happyCase() throws Exception {
	    Map<String, String> map = MapUtils.mapOf("foo", "bar", "foz", "boz");
	    assertThat(map.get("foo"), is("bar"));
	    assertThat(map.get("foz"), is("boz"));
	    assertThat(map.size(), is(2));
	}

   @Test
    public void emptyList() throws Exception {
        Map<String, String> map = MapUtils.mapOf();
        assertThat(map.size(), is(0));
    }

   @Test(expected=IllegalArgumentException.class)
   public void uneven() throws Exception {
       MapUtils.mapOf("foo");
   }

}
