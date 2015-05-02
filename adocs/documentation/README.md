title: Apache Isis

Notice:    Licensed to the Apache Software Foundation (ASF) under one
           or more contributor license agreements.  See the NOTICE file
           distributed with this work for additional information
           regarding copyright ownership.  The ASF licenses this file
           to you under the Apache License, Version 2.0 (the
           "License"); you may not use this file except in compliance
           with the License.  You may obtain a copy of the License at
           .
             http://www.apache.org/licenses/LICENSE-2.0
           .
           Unless required by applicable law or agreed to in writing,
           software distributed under the License is distributed on an
           "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
           KIND, either express or implied.  See the License for the
           specific language governing permissions and limitations
           under the License.

Isis documentation
-------------------------

Isis documentation uses [Asciidoc](http://www.methods.co.nz/asciidoc/). You're welcome to contribute.

License
-------
Apache Isis is licensed under ALv2.
See the LICENSE file for the full license text.

One time configuration
-----------------

Put the following information in your ~/.m2/settings.xml file

    <server>
      <id>isis-site</id>
      <username><YOUR_USERNAME></username>
      <password><YOUR_PASSWORD></password>
    </server>


Naming Conventions
------------------

For documents with inclusions, use '_' to separate out the logical hierarchy:

    xxx-xxx/xxx-xxx.adoc
            _xxx-xxx_ppp-ppp.adoc
            _xxx-xxx_qqq-qqq.adoc
            _xxx-xxx_qqq-qqq_mmm-mmm.adoc
            _xxx-xxx_qqq-qqq_nnn-nnn.adoc

Any referenced images should be in subdirectories of the `images` directory: 

    xxx-xxx/images/.
                  /ppp-ppp/.
                  /qqq-qqq/.
                          /mmm-mmm
                          /nnn-nnn

And similarly any resources should be in the `resources` subdirectory:

    xxx-xxx/resources/.
                      ppp-ppp/.
                      qqq-qqq/.
                             /mmm-mmm/
                             /nnn-nnn/


Build and Review (using Maven)
-----------------------

To build the documentation locally prior to release, use:

    mvn site

The site will be generated at `target/site/index.html`.

You could then use a web server such as Python's SimpleHTTPServer to preview (so that all Javascript works correctly).  However, instead we recommend using instant preview, described next.


Instant Rebuild (using Ruby)
---------------

The ruby script, `monitor.rb` emulates the `mvn site` command, regenerating any changed Asciidoctor files to the relevant `target/site` directory.  If any included files are changed then it rebuilds the parent (per the above naming convention).   

To setup:

* download and install ruby 2.0.0, from [http://rubyinstaller.org/downloads/](rubyinstaller.org/downloads)
* download devkit for the Ruby 2.0 installation, also from [http://rubyinstaller.org/downloads/](rubyinstaller.org/downloads).  Then follow the [https://github.com/oneclick/rubyinstaller/wiki/Development-Kit](installation instructions) on their wiki

> Note the wdm gem (required to monitor the filesystem if running on Windows) is not currently compatible with Ruby 2.1.

install:

    gem install bundler
    bundle install

to run, typically just use:

    ruby monitor.rb

This will start monitoring all files under `src/main/asciidoc`, and start up a web server on port 4000 so you can review results.

There are several other options, use `-h` flag for usage:

    ruby monitor.rb -h

which should print:

    usage: monitor.rb [options]
        -a, --all        process all files
        -x, --nomonitor  do not monitor, just process all files then exit
        -p, --port       port (default: 4000)
        -b, --browser    launch browser
        -h, --help       help

So, for example

    ruby monitor.rb -a -b -p 9090
    
will process all files before starting monitoring, start the web browser on port 9000, and will also automatically open up your default web browser at that port.



Publish procedure
-----------------

To publish the documentation to the [ASF staging server](http://isis.staging.apache.org/docs), run:

    mvn clean site-deploy

Then log in to <https://cms.apache.org/isis/publish> and click on the `Submit` button.


Preview procedure
-----------------
Note that it is also possible to push to a ["preview" area](http://isis.staging.apache.org/preview/docs) on the staging server;
in general you can skip this unless you want to double-check your edits first:

    mvn clean site-deploy -Ppreview

