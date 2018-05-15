Alarm Service 
==============

![image](https://user-images.githubusercontent.com/20153890/40037190-cfd1ca38-5846-11e8-8443-a00a08a57fb5.png)

> **NOTE** 
> - 여기서는 MSA에서의 Service중 하나인 Alarm Service를 구축하여 본다.
> - Service는 API Server로 구성된다.
> - Alarm Service는 Spring boot Project로 구현된다.
> - 기존 Spring에서는 Maven, Gradle등의 dependency tool을 이용해 WAR파일을 생성한 후 tomcat같은 WAS에 배포하여
웹 어플리케이션을 구동하였으나, Spring boot는 JAR파일에 내장 tomcat이 존재하여, 단순히 JAR파일을 빌드하고 실행하는 것 만으로 웹 어플리케이션 구동이 가능하다.
> - API Server를 구축하고, 생성된 JAR파일을 Docker container로 띄워 서비스한다. 
> - JPA repository로 DB에 접근한다.

**Service는 "Service Register & Discovery" Server인 Eureka Server의 Client이다.**



## 1. REST API  ##

METHOD | PATH | DESCRIPTION 
------------|-----|------------
GET | /notice/ | 전체 알림정보 제공
GET | /notice/{receiver_ID} | 해당 receiver 에 대한 알림 정보 제공
GET | /notice/latest/{receiver_ID} | 해당 receiver 에 대한 최근 10개 알림정보 제공
GET | /notice/previous/{receiver_ID}/{id} | 해당 receiver 에 대한 정보들 중 {id}값을 기준으로 이전 10개 정보 제공
POST | /notice/ | 알림 정보 입력



## 2. Project directory tree


    .
    ├── AlarmServiceApplication.java
    ├── domain                    
    │   ├── Notice.java           
    ├── repository                        
    │   ├── NoticeRepository.java         
    ├── rest   
    │   ├── NoticeController.java         
    ├── service
        ├── NoticeService.java          
        └── NoticeServiceImpl.java      
    
    
    
## 3. dependency ##



1. pom.xml에 eureka-client, hystrix, jpa, mysql-connector-java 추가 


**pom.xml**


```xml

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
            <version>1.4.4.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-data-jpa</artifactId>
             <version>2.0.1.RELEASE</version>
        </dependency>

        <dependency>
              <groupId>mysql</groupId>
              <artifactId>mysql-connector-java</artifactId>
              <version>5.1.21</version>
        </dependency>
```
2. dependency Management 추가 

```xml
        <dependencyManagement>
                <dependencies>
                        <dependency>
                                <groupId>org.springframework.cloud</groupId>
                                <artifactId>spring-cloud-dependencies</artifactId>
                                <version>${spring-cloud.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                        </dependency>
                </dependencies>
        </dependencyManagement>
```

2. docker build를 위한 dockerfile-maven-plugin, repository 추가 

```xml

        <build>
                <plugins>
                        <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                        </plugin>

            <plugin>
                <groupId>com.spotify</groupId>
                <artifactId>dockerfile-maven-plugin</artifactId>
                <version>1.3.6</version>
                <configuration>
                    <repository>${docker.image.prefix}/${project.artifactId}</repository>
                                <buildArgs>
                                        <JAR_FILE>target/${project.build.finalName}.jar</JAR_FILE>
                                </buildArgs>
                </configuration>
            </plugin>

                        </plugins>
                </build>

        <repositories>
                <repository>
                        <id>spring-milestones</id>
                        <name>Spring Milestones</name>
                        <url>https://repo.spring.io/milestone</url>
                        <snapshots>
                                <enabled>false</enabled>
                        </snapshots>
                </repository>
        </repositories>
```

## 4. resources ##

bootstrap.yml file은 Spring cloud application에서 apllication.yml보다 먼저 실행된다. bootstrap.yml에서 db connection을 진행하고, apllication.yml에서 applicaion의 port와 eureka server instance의 정보를 포함시킨다.

**1. bootstrap.yml**

```xml
spring:
    application:
        name: Alarm-service

    jpa:
      hibernate:
        ddl-auto: update
        show_sql: true
        use_sql_comments: true
        fotmat_sql: true

    datasource:
      url: jdbc:mysql://127.0.0.1:3306/notice
      username: sangmin
      password: tkdals12
      driver-class-name: com.mysql.jdbc.Driver
      hikari:
        maximum-pool-size: 2

    cloud:
        config:
            uri: ${CONFIG_SERVER_URL:http://127.0.0.1:8888}
```

**2. application.yml**

```xml

server:
    port: 8763

eureka:
  client:
    healthcheck: true
    fetch-registry: true
    serviceUrl:
      defaultZone: ${vcap.services.eureka-service.credentials.uri:http://192.168.10.168:8761}/eureka/
    instance:
      statusPageUrlPath: https://${eureka.hostname}/info
      healthCheckUrlPath: https://${eureka.hostname}/health
      homePageUrl: https://${eurkea.hostname}/
    preferIpAddress: true
```

