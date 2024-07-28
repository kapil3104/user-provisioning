FROM alpine:3.19

ARG version=22.0.2.9.1

# Please note that the THIRD-PARTY-LICENSE could be out of date if the base image has been updated recently.
# The Corretto team will update this file but you may see a few days' delay.
RUN wget -O /THIRD-PARTY-LICENSES-20200824.tar.gz https://corretto.aws/downloads/resources/licenses/alpine/THIRD-PARTY-LICENSES-20200824.tar.gz && \
    echo "82f3e50e71b2aee21321b2b33de372feed5befad6ef2196ddec92311bc09becb  /THIRD-PARTY-LICENSES-20200824.tar.gz" | sha256sum -c - && \
    tar x -ovzf THIRD-PARTY-LICENSES-20200824.tar.gz && \
    rm -rf THIRD-PARTY-LICENSES-20200824.tar.gz && \
    wget -O /etc/apk/keys/amazoncorretto.rsa.pub https://apk.corretto.aws/amazoncorretto.rsa.pub && \
    SHA_SUM="6cfdf08be09f32ca298e2d5bd4a359ee2b275765c09b56d514624bf831eafb91" && \
    echo "${SHA_SUM}  /etc/apk/keys/amazoncorretto.rsa.pub" | sha256sum -c - && \
    echo "https://apk.corretto.aws" >> /etc/apk/repositories && \
    apk add --no-cache amazon-corretto-22=$version-r0 && \
    rm -rf /usr/lib/jvm/java-22-amazon-corretto/lib/src.zip


ENV LANG C.UTF-8

ENV JAVA_HOME=/usr/lib/jvm/default-jvm
ENV PATH=$PATH:/usr/lib/jvm/default-jvm/bin

# Set the working directory
WORKDIR /app

# The application's jar file
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

# Expose the application port (e.g., 8080)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
