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

import net.arnx.jsonic.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handlebars script engine.
 *
 * @author kawasima
 */
public class HandlebarsEngine {
    /** The cache directory of handlebars script*/
    private File cacheDir;

    /** The encoding of handlebars templates */
    private String encoding;

    /** The name of handlebars script */
    private String handlebarsName;

    /** The url of handlebars script */
    private URL handlebarsUrl;

    private static final URI handlebarsDownloadsUri;
    private static final Log LOG = new SystemStreamLog();
    private List<String> knownHelpers = Collections.emptyList();
    private Boolean knownHelpersOnly;

    static {
        try {
            handlebarsDownloadsUri = new URI("https://api.github.com/repos/wycats/handlebars.js/downloads");
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException("GitHub url is invalid?");
        }
    }

    public HandlebarsEngine(String handlebarsName) throws MojoExecutionException {
        this.handlebarsName = handlebarsName;
    }

    public void startup() throws MojoExecutionException {
        handlebarsUrl = getClass().getClassLoader().getResource("script/" + handlebarsName);
        if (handlebarsUrl == null) {
            File cacheFile = new File(cacheDir, handlebarsName);
            if (!cacheFile.exists()) {
                fetchHandlebars(handlebarsName);
            }
            try {
                handlebarsUrl = cacheFile.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Invalid handlebars cache file.", e);
            }
        }
    }

    public void precompile(Collection<File> templates, File outputFile, boolean purgeWhitespace) throws IOException {
        Context cx = Context.enter();
        PrintWriter out = null;
        LOG.info("precompile " + templates + " to " + outputFile);
        try {
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encoding));
            out.print("(function() {\n  var template = Handlebars.template, "
                    + "templates = Handlebars.templates = Handlebars.templates || {};\n");
            // Rhino for Handlebars Template
            ScriptableObject global = cx.initStandardObjects();
            InputStreamReader in = new InputStreamReader(handlebarsUrl.openStream());
            cx.evaluateReader(global, in, handlebarsName, 1, null);
            IOUtils.closeQuietly(in);

            Scriptable options = new Options(new KnownHelpers(knownHelpers), knownHelpersOnly);
            ScriptableObject.putProperty(global, "options", options);

            for (File template : templates) {
                String data = FileUtils.readFileToString(template, encoding);

                if (purgeWhitespace)
                    data = StringUtils.replaceEach(data, new String[]{"\n", "\r", "\t"}, new String[]{"", "", ""});

                ScriptableObject.putProperty(global, "data", data);

                Object obj = cx.evaluateString(global, "Handlebars.precompile(String(data), options);", "<cmd>", 1, null);
                out.println("templates['" + FilenameUtils.getBaseName(template.getName()) + "']=template(" + obj.toString() + ");");
            }
        } finally {
            Context.exit();
            if (out != null)
                out.println("})();");
            IOUtils.closeQuietly(out);
        }

    }

    protected void fetchHandlebars(String handlebarsName) throws MojoExecutionException {
        String downloadUrl = null;
        URLConnection conn = null;
        try {
            conn = handlebarsDownloadsUri.toURL().openConnection();
            List<GitHubDownloadDto> githubDownloadDtoList = JSON.decode(conn.getInputStream(),
                    (new ArrayList<GitHubDownloadDto>() {}).getClass().getGenericSuperclass());
            for (GitHubDownloadDto githubDownloadDto : githubDownloadDtoList) {
                if (StringUtils.equals(githubDownloadDto.getName(), handlebarsName)) {
                    downloadUrl = githubDownloadDto.getHtmlUrl();
                }
            }
        } catch(Exception e) {
             throw new MojoExecutionException("Failure fetch handlebars.", e);
        } finally {
            if (conn != null) {
                ((HttpURLConnection) conn).disconnect();
            }
        }

        conn = null;
        try {
            if (!cacheDir.exists()) {
                FileUtils.forceMkdir(cacheDir);
            }
            conn = new URL(downloadUrl).openConnection();
            if (((HttpURLConnection) conn).getResponseCode() == 302) {
                String location = conn.getHeaderField("Location");
                ((HttpURLConnection) conn).disconnect();
                conn = new URL(location).openConnection();
            }
            LOG.info("Fetch handlebars.js from GitHub ("+ conn.getURL() +")");
            IOUtils.copy(conn.getInputStream(), new FileOutputStream(new File(cacheDir, handlebarsName)));
        } catch(Exception e) {
            throw new MojoExecutionException("Failure fetch handlebars.", e);
        } finally {
            if (conn != null) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setKnownHelpers(List<String> knownHelpers) {
        this.knownHelpers = knownHelpers;
    }


    public void setKnownHelpersOnly(Boolean knownHelpersOnly) {
        this.knownHelpersOnly = knownHelpersOnly;
    }
}
