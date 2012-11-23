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
:net.unit8.maven.plugins:handlebars-maven-plugin:0.1.2:precompile

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
