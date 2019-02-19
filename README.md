# Driftlog
A log reader built into a Docker image. The reader is a web service, exposed with a REST API. It provides access to host machine log files and Docker container logs.

The project was built with Java, Spring Boot, and Gradle.

* [Docker Hub](https://hub.docker.com/r/seandougnelson/driftlog)
* User Manual

## Reflection
The goal of this project was to create a simple log reader to demonstrate my knowledge of Docker, Java, and Spring Boot. Interacting with Docker was straightforward, thanks to Spotify's [Java Docker Client](https://github.com/spotify/docker-client) and Benjamin Muschko's [Gradle Docker Plugin](https://github.com/bmuschko/gradle-docker-plugin).

I'm familiar with Spring Boot but there were some firsts in this project. I had never created tests using Spring's MockMvc. It made testing REST API resources a breeze. Instead of starting a server, MockMvc hands off HTTP requests right to the controller. Additionally, I had never added HTTPS support to a Spring project. It was just a matter of creating a profile which defines SSL variables and setting that profile during runtime.

This was also my first time creating a Java project with IntelliJ IDEA. I've always used Eclipse but wanted to try out alternatives. There were some growing pains but, those aside, I'm extremely pleased with IntelliJ. I've decided to use it for all future personal Java projects. Little things made it a delight to use, like built-in Gradle integration instead of having to deal with Eclipse's Buildship plugin.
