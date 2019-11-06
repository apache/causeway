package domain.application.lockdown;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assume.assumeThat;

public class LockDownMavenDeps_Test {

    @Before
    public void setUp() throws Exception {
        //assumeThat(System.getProperty("lockdown"), is(not(nullValue())));
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
