## Build
Build is done via gradle - under Windows with gitbash:

* ./gradlew.bat tasks # list all gradle tasks
* ./gradlew.bat webpack-bundle # create main.bundle.js
* ./gradlew.bat test --exclude-task npm-install
 
Internally gradle uses npm for the JS part.

 npm --verbose 
 
 If task npm-install hangs, try
 
 ./gradlew.bat npm-install --info --debug --stacktrace
 

Helps in identifying thing that may go wrong (eg. due to proxy settings).
 


## Setup
proxy settings:
https://jjasonclark.com/how-to-setup-node-behind-web-proxy/
https://gist.github.com/EudesSilva/0329645b9c258e0495544b8a5ccd1454



# Toolchain
* Apache Gradle 
* Google Chrome (72.0.3626.81)
* Moesif CORS Plugin (for Chrome)

Kotlin/JS uses `Gradle` for the build, for the JS runtime `NodeJS`, and for the JS dependency management part `npm`.

Depending on the network you are in, you may need to configure the proxy settings. Among the relevant files are:
```bash
~/.npmrc
~/.gitconfig
~/.ssh/config
~/.ssh/id_rsa
```
### Access to git from npm
#### Problem
```bash
npm ERR! Error while executing:
npm ERR! C:\Program Files\Git\bin\git.EXE ls-remote -h -t ssh://git@github.com/jarecsni/font-awesome-webpack.git
npm ERR!
npm ERR! git@ssh.github.com: Permission denied (publickey).
npm ERR! fatal: Could not read from remote repository.
npm ERR!
npm ERR! Please make sure you have the correct access rights
npm ERR! and the repository exists.
npm ERR!
npm ERR! exited with error code: 128
```
#### Solution
`~/.ssh/config`
```bash
ProxyCommand /bin/connect.exe -H proxy.server.name:3128 %h %p

Host github.com
  User git
  Port 22
  Hostname github.com
  IdentityFile "C:\users\username\.ssh\id_rsa"
  TCPKeepAlive yes
  IdentitiesOnly yes

Host ssh.github.com
  User git
  Port 443
  Hostname ssh.github.com
  IdentityFile "C:\users\username\.ssh\id_rsa"
  TCPKeepAlive yes
  IdentitiesOnly yes
```

#### Corporate Firewall with SSL 'inspection'
There are some questionable setups in coporate settings that are based on SSL replacement.
In order to cope with it, you may try to import the Certificate into cacerts, 
see https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000094584-IDEA-Ultimate-2016-3-4-throwing-unable-to-find-valid-certification-path-to-requested-target-when-trying-to-refresh-gradle

### Karma-Tests do not respond to code changes 

Windows:
```
taskkill /f /im node.exe 
```

Linux:
```
killall node 
```
