Z (zeta)
========================

Go from a 10,000 view of the entire project down to any file simply by zooming.

Languages/Frameworks Supported: Java/Maven, Ruby/Gems

* Java/Maven: Tested on itself
* Ruby/Gems: Tested on the Rails project

Features
--------
* Three different ways of organizing nodes: Bloom, Grid, random
* Zoom-in/out using the mouse-wheel
* right-click anywhere to get the menu
* click on a node to select it.
* ctrl+right-click on a node to edit it.

Future Plans
------------
* Tasks/Working-sets: ability to save a group of files and return to them at any time.
* History: Forward/Backward history.
* Plugins: for git, compiling, testing, etc.
* Docs: ability to quickly see code-documentation by hovering, etc.
* Call-heirarchy: see the methods called by current method.
* Refactoring: ability to rename methods, move methods, etc.

Usage
-----

First you need to compile using Maven.

	mvn package

Then you can just run the resulting jar:

	java -jar z-0.1-beta-1.jar


Known Issues
-------------
This code is still in beta, so use with caution. The code parsers will mess up sometimes.


License
-------

Copyright 2012, Adam L. Davis. All rights reserved.
Made Available for use under a BSD-style license. See LICENSE
