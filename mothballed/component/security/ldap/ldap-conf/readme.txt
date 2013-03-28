An LDAP browser can be downloaded from http://jxplorer.org/
Windows port of OpenLDAP is at http://lucas.bergmans.us/hacks/openldap/

Files:

example.ldif - sample file for loading into ldap (using ldapadd)
  usage
  ldapadd -x -D "cn=Manager,dc=<MY-DOMAIN>,dc=<COM>" -W -f example.ldif
  e.g.
  ldapadd -x -D "cn=Manager,dc=isis,dc=org" -W -f example.ldif

slapd.conf - sample configuration for the OpenLDAP server (Windows port)