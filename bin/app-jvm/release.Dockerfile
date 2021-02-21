# FROM ubuntu:20.04

# COPY ./target/app.upx.native /opt/app

# ENTRYPOINT ["/opt/app"]

# FROM adoptopenjdk/openjdk14:jre-14.0.2_12-ubuntu@sha256:318d9082e0514727f5ce7479f7bc6cd9dfc78a04c2a1656d5004bc0e867cde2c
FROM adoptopenjdk/openjdk14-openj9:jre-14.0.2_12_openj9-0.21.0-ubuntu@sha256:a170e21ac6272229d0ac3af45bb12d03060acc7a92f90ae6b76408bc06649d85

COPY ./target/app.standalone.jar /opt/app.standalone.jar

CMD ["java", "-jar", "/opt/app.standalone.jar"]

