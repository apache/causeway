FROM incodehq/tomcat
MAINTAINER ${docker-plugin.maintainer}
RUN rm -rf ${DEPLOYMENT_DIR}/ROOT
COPY ${docker-plugin.resource.include} ${DEPLOYMENT_DIR}/ROOT.war
EXPOSE 8080