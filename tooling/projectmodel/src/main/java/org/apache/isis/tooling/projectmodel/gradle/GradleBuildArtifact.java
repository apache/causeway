package org.apache.isis.tooling.projectmodel.gradle;

import java.io.File;
import java.util.Optional;

import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
public class GradleBuildArtifact {
    private final String name;
    private final String realtivePath;
    private final File projectDirectory;
    
    public final boolean isRoot() {
        return realtivePath.equals("/");
    }
    
    public final Optional<File> getDefaultBuildFile() {
        val buildFile = new File(getProjectDirectory(), "build.gradle");
        if(buildFile.exists()) {
            return Optional.of(buildFile);
        }
        return Optional.empty();
    }
    
}
