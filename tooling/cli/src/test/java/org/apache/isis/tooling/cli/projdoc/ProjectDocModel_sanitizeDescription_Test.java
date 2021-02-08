package org.apache.isis.tooling.cli.projdoc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProjectDocModel_sanitizeDescription_Test {

    @Test
    void strips() {

        final String str = ProjectDocModel.sanitizeDescription(
                "JDO Spring integration.\n" +
                "\t\t\n" +
                "\t\tThis is a fork of the Spring ORM JDO sources at github, \n" +
                "        for which support had been dropped back in 2016 [1].\n" +
                "\t\t\n" +
                "\t\tCredits to the original authors.\n" +
                "\t\t\n" +
                "\t\t[1] https://github.com/spring-projects/spring-framework/issues/18702");

        Assertions.assertEquals(str,
                "JDO Spring integration.\n" +
                "\n" +
                "This is a fork of the Spring ORM JDO sources at github,\n" +
                "for which support had been dropped back in 2016 [1].\n" +
                "\n" +
                "Credits to the original authors.\n" +
                "\n" +
                "[1] https://github.com/spring-projects/spring-framework/issues/18702");
    }
}
