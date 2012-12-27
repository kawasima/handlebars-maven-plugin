package net.unit8.maven.plugins.handlebars;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Handlebars precompile
 *
 * @phase compile
 * @goal precompile
 * @author kawasima
 *
 */
public class PrecompileMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

	/**
	 * @parameter
	 */
	private String[] templateExtensions;

	/**
	 * @parameter expression="${encoding}" default-value="UTF-8"
	 */
	private String encoding = "UTF-8";

	/**
	 * @required
	 * @parameter expression="${sourceDirectory}"
	 */
	protected File sourceDirectory;

	/**
	 * @parameter expression="${outputDirectory}"
	 */
	protected File outputDirectory;

	/**
	 * @parameter expression="${preserveHierarchy}"
	 */
	protected Boolean preserveHierarchy;

    /**
     * Handlebars script filename
     *
     * @paramter expression="${handlebarsName}" default-value="handlebars-1.0.rc.1.min.js"
     */
    protected String handlebarsName;

    private HandlebarsEngine handlebarsEngine;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (outputDirectory == null)
			outputDirectory = new File(sourceDirectory.getAbsolutePath());
		if (!outputDirectory.exists()) {
			try {
				FileUtils.forceMkdir(outputDirectory);
			} catch (IOException e) {
				throw new MojoExecutionException("Failure to make an output directory.", e);
			}
		}
		if (preserveHierarchy == null)
			preserveHierarchy = true;
			
		if (templateExtensions == null)
			templateExtensions = new String[]{"html"};

        handlebarsEngine = new HandlebarsEngine(handlebarsName);
        handlebarsEngine.setCacheDir(new File(project.getBuild().getDirectory(), "handlebars-maven-plugins/script"));

        try {
			visit(sourceDirectory);
		} catch(IOException e) {
			throw new MojoExecutionException("Failure to precompile handlebars templates.", e);
		}
	}

	protected void visit(File directory) throws IOException {
		precompile(directory);
		File[] children = directory.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY);
		for (File child : children) {
			visit(child);
		}
	}

	protected void precompile(File directory) throws IOException {
		Collection<File> templates = FileUtils.listFiles(directory, templateExtensions, false);
		if (templates.isEmpty())
			return;

		Context cx = Context.enter();
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(getOutputFile(directory)), encoding));
			out.print("(function() {\n  var template = Handlebars.template, templates = Handlebars.templates = Handlebars.templates || {};\n");
			// Rhino for Handlebars Template
			ScriptableObject global = cx.initStandardObjects();


			InputStreamReader in = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("script/handlebars-1.0.rc1.min.js"));
			cx.evaluateReader(global, in, "handlebars-1.0.rc1.min.js", 1, null);
			IOUtils.closeQuietly(in);

			for (File template : templates) {
				String data = FileUtils.readFileToString(template, "UTF-8");
				ScriptableObject.putProperty(global, "data", data);
				Object obj = cx.evaluateString(global, "Handlebars.precompile(String(data));", "<cmd>", 1, null);
				out.println("templates['" + FilenameUtils.getBaseName(template.getName()) + "']=(" + obj.toString() + ");");
			}
		} finally {
			Context.exit();
			out.println("})();");
			IOUtils.closeQuietly(out);
		}
	}
	private final File getOutputFile(File directory) throws IOException {
		if (preserveHierarchy) {
			String relativePath = sourceDirectory.toURI().relativize(directory.toURI()).getPath();
			File outputBaseDir = new File(outputDirectory, relativePath);
			if (!outputBaseDir.exists()) {
				FileUtils.forceMkdir(outputBaseDir);
			}
			return new File(outputBaseDir, directory.getName() + ".js");
		} else {
			String relativePath = sourceDirectory.toURI().relativize(directory.toURI()).getPath();
			String name = StringUtils.chomp(relativePath, "/").replace('/', '-');
			if (StringUtils.isEmpty(name))
				name = "index";
			return new File(outputDirectory, name + ".js");
		}
	}
}
