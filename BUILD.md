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