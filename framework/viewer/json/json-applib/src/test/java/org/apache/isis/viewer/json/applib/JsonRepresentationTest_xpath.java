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
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_xpath {


    private JsonRepresentation listRepresentation;
    private JsonRepresentation mapRepresentation;

    @Before
    public void setUp() throws Exception {
        mapRepresentation = new JsonRepresentation(readJson("map.json"));
        listRepresentation = new JsonRepresentation(readJson("servicesList.json"));
    }
    
    @Test
    public void mapXpath_matchingSingleElement() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation matching = mapRepresentation.xpath("//*[rel='someRel']");
        assertThat(matching, is(not(nullValue())));
        assertThat(matching.isArray(), is(false));
        assertThat(matching.getString("aLink.rel"), is("someRel"));
    }

    @Test
    public void mapXpath_matchingMultipleElementsInMap() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation matching = mapRepresentation.xpath("/*");
        assertThat(matching, is(not(nullValue())));
        assertThat(matching.isArray(), is(false));
        
        assertThat(matching.getString("aString"), is(equalTo(mapRepresentation.getString("aString"))));
        assertThat(matching.getLink("aLink"), is(equalTo(mapRepresentation.getLink("aLink"))));
    }

    @Test
    public void xpath_matchingNone() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation matching = mapRepresentation.xpath("//*[nonExistent='nonExistent']");
        assertThat(matching, is(nullValue()));
    }

    @Test
    public void xpath_againstList_returningOne() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation applibRepo = listRepresentation.xpath("/e[title='ApplibValues']");
        assertThat(applibRepo, is(not(nullValue())));
        assertThat(applibRepo.isArray(), is(false));
        assertThat(applibRepo.getString("e.title"), is("ApplibValues"));
    }
    
    @Test
    public void xpath_againstList_returningMany() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation applibRepo = listRepresentation.xpath("/e");
        assertThat(applibRepo, is(not(nullValue())));
        assertThat(applibRepo.isArray(), is(true));
        assertThat(applibRepo.arraySize(), is(4));
    }

    @Test
    public void xpath_againstList_returningNone() throws JsonParseException, JsonMappingException, IOException, ValidityException, ParsingException {
        JsonRepresentation applibRepo = listRepresentation.xpath("/e[title='NonExistent']");
        assertThat(applibRepo, is(nullValue()));
    }
    
}
          