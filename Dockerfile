#FROM java:8
#
#RUN mkdir -p /usr/src/service
#WORKDIR /usr/src/service
#
#CMD ./gradlew bootRun

FROM java:8
EXPOSE 8080
ADD /build/libs/deeppocket-1.0.0-SNAPSHOT.jar deeppocket.jar
ENTRYPOINT ["java","-jar", "deeppocket.jar"]