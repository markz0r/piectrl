FROM alpine:latest
MAINTAINER Mark Culhane <mark.culhane@assetowl.com>

RUN apk --update add openjdk8-jre
ADD target/piectrl.jar piectrl.jar
#ENV PI_INFO="{:url \"http://192.168.0.10:8000/\" :user \"myuser\" :password \"mypass\" :GPIOs [17, 18]}"

EXPOSE 3000
CMD ["java","-jar","/piectrl.jar"]
