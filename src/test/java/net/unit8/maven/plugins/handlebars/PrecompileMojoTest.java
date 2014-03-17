package net.unit8.maven.plugins.handlebars;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class PrecompileMojoTest extends PrecompileMojo {
	private PrecompileMojo mojo;

	@Before
	public void setUp() {
		mojo = new PrecompileMojo();
		mojo.sourceDirectory = new File("src/test/resources/templates");
		mojo.outputDirectory = new File("target/output");
        mojo.handlebarsName =  "handlebars-1.0.12.min.js";
        mojo.knownHelpers = Arrays.asList("helper");
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
    public void testKnownHelpersOnly() throws MojoExecutionException, MojoFailureException {
        mojo.knownHelpers = Collections.emptyList();
        mojo.knownHelpersOnly = true;
        try {
            mojo.execute();
            fail();
        } catch (JavaScriptException e) { }
        assertFalse(new File(mojo.outputDirectory, "with_helper.js").exists());
    }


    @Test
	public void testPreserveHierarchy() throws MojoExecutionException, MojoFailureException {
		mojo.preserveHierarchy = true;
		mojo.execute();
		assertTrue(new File(mojo.outputDirectory, "templates.js").exists());
		assertTrue(new File(mojo.outputDirectory, "hoge/hoge.js").exists());
		assertTrue(new File(mojo.outputDirectory, "hoge/fuga/fuga.js").exists());
	}

    @Test
    public void testPurgingWhitespace() throws MojoExecutionException, MojoFailureException, IOException {
        mojo.preserveHierarchy = false;
        mojo.purgeWhitespace = true;
        mojo.execute();
        File precompiled = new File(mojo.outputDirectory, "index.js");
        assertTrue(precompiled.exists());

        Context cx = Context.enter();
        try {
            ScriptableObject global = cx.initStandardObjects();
            URL handlebarsUrl = getClass().getClassLoader().getResource("script/handlebars-1.0.12.min.js");
            if (handlebarsUrl == null)
                throw new IllegalArgumentException("can't find resource handlebars.");
            InputStreamReader in = new InputStreamReader(handlebarsUrl.openStream());
            try {
                cx.evaluateReader(global, in, handlebarsName, 1, null);
            } finally {
                IOUtils.closeQuietly(in);
            }

            FileReader inSource = new FileReader(precompiled);
            try {
                cx.evaluateReader(global, inSource, precompiled.getName(), 1, null);
            } finally {
                IOUtils.closeQuietly(inSource);
            }

            Object obj = cx.evaluateString(global, "Handlebars.templates['root1']({hello:'I am '})", "<inline>", 1, null);
            assertEquals("I am root1", obj.toString());
        } finally {
            Context.exit();
        }

    }

	@After
	public void tearDown() throws IOException {
		FileUtils.forceDelete(mojo.outputDirectory);
	}
}
