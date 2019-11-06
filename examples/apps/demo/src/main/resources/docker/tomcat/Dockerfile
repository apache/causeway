FROM tomcat:9-jdk11-openjdk
 
LABEL maintainer="ahuber@apache.org"

ENV DEPLOY_DIR /usr/local/tomcat/webapps/

RUN rm -rf ${DEPLOY_DIR}/ROOT
COPY ./*.war ${DEPLOY_DIR}/ROOT.war

EXPOSE 8080