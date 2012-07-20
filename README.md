Zeta Code
========================

Go from a 10,000 ft view of the entire project down to any file or method.
Zeta Code is a [ZUI](https://en.wikipedia.org/wiki/Zooming_User_Interface).
Zeta Code is a way to view code as a graph.
It does not mean to replace traditional IDE's, but rather is
a different way to view and manipulate code.
It looks at a project holistically - including imports and dependencies
as top-level objects and breaks out of the ancient code-as-files metaphor.
![https://p.twimg.com/AvwpduGCIAAC0Qa.png:large](https://p.twimg.com/AvwpduGCIAAC0Qa.png:large)

Languages/Frameworks Supported: Java/Maven, Groovy scripting

Features
--------
* right-click anywhere to get the menu
* click on a node to select it.
* ctrl-click on a node to open an editor for it.
* Three different ways of organizing nodes: Bloom, Grid, random
* Choose any directionality: left-right, down, up, right-left
* Time-Travel: gource-like animation of commit history (git)
* Neo-Edit Editor with syntax highlighting and many other features
* Playground: immediately evaluating groovy
* Zoom-in/out using the mouse-wheel

Future Plans
------------
* Support for more languages and frameworks
* Data Visualization: change saturation based on number of callers of a function.
* Tasks/Working-sets: ability to save a group of files and return to them at any time.
* History: Forward/Backward history.
* Plugins: for version-control, compiling, testing, etc. It will be fully extensible.
* Call-heirarchy: see the methods called by current method.
* Refactoring: ability to rename methods, move methods, etc.

Usage
-----

First you need to compile using Maven.

	mvn package

Unzip the zip-with-lib, then you can just run the resulting jar:

	cd target/
	unzip z-0.1-beta-3-zip-with-lib.zip
	java -jar z-0.1-beta-3/z-0.1-beta-3.jar


Known Issues
-------------
This code is still in beta, so use with caution. It is not done.


License
-------

Copyright 2012, Adam L. Davis. All rights reserved.
Made Available for use under a BSD-style license. See LICENSE
