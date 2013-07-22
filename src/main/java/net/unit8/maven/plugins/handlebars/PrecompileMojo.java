/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.unit8.maven.plugins.handlebars;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

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
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

	/**
	 * @parameter
	 */
	protected String[] templateExtensions;


    /**
     * @parameter
     */
    protected Boolean purgeWhitespace;

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
     * @parameter expression="${knownHelpers}"
     */
    protected List<String> knownHelpers;

    /**
     * @parameter expression="${knownHelpersOnly}"
     */
    protected Boolean knownHelpersOnly;

	/**
	 * @parameter expression="${preserveHierarchy}"
	 */
	protected Boolean preserveHierarchy;

    /**
     * Handlebars script filename
     *
     * @parameter expression="${handlebarsName}" default-value="handlebars-1.0.12.min.js"
     */
    protected String handlebarsName;

    /**
     * @parameter expression="${encoding}" default-value="UTF-8"
     */
    protected String encoding = "UTF-8";

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
			templateExtensions = new String[]{"html", "hbs"};

        if(purgeWhitespace == null)
            purgeWhitespace = false;

        handlebarsEngine = new HandlebarsEngine(handlebarsName);
        handlebarsEngine.setEncoding(encoding);
        handlebarsEngine.setKnownHelpersOnly(knownHelpersOnly);

        if (knownHelpers != null) {
            getLog().debug("Known helpers " + knownHelpers.toString());
            handlebarsEngine.setKnownHelpers(knownHelpers);
        }

        if (project != null) {
            handlebarsEngine.setCacheDir(
                    new File(project.getBuild().getDirectory(), "handlebars-maven-plugins/script"));
        }
        handlebarsEngine.startup();

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
        handlebarsEngine.precompile(templates, getOutputFile(directory), purgeWhitespace);
	}

	private File getOutputFile(File directory) throws IOException {
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
