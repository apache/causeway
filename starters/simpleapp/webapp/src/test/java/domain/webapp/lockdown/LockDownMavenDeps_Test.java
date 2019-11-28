package domain.webapp.lockdown;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import static org.hamcrest.CoreMatchers.*;

public class LockDownMavenDeps_Test {

    @BeforeEach
    public void setUp() throws Exception {
        Assumptions.assumeThat(System.getProperty("lockdown")).isNotNull();
    }

    @UseReporter(DiffReporter.class)
    @Test
    public void list() throws Exception {
        Approvals.verify(sort(read("list")));
    }

    @UseReporter(DiffReporter.class)
    @Test
    public void tree() throws Exception {
        Approvals.verify(read("tree"));
    }

    private String read(final String goal) throws IOException {
        final URL resource = Resources.getResource(getClass(),
                String.format("%s.%s.actual.txt", getClass().getSimpleName(), goal));
        return Resources.toString(resource, Charsets.UTF_8);
    }

    private static String sort(final String unsorted) {
        final String[] lines = unsorted.split("[\r\n]+");
        Arrays.sort(lines);
        return String.join("\n", lines);
    }

}
