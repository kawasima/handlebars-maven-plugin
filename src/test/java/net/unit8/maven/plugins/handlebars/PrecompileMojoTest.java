package net.unit8.maven.plugins.handlebars;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrecompileMojoTest extends PrecompileMojo {
	private PrecompileMojo mojo;

	@Before
	public void setUp() {
		mojo = new PrecompileMojo();
		mojo.sourceDirectory = new File("src/test/resources/templates");
		mojo.outputDirectory = new File("target/output");
        mojo.handlebarsName =  "handlebars-1.0.rc1.min.js";
	}

	@Test
	public void testNotPreserveHierarchy() throws MojoExecutionException, MojoFailureException {
		mojo.preserveHierarchy = false;
		mojo.execute();
		assertTrue(new File(mojo.outputDirectory, "index.js").exists());
		assertTrue(new File(mojo.outputDirectory, "hoge.js").exists());
		assertTrue(new File(mojo.outputDirectory, "hoge-fuga.js").exists());
	}

	@Test
	public void testPreserveHierarchy() throws MojoExecutionException, MojoFailureException {
		mojo.preserveHierarchy = true;
		mojo.execute();
		assertTrue(new File(mojo.outputDirectory, "templates.js").exists());
		assertTrue(new File(mojo.outputDirectory, "hoge/hoge.js").exists());
		assertTrue(new File(mojo.outputDirectory, "hoge/fuga/fuga.js").exists());
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.forceDelete(mojo.outputDirectory);
	}
}
