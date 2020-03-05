package com.connexta;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * The FeatureLint plugin checks project feature files to ensure that all features are uniquely named
 * and correctly prefixed..
 */
@Mojo(name = "lint")
public class FeatureLintMavenPlugin extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    public void execute () throws MojoExecutionException {
        String projectDirectory = project.getBasedir().getPath();
        FeatureLint.lint(projectDirectory);
    }
}
