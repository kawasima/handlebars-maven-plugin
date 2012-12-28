handlebars-maven-plugin
=======================

A maven plugin for precompiling handlebars templates

Introduction
------------

handlebars-maven-plugin is used to precompile the handlebars templates of your project.

Goals
-----

Goal                 |Description
---------------------|-------------------------------
handlebars:precompile|Precompile handlebars templates

### handlebars:precompile

Full name
:net.unit8.maven.plugins:handlebars-maven-plugin:0.2.0-SNAPSHOT:precompile

Description
:precomiple handlebars templates

#### Optional parameters

Name              |Type    |Description
------------------|--------|--------------------------------------
sourceDirectory   |String  |The directory of handlebars templates
outputDirectory   |String  |The directory of precompiled templates
preserveHierarchy |Boolean |true if preserve the hierarchy of source directories.
encoding          |String  |charset of template files.
templateExtensions|String[]|The extensions of handlebars templates
handlebarsName    |String  |The handlebars filename using by precompile. Default value is "handlebars-1.0.rc.1.min.js". If you want to use an older version, add this parameter (e.g. handlebars-1.0.0.beta.5.js ). And this plugin will fetch the version from GitHub.
