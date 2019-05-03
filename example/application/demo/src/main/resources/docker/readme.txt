Each sub-folder contains a Dockerfile where by convention the folder name 
corresponds to the docker image tag used.

Also we use different profiles in the project's pom.xml using the same naming convention: 

<profile>
    <id>flavor-tomcat</id>
    <activation>
        <property>
            <name>flavor-tomcat</name>
        </property>
    </activation>
    
    ...
    
</profile> 