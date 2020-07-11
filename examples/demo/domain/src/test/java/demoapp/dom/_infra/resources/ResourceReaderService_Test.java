package demoapp.dom._infra.resources;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.val;
import static org.assertj.core.api.Assertions.*;

import demoapp.dom._infra.resources.MarkupVariableResolverService;
import demoapp.dom._infra.resources.ResourceReaderService;

class ResourceReaderService_Test {

    ResourceReaderService resourceReaderService;

    @BeforeEach
    void setUp() {
        resourceReaderService = new ResourceReaderService();
        resourceReaderService.markupVariableResolverService = new MarkupVariableResolverService();
    }

    @Test
    void read_with_tags() {

        // given
        val attributes = new HashMap<String, Object>();
        attributes.put("tags", "class");

        // when
        String actual = resourceReaderService.readResource(getClass(), "ResourceReaderService_Test-Test1.java", attributes);

        // then
        String expected = resourceReaderService.readResource(getClass(), "ResourceReaderService_Test-Test1-expected.java");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void read_missing_tags() {

        // given
        val attributes = new HashMap<String, Object>();
        attributes.put("tags", "other");

        // when
        String actual = resourceReaderService.readResource(getClass(), "ResourceReaderService_Test-Test1.java", attributes);

        // then
        String expected = "";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void read_subdir_with_tags() {

        // given
        val attributes = new HashMap<String, Object>();
        attributes.put("tags", "class");

        // when
        String actual = resourceReaderService.readResource(getClass(), "subdir/ResourceReaderService_Test-Test1.java", attributes);

        // then
        String expected = resourceReaderService.readResource(getClass(), "subdir/ResourceReaderService_Test-Test1-expected.java");
        assertThat(actual).isEqualTo(expected);
    }

}
