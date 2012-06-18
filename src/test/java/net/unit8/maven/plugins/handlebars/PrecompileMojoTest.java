package net.unit8.maven.plugins.handlebars;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class PrecompileMojoTest extends PrecompileMojo {

	@Test
	public void test() throws MojoExecutionException, MojoFailureException {
		PrecompileMojo mojo = new PrecompileMojo();
		mojo.sourceDirectory = new File("src/test/resources/templates");
		mojo.outputDirectory = new File("target/output");
		mojo.preserveHierarchy = false;
		mojo.execute();
	}
}
