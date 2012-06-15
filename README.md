Zeta Code
========================

Go from a 10,000 ft view of the entire project down to any file simply by zooming.

Languages/Frameworks Supported: Java/Maven, Ruby/Gems

* Java/Maven: Tested on itself
* Ruby/Gems: Tested on the Rails project

Features
--------
* right-click anywhere to get the menu
* click on a node to select it.
* ctrl-click on a node to open an editor for it.
* Three different ways of organizing nodes: Bloom, Grid, random
* Choose any directionality: left-right, down, up, right-left
* Time-Travel: gource-like animation of commit history (git)
* Neo-Edit Editor with syntax highlighting and many other features
* Press F6 in editor for runtime-debugging

Future Plans
------------
* Support for more languages and frameworks
* Zoom-in/out using the mouse-wheel
* Tasks/Working-sets: ability to save a group of files and return to them at any time.
* Dependencies: Easily manage your dependencies. See which code is using what, and easily search for existing open-source software.
* History: Forward/Backward history.
* Testing: Integration with modern testing frameworks like JUnit, TestNG, and spock.
* Plugins: for version-control, compiling, testing, etc. It will be fully extensible.
* Docs: ability to quickly see code-documentation by hovering, etc.
* Call-heirarchy: see the methods called by current method.
* Refactoring: ability to rename methods, move methods, etc.
* Preview: show real values in real-time ("coding without blinders")

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
