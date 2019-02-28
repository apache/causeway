# Toolchain Requirements
* (Oracle) JDK 1.8_181 (or higher)
* Apache Maven 3.6.0


# Create a Sample Backend 
Create from the Apache Isis SimpleApp archetype:
```bash
mvn archetype:generate \
    -D archetypeGroupId=org.apache.isis.archetype \
    -D archetypeArtifactId=simpleapp-archetype \
    -D archetypeVersion=2.0.0-M2 \
    -D groupId=org.my \ 
    -D artifactId=myapp-2.0.0-M2 \ 
    -D version=1.0.0 -B
```
Build via
```bash
mvn clean install -DskipTests
```


# Dealing with CORS

Nicely done introduction: 
* https://www.moesif.com/blog/technical/cors/Authoritative-Guide-to-CORS-Cross-Origin-Resource-Sharing-for-REST-APIs/#how-is-origin-definedhttps://www.moesif.com/blog/technical/cors/Authoritative-Guide-to-CORS-Cross-Origin-Resource-Sharing-for-REST-APIs/#how-is-origin-defined

##Amend web.xml 

Add to webapp\src\main\webapp\WEB-INF\web.xml

```xml
	 	<!-- CORS filter for XmlHttpRequests -->
	<filter>
		<filter-name>cross-origin</filter-name>
		<filter-class>org.eclipse.jetty.servlets.CrossOriginFilter</filter-class>
		<init-param>
			<param-name>allowedOrigins</param-name>
			<param-value>*</param-value>
		</init-param>
		<init-param>
			<param-name>allowedMethods</param-name>
			<value>*</value>
		</init-param>
		<init-param>
			<param-name>allowedHeaders</param-name>
			<param-value>*</param-value>
		</init-param>
		<init-param>
			<param-name>supportsCredentials</param-name>
			<param-value>true</param-value>
		</init-param>
		<init-param>
			<param-name>chainPreflight</param-name>
			<param-value>false</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>cross-origin</filter-name>
		<url-pattern>/restful/*</url-pattern>
	</filter-mapping>
```

##Put into webapp/src/main/webapp/WEB-INF/lib
* https://search.maven.org/artifact/org.eclipse.jetty/jetty-util/9.4.12.v20180830/jar
* https://search.maven.org/artifact/org.eclipse.jetty/jetty-servlets/9.4.12.v20180830/jar

## Start the Backend
```bash
cd webapp
mvn jetty:run -Djetty.port=8080
``` 