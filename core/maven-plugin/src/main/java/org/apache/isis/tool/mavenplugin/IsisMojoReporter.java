package org.apache.isis.tool.mavenplugin;

import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public interface IsisMojoReporter {
    Log getLog();

    void logErrors(String... logMessages);

    void throwFailureException(String errorMessage, Set<String> logMessages) throws MojoFailureException;

    void throwFailureException(String errorMessage, String... logMessages) throws MojoFailureException;

    void throwExecutionException(String errorMessage, Exception e) throws MojoExecutionException;
}
