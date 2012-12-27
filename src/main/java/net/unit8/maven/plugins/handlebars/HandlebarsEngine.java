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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import sun.net.www.protocol.file.FileURLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handlebars script engine.
 *
 * @author kawasima
 */
public class HandlebarsEngine {
    private File cacheDir;
    private static final URI handlebarsDownloadsUri;
    static {
        try {
            handlebarsDownloadsUri = new URI("https://api.github.com/repos/wycats/handlebars.js/downloads");
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException("GitHub url is invalid?");
        }
    }

    protected HandlebarsEngine() { }

    public HandlebarsEngine(String handlebarsFile) throws MojoExecutionException {
        URL uri = getClass().getClassLoader().getResource("script/" + handlebarsFile);
        if (uri == null) {
            if (new File(cacheDir, handlebarsFile).exists()) {

            } else {
                fetchHandlebars(handlebarsFile);
            }
        } else {

        }
    }

    protected void fetchHandlebars(String handlebarsFile) throws MojoExecutionException {
        String downloadUrl = null;
        URLConnection conn = null;
        try {
            conn = handlebarsDownloadsUri.toURL().openConnection();
            List<GitHubDownloadDto> githubDownloadDtoList = JSON.decode(conn.getInputStream(),
                    (new ArrayList<GitHubDownloadDto>() {}).getClass().getGenericSuperclass());
            for (GitHubDownloadDto githubDownloadDto : githubDownloadDtoList) {
                if (StringUtils.equals(githubDownloadDto.getName(), handlebarsFile)) {
                    downloadUrl = githubDownloadDto.getHtmlUrl();
                }
            }
        } catch(Exception e) {
             throw new MojoExecutionException("Failure fetch handlebars.", e);
        } finally {
            if (conn != null) {
                ((HttpsURLConnection) conn).disconnect();
            }
        }

        conn = null;
        try {
            if (!cacheDir.exists()) {
                FileUtils.forceMkdir(cacheDir);
            }
            conn = new URL(downloadUrl).openConnection();
            IOUtils.copy(conn.getInputStream(), new FileOutputStream(new File(cacheDir, handlebarsFile)));
        } catch(Exception e) {
            throw new MojoExecutionException("Failure fetch handlebars.", e);
        } finally {
            if (conn != null) {
                ((HttpsURLConnection) conn).disconnect();
            }
        }
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }
}
