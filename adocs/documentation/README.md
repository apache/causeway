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


One-time setup
--------------

The generated site is published by copying into the `content/` directory of the (new) [isis-site git repo](https://git-wip-us.apache.org/repos/asf/isis-site.git).  You therefore need to check this out.

The scripts assume that the two git repos (for [isis](https://git-wip-us.apache.org/repos/asf/isis.git) itself and for isis-site) are checked out into the same parent directory, eg:

    /APACHE/
        isis/                       # checkout of isis.git
            adocs/
                documentation/  
                    README.md       # this file you are reading right now
                    ...
        isis-site/                  # checkout of isis-site.git
            content/                # the published website
            
If this isn't the case, then it is possible to override the relative directory by passing in a system property to the mvn goal; see below.


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

To (re)build the documentation locally prior to release, use:

    mvn clean compile

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

> The generated files are self-contained HTML files, so in many cases you can also preview just by loading from the `file://` system.


Publish procedure
-----------------

> This section assuming you have checked out the `isis.git` and `isis-site.git` repos as discussed in "One-time setup", above.  If the `isis-site.git` repo is checked out to some other directory, then override the default using `-Disis-site.dir=...`

First, BE AWARE that ASF's publishing script work from the 'asf-site' branch, NOT from the 'master' branch.  Therefore, in the `isis.git` repo site:
 
    git checkout asf-site

Back in the main `isis-git.repo`... to copy the generated documents to the `isis-site.git` repo , run: 

    mvn clean package

To copy and also commit the generated documents to the `isis-site.git` repo , run:
 
    mvn clean install

To specify a commit message, use:
 
    mvn clean install -Dmessage="ISIS-nnnn: a custom commit message"

Pushing the commits (in the `isis-site.git` directory, of course) will publishing the changes:
 
    git push

Double check at [isis.apache.org](http://isis.apache.org).

