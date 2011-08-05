package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

public class JsonRepresentationTest_xpath {


    @Test
    public void xpath_matchingSingleElement() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation jsonRepresentation = 
                new JsonRepresentation(readJson("map.json"));

        JsonRepresentation matching = jsonRepresentation.xpath("//*[rel='someRel']");
        assertThat(matching, is(not(nullValue())));
        assertThat(matching.isArray(), is(false));
        assertThat(matching.getString("rel"), is("someRel"));
    }

    @Test
    public void xpath_matchingMultipleElementsInMap() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation jsonRepresentation = 
                new JsonRepresentation(readJson("map.json"));

        JsonRepresentation matching = jsonRepresentation.xpath("/*");
        assertThat(matching, is(not(nullValue())));
        assertThat(matching.isArray(), is(false));
        
        assertThat(matching.getString("aString"), is(equalTo(jsonRepresentation.getString("aString"))));
        assertThat(matching.getLink("aLink"), is(equalTo(jsonRepresentation.getLink("aLink"))));
    }

    @Test
    public void xpath_matchingNone() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation jsonRepresentation = 
                new JsonRepresentation(readJson("map.json"));

        JsonRepresentation matching = jsonRepresentation.xpath("//*[nonExistent='nonExistent']");
        assertThat(matching, is(nullValue()));
    }


}
