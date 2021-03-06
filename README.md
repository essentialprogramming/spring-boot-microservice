# Spring Boot Microservice

This project is inspired by the idea to quickly create a production ready project with all the required infrastructure at low cost yet with important security measures in place and ability to quickly scale in order to ship a quality product to early adopters. Ideal for quickly starting an app to validate ideas and scale if needed. The implementation includes code samples for different features. It uses Spring Boot as the framework of choice because it provides a nice set of convenience features when bootstrapping and plugging together the application. Spring Boot has taken Spring framework to the next level, by drastically reducing the configuration and setup time required for Spring projects. 

### 🌀 Prerequisites:
By default, the project uses JDK 8, but will also work with JDK 11 and above.

* **JDK**
  - Oracle
    - Java : http://www.oracle.com/technetwork/java/javase/downloads/index.html
  - Adoptium
    - Java : https://adoptium.net/temurin/releases
 
  - RedHat
    - Java : https://developers.redhat.com/products/openjdk/download
* [Maven](https://maven.apache.org/)
---------------
### 🌀 Build and run

Build
---------------
* Get the latest version from the git repository.
* Run ` mvn clean install` to build the project.


Run
---------------
#### 1. Using IntelliJ
Start the Server using  " Run `Server.main()` " command, selected from the dialog after right click on:
- Main Application class: `com.server.Server`

#### 2. Using CLI
To run the application from command line do following steps
- open `cmd` window
- change directory to the root of your microservice project
- run `mvn clean install` to create a jar-file of your microservice.
- call `java -jar essentialprogramming-api/target/uber-essentialprogramming-api-1.0.0-SNAPSHOT` from the console

Visit `localhost:8080/apidoc` to see the endpoints.

---------------
### 🌀 DB Migration
- change directory to 'db-migration'
- run
`mvn compile flyway:baseline; `
`mvn compile flyway:migrate; `

### 🌀 Developer Setup
#### Enable lombok

- https://projectlombok.org/setup/intellij
- Verify that annotation processing is enabled in Intellij (`File` -> `Settings` -> `Build, Execution, and Deployment`
  -> `Compiler` -> `Annotation Processers`)
  
### 🌀 Usefull commands
    `mvn dependency:tree`
 
    `mvn dependency:tree -Dincludes=net.minidev:json-smart` //Filter the dependency tree
