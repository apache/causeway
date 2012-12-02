package org.apache.isis.tools.mavenplugin.util;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

public final class MavenProjects {
    
    private MavenProjects(){}

    public static Plugin lookupPlugin(MavenProject mavenProject, String key) {
    
        @SuppressWarnings("unchecked")
        List<Plugin> plugins = mavenProject.getBuildPlugins();
    
        for (Plugin plugin : plugins) {
            if (key.equalsIgnoreCase(plugin.getKey())) {
                return plugin;
            }
        }
        return null;
    }

}
