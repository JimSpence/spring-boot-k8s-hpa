#Docker base image : Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine

#Check the java version
RUN ["java", "-version"]

#Install maven
RUN apt-get update
RUN apt-get install -y maven

#Set the working directory for RUN and ADD commands
WORKDIR /code

#Copy the SRC, LIB and pom.xml to WORKDIR
ADD pom.xml /code/pom.xml
ADD lib /code/lib
ADD src /code/src

#Build the code
RUN ["mvn", "clean"]
RUN ["mvn", "install"]

#Optional you can include commands to run test cases.

#Port the container listens on
EXPOSE 8081

#CMD to be executed when docker is run.
ENTRYPOINT ["java","-jar","target/recruitment-service-0.0.1.jar"]