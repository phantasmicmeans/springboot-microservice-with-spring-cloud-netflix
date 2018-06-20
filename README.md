Spring Boot Microservice with Spring Cloud Netflix
==============

*by S.M.Lee*

![image](https://user-images.githubusercontent.com/20153890/41583901-d1e8aa5c-73e0-11e8-97ff-188fed3cd715.png)


> **NOTE** 
&nbsp;

> - 여기서는 MSA에서의 Service중 하나인 Notice Service를 구축하여 본다.
> - Notice Service는 간단한 REST API Server로 구성되고, Spring Cloud Netflix의 여러 component들을 활용한다. 
> - Notice Service는 Spring boot Project로 구현된다. 생성된 JAR파일을 Docker container로 띄워 서비스한다.
> - 기존 Spring에서는 Maven, Gradle등의 dependency tool을 이용해 WAR파일을 생성한 후 tomcat같은 WAS에 배포하여
웹 어플리케이션을 구동하였으나, Spring boot는 JAR파일에 내장 tomcat이 존재하여, 단순히 JAR파일을 빌드하고 실행하는 것 만으로 웹 어플리케이션 구동이 가능하다.
> - JPA repository로 DB(MySQL 5.6)에 접근한다.

&nbsp;
&nbsp;

## Service Description ##



**Project directory tree**

    .
    ├── Dockerfile
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml
    ├── src/main/java/com/example/demo
    |        |                      ├── AlarmServiceApplication.java   
    |        |                      ├── domain                    
    |        |                      |       └── Notice.java           
    |        |                      ├── repository                        
    |        |                      │       └── NoticeRepository.java         
    |        |                      ├── rest   
    |        |                      │       └──  NoticeController.java         
    |        |                      ├── service
    |        |                               ├── NoticeService.java          
    |        |                               └── NoticeServiceImpl.java      
    │        └── resources
    │           ├── application.yml
    │           └── bootstrap.yml
    └── target
          ├── classes
          ├── notice-service-0.1.0.jar
          ├── notice-service.0.1.0.jar.original
          ├── generated-sources ...


&nbsp;

**Service는 "Service Register & Discovery" Server인 Eureka Server의 Client이다.**


진행하기에 앞서 Eureka에 대한 이해가 필요하다. Hystrix는 다음 장에서 다룰 예정이지만, Eureka에 대한 이해는 필수적이다.
하지만 단순히 REST API Server 구축이 목표라면 스킵하고 진행해도 된다.

> - *Netflix의 Eureka에 대한 이해 => https://github.com/phantasmicmeans/Spring-Cloud-Netflix-Eureka-Tutorial/*
> - *Hystrix에 대한 이해  => https://github.com/phantasmicmeans/Spring-Cloud-Netflix-Hystrix/*
> - *Service Registration and Discovery => https://spring.io/guides/gs/service-registration-and-discovery/*
> - *Service Discovery: Eureka Clients =>https://cloud.spring.io/spring-cloud-netflix/multi/multi__service_discovery_eureka_clients.html*

위 reference를 모두 읽고 이 튜토리얼을 진행하면 순탄하게 진행할 수 있을 것이다.

&nbsp;

어쨌든 Eureka Client로 만들어진 Microservice는 Eureka Server(Registry)에 자신의 meta-data(host,port,address 등)를 전송한다. 이로인해 Eureka Client들은 Eureka Registry 정보를 이용해 서로간의 Communication이 가능하다.  

그리고 Eureka Client는 자신이 살아 있음을 알리는 hearbeat를 Eureka Server에 보낸다. Eureka Server는 일정한 시간안에 hearbeat를 받지 못하면 Registry로 부터 Client의 정보를 제거한다.

Eureka Client는 Registry에 자신의 hostname을 등록하게 되는데 이는 DNS 역할을 하며, 추후에 Netflix의 API Gateway에서 Ribbon + Hystrix + Eureka 조합을 적절히 활용하여 편하게 Dynamic Routing 시킬 수 있다. 

큰 개념은 이정도로 이해하고 일단 Server를 구축하고 Eureka Client로 만들어보자.

&nbsp;
&nbsp;
    
## 1. Dependency ##

Eureka Client로 service를 만들기 위해 spring-cloud-starter-netflix-eureka-client dependency를 추가한다. 그리고 hystrix 적용을 위해 hystrix dependency를 추가한다. 그리고 dockerfile-maven-plugin 또한 추가한다. 


**pom.xml**


```xml

 	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.0.1.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target> 
        <spring-cloud.version>Finchley.M9</spring-cloud.version>
        <docker.image.prefix>phantasmicmeans</docker.image.prefix>
	</properties>

	<dependencies>
    		<dependency>
        		<groupId>org.springframework.cloud</groupId>
            		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
       		</dependency>
        	<dependency>
        		<groupId>org.springframework.cloud</groupId>
            		<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
           		<!--           <version>1.4.4.RELEASE</version> -->
        	</dependency>
        	<dependency>
            		<groupId>org.springframework.boot</groupId>
            		<artifactId>spring-boot-starter-actuator</artifactId>
        	</dependency>
		<dependency>
    			<groupId>org.springframework.boot</groupId>
            		<artifactId>spring-boot-starter-web</artifactId>
            		<!--    <version>1.4.0.RELEASE</version> -->
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
        	<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
    	</dependencies>

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

&nbsp;

## 2. Configuration ##

bootstrap.yml file은 Spring cloud application에서 apllication.yml보다 먼저 실행된다. bootstrap.yml에서 db connection을 진행하고, apllication.yml에서 applicaion의 port와 eureka server instance의 정보를 포함시킨다.

**1. bootstrap.yml**

```xml
spring:
    application:
        name: notice-service

    jpa:
      hibernate:
        ddl-auto: update
        show_sql: true
        use_sql_comments: true
        fotmat_sql: true

    datasource:
      url: jdbc:mysql://{Your_MYSQL_Server_Address}:3306/notice
      username: {MYSQL_ID}
      password: {MYSQL_PASSWORD}
      driver-class-name: com.mysql.jdbc.Driver
      hikari:
        maximum-pool-size: 2

```

사용중인 MySQL Server Address를 spring.datasource.url 부분에 입력해야한다. 또한 username과 password도 추가한다.

**2. application.yml**

```xml

server:
    port: 8763

eureka:
    client:
        healthcheck: true
        fetch-registry: true
        serviceUrl:
            defaultZone: ${vcap.services.eureka-service.credentials.uri:http://{Your-Eureka-server-Address}:8761}/eureka/
    instance:
        preferIpAddress: true
```

eureka.client.serviceUrl.defaultZone에 다음처럼 Eureka Server Address를 추가한다.

* eureak.client.fetch-registry - Eureka Registry로 부터 Registry에 속해 있는 Eureka Client들의 정보를 가져올 수 있는 옵션이다. 이는 true로 주자!
* eureka.client.serviceUrl.defaultZone - Spring Cloud Netflix의 공식 Document에서는 "defaultZone" is a magic string fallback value that provides the service URL for any client that does not express a preference (in other words, it is a useful default).  라고 소개한다. 뭐 일단 이대로 진행하면 된다. 
* eureka.instance.preferIpAddress - Eureka Client가 Eureka Registry에 자신을 등록할 때 eureka.instance.hostname으로 등록하게 된다. 그러나 어떠한 경우에는 hostname보다 IP Address가 필요한 경우가 있다. 여기서는 IP Address를 이용할 것이다. 
* eureka.instance.hostname - JAVA단에서 hostname을 찾지 못하면 IP Address로 Eureka Registry에 전송된다. (이를 방지하려면 eureka.instance.hostname={your_hostname} 으로 원하는 hostname을 입력해도 되고, eureka.instance.hostname=${HOST_NAME} 으로 environment variable을 이용해 run-time때 hostname을 지정해줘도 된다.)
* eureka.instance.instanceId - 위의 예시에서는 instanceId를 등록하지 않는다. default는 ${spring.cloud.client.hostname}:${spring.application.name}:${spring.application.instance_id:${server.port}}} 이다. Eureka Server가 같은 service(application)이지만 다른 client임을 구별하기 위한 용도로 사용할 수 있다.

*참고*
* Eureka Client들을 Eureka Registry에 등록하면 다음처럼 등록된다.(아래 사진은 예시일뿐이다.)
![image](https://user-images.githubusercontent.com/20153890/41632852-e3c59ffa-7476-11e8-8920-0935bafc1c40.png)


사진을 보면 Application, AMIs, Availability Zones, Status를 확인 할 수 있다.
* Application에 보여지는 여러 Service들은 각 Eureka Client의 spring.application.name이다.
* Status는 현재 service가 Up인 상태인지, Down인 상태인지를 나타낸다. 

또 한가지 알아두어야 할 점은 Status 오른쪽의 list들이다. 이 list에는 각 Eureka Client의 eureka.instance.instanceId값이 등록된다.
쉽게 이해하기 위해 Notice-Service를 보자.

Notice-Service는 3개의 Up상태인 client를 가지고 있다. 
* notice-service:7c09a271351a998027f0d1e2c72148e5  
* notice-service:14d5f9837de754b077a6b58b7e159827  
* notice-service:7c6d41264f2f71925591bbc07cfe51ec 

이 3개의 client는 spring.application.name=notice-service로 같지만, eureka.instance.instanceId가 각기 다르단 얘기이다. 

즉 Eureka Registry에 같은 spring.application.name을 가진 어떠한 Client가 등록되면, eureka.instance.instanceId값으로 구분 할 수 있다는 얘기다. 우리는 이를 잘 이용해서 추후에 Dynamic Routing을 할 것이므로 알아 두자.


&nbsp;
&nbsp;

## 3. EurekaClient ##

```java
@SpringBootApplication
@EnableEurekaClient
public class AlarmServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlarmServiceApplication.class, args);
	}
```

dependency는 앞에서 설정 했으므로 main class에 @EnableEurekaClient annotation만 추가하면 된다. 

그럼 이 Eureka Client를 REST API Server로 만들어보자

&nbsp;

## 4. REST API Server 구축 ##

&nbsp;

**REST API**

METHOD | PATH | DESCRIPTION 
------------|-----|------------
GET | /notice/ | 전체 알림정보 제공
GET | /notice/{receiver_ID} | 해당 receiver 에 대한 알림 정보 제공
GET | /notice/latest/{receiver_ID} | 해당 receiver 에 대한 최근 10개 알림정보 제공
GET | /notice/previous/{receiver_ID}/{id} | 해당 receiver 에 대한 정보들 중 {id}값을 기준으로 이전 10개 정보 제공
POST | /notice/ | 알림 정보 입력

&nbsp;
&nbsp;


**Table(table name = notice) description**

| Field       | Type        | Null | Key | Default | Extra          |
--------------|-------------|------|-----|---------|----------------|
| id          | int(11)     | NO   | PRI | NULL    | auto_increment |
| receiver_id | varchar(20) | NO   |     | NULL    |                |
| sender_id   | varchar(20) | NO   |     | NULL    |                |
| article_id  | int(11)     | NO   |     | NULL    |                |


우리는 JPA를 이용해 DB에 접근할 것이다. 따라서 JPA란 무엇인지에 대해 간단하게 알아보고 넘어가자.
(DB setting은 개인적으로 하자..)

&nbsp;

**JPA란?**

 JPA는 자바 진영의 ORM 기술 표준이다. Java Persistence API(JPA)는 RDB에 접근하기 위한 표준 ORM을 제공하고, 기존 EJB에서 제공하는 Entity Bean을 대체하는 기술이다. Hibernate, OpenJPA 와 같은 구현체들이 있고 이에 따른 표준 인터페이스가 JPA인 것이다.

**ORM 이란?**

 객체와 RDB를 매핑한다. 기존에 spring에서 많이 사용하던 mybatis등은 ORM이 아니고, SQL Query를 Mapping하여 실행한다.
따라서 Spring-Data-JPA를 이용하면, 객체 관점에서 DB에 접근하는 형태로 어플리케이션을 개발할 수 있다.

**JPA를 사용해야하는 이유는?**

1. 생산성 => 반복적인 SQL 작업과 CRUD 작업을 개발자가 직접 하지 않아도 된다.
2. 성능 => 캐싱을 지원하여 SQL이 여러번 수행되는것을 최적화 한다.
3. 표준 => 표준을 알아두면 다른 구현기술로 쉽게 변경할 수 있다.

**JPA Annotation**

Annotaion | DESCRIPTION 
----------|------------
@Entity | Entity임을 정의한다.
@Table | (name = "table name") , Mapping할 table 정보를 알려준다. 
@id | Entity class의 필드를 table의 PK에 mapping한다.
@Comlumn | field를 column에 매핑한다. 

@Comlumn annotaion은 꼭 필요한것은 아니다. 따로 선언해주지 않아도 기본적으로 멤버 변수명과 일치하는 DB의 Column을 mapping한다.

@Table annotaion또한 기본적으로 @Entity로 선언된 class의 이름과 실제 DB의 Table 명이 일치하는 것을 mapping한다.

&nbsp;

## 4.1 JPA Entity ##

SpringBoot에서는 JPA로 데이터를 접근하게끔 유도하고 있다. 이를 활용해서 REST API Server를 구축해보자.

아래는 JPA Entity를 담당할 Class이다. 

**Notice.java**
```java
@Entity
public class Notice {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private int id;
        private String receiver_id;
        private String sender_id;
        private int article_id;

        protected Notice() {}

        public Notice(final int id, final String receiver_id, final String sender_id,final int article_id)
        {
                this.receiver_id=receiver_id;
                this.sender_id=sender_id;
                this.article_id=article_id;
        }

        public int getId()
        {
                return id;
        }

        public String getReceiver_id()
        {
                return receiver_id;
        }
        public String getSender_id()
        {
                return sender_id;
        }

        public void setReceiver_id(String receiver_id)
        {
                this.receiver_id=receiver_id;
        }

        public void setSender_id(String sender_id)
        {
                this.sender_id=sender_id;
        }

        public int getArticle_id()
        {
                return article_id;
        }
        public void setArticle_id(int article_id)
        {
                this.article_id=article_id;
        }
        @Override
        public String toString()
        {
                return String.format("Notice [id = %d ,receiver_id = '%s', sender_id = '%s', article_id = %d] ", id, receiver_id, sender_id, article_id);

        }

}
```

&nbsp;

## 4.2 Repository ##  

Entity class를 생성했다면, Repository Interface를 생성해야 한다. Spring에서는 Entity의 기본적 insert, delete, update 등이 가능하도록
CrudRepository라는 interface를 제공한다.

**NoticeRepository.java**
```java
public interface NoticeRepository extends CrudRepository<Notice, String>{
	
	
	@Query("SELECT n FROM Notice n WHERE receiver_id=:receiver_id ORDER BY id DESC")
	List<Notice> findNoticeByReceiverId(@Param("receiver_id") String receiver_id);
	
	@Query("SELECT n FROM Notice n WHERE receiver_id=:receiver_id ORDER BY id DESC")
	List<Notice> findLatestNoticeByReceiverId(@Param("receiver_id") String receiver_id, Pageable pageable);
	
	@Query("SELECT n FROM Notice n WHERE n.receiver_id=:receiver_id AND n.id < :id ORDER BY n.id DESC")
	List<Notice> findPreviousNoticeByReceiverId(@Param("receiver_id")String receiver_id, @Param("id") int id, Pageable pageable);
	
	
}

```

위 코드는 실제 Notice Entity를 이용하기 위한 Repository이다. 기본적인 CRUD외에 필자가 필요한 메소드를 @Query를 이용해 기존의 SQL처럼 사용하도록 지정해 놓은 상태이다.

이 외에도 CrudRepositorys는 find(), findAll(), findAllById() 등 여러 method를 제공한다. 이에 대한 세부사항은 다음 레퍼런스를 꼭 참고하자.
* Interface CrudRepository<T,ID> => https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html

&nbsp;

## 5. Service ## 

이제 실제 필요한 Service interface를 만들어 볼 차례이다.

    ├── rest   
    │   ├── NoticeController.java         
    ├── service
        ├── NoticeService.java          
        └── NoticeServiceImpl.java     
        
먼저 NoticeService.java 와 NoticeServiceImpl.java파일을 생성한다. NoticeService는 interface로 생성할 것이고, 이에대한 명세는 NoticeServiceImpl.java에서 구현한다. interface에 대한 method 구현시 NoticeRepository의 method를 활용한다.

**NoticeService.java**

```java
public interface NoticeService {

        List<Notice> findAllNotice();
        List<Notice> findAllNoticeByReceiverId(String receiver_id);
        List<Notice> findLatestNoticeByReceiverId(String receiver_id);
        List<Notice> findPreviousNoticeByReceiverId(String receiver_id, int id);
        Notice saveNotice(Notice notice);

}
```

**NoticeServiceImpl.java 일부**

```java
@Service("noticeService")
public class NoticeServiceImpl implements NoticeService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	

	@Autowired
	private NoticeRepository noticeRepository;
	
	@Override
	public List<Notice> findAllNotice()
	{
		
		Optional<Iterable<Notice>> maybeNoticeIter = Optional.ofNullable(noticeRepository.findAll());
		
		return Lists.newArrayList(maybeNoticeIter.get());

		
	}
	
	@Override
	public List<Notice> findAllNoticeByReceiverId(String receiver_id)
	{
	    
		Optional<List<Notice>> maybeNotice =
			Optional.ofNullable(noticeRepository.findNoticeByReceiverId(receiver_id));
		
		return maybeNotice.get();

	}
	

	@Override
    public List<Notice> findLatestNoticeByReceiverId(String receiver_id)
    {
		Optional<List<Notice>> maybeLatestNotice= 			
			Optional.ofNullable(noticeRepository.findLatestNoticeByReceiverId(receiver_id, PageRequest.of(0, 10)));
		
		return maybeLatestNotice.get();

	
	}

```

&nbsp;

## 6. Rest Controller ## 

이제 controller를 만들어 보자. rest package를 따로 만들고 그곳에 RestController들을 정의한다. 


    ├── rest   
    │   ├── NoticeController.java 
    
@RestControler annotation을 설정하여 RestController를 만든다.
(HystrixMethod적용은 다음 단계에서 진행한다. 여기서는 REST API만 구축한다)

**NoticeController.java 일부**


```java
@RestController
@CrossOrigin(origins="*")
public class NoticeController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    	public static List<Notice> Temp;

	@Autowired
	private NoticeService noticeService;

    	@Autowired
    	private DiscoveryClient discoveryClient;

	@RequestMapping(value = "/notice", method=RequestMethod.GET)
	public ResponseEntity<List<Notice>> getAllNotice(){
	
		try{
			Optional<List<Notice>> maybeAllStory = Optional.ofNullable(noticeService.findAllNotice());
		
			return new ResponseEntity<List<Notice>>(maybeAllStory.get(), HttpStatus.OK);
		}catch(Exception e)
		{
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
		}	
	}	

	@RequestMapping(value="/notice/{receiver_id}", method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getAllNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id)
	{

		try {
			Optional<List<Notice>> maybeSelectedNotice =
				Optional.of(noticeService.findAllNoticeByReceiverId(receiver_id));
		
			return new ResponseEntity<List<Notice>>(maybeSelectedNotice.get(), HttpStatus.OK);
			
		}catch(Exception e)
		{
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
		}
	}

```
&nbsp;

## 7. Maven Packaging ## 

Host OS에 설치된 maven을 이용해도 되고, spring boot application의 maven wrapper를 사용해도 된다
(maven wrapper는 Linux, OSX, Windows, Solaris 등 서로 다른 OS에서도 동작한다. 따라서 추후에 여러 서비스들을 Jenkins에서 build 할 때 각 서비스들의 Maven version을 맞출 필요가 없다.)

*A Quick Guide to Maven Wrapper => http://www.baeldung.com/maven-wrapper)*

**a. Host OS의 maven 이용**

```bash
[sangmin@Mint-SM] ~/springcloud-service $ mvn package 
```
&nbsp;

**b. maven wrapper 이용**

```bash
[sangmin@Mint-SM] ~/springcloud-service $ ./mvnw package 
```
&nbsp;

## 8. Execute Spring Boot Application ##

REST API Server가 제대로 구축 되어졌는지 확인해보자.

```bash
[sangmin@Mint-SM] ~/springcloud-service $java -jar target/{your_application_name}.jar
```

Eureka Dashboard를 통해 Client가 제대로 등록 되어졌는지 확인해보자

Check Your Eureka Dashboard 
 * http://{Your-Eureka-Server-Address}:8761 
 * http://{Your-Eureka-Server-Address}:8761/eureka/apps

Client가 Eureka Server에 등록 될 때 약간의 시간이 소요될 수 있다.

&nbsp;

## 9. Dockerizing ## 

구축한 Eureka Client를 docker image를 만들어 볼 차례이다. 먼저 Dockerfile을 작성한다. 

> -       $mvn package 


**Dockerfile**
```
	FROM openjdk:8-jdk-alpine
	VOLUME /tmp
	#ARG JAR_FILE
	#ADD ${JAR_FILE} app.jar
	#dockerfile-maven-plugin으로 docker image를 생성하려면 아래 ADD ~를 주석처리하고, 위 2줄의 주석을 지우면 된다.
	ADD ./target/notice-service-0.0.1.jar app.jar
	ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

Dockerfile 작성이 끝났다면 image를 build 하자

**a. dockerfile-maven-plugin 사용시**

```bash
[sangmin@Mint-SM] ~/springcloud-service $ ./mvnw dockerfile:build
```
&nbsp;

**b. docker CLI 사용시**

```bash
[sangmin@Mint-SM] ~/springcloud-service $ docker build -t {your_docker_id}/notice-service:latest
```

이후 docker image가 잘 생성 되었음을 확인하자.

```bash
[sangmin@Mint-SM] ~/springcloud-service $ docker images
REPOSITORY                      TAG                 IMAGE ID            CREATED             SIZE
phantasmicmeans/notice-service  latest              4b79d6a1ed24        2 weeks ago         146MB
openjdk                         8-jdk-alpine        224765a6bdbe        5 months ago        102MB

```
&nbsp;

## 10.  Run Docker Container ##

Docker image를 생성하였으므로 이미지를 실행 시켜보자.

```bash
[sangmin@Mint-SM] ~ $ docker run -it -p 8763:8763 phantasmicmeans/notice-service:latest 
```

이제 Eureka Dashboard를 통해 Client가 제대로 실행 되었는지 확인하면 된다.

&nbsp;


## Conclusion ## 

이상으로 간단한 REST API Server로 구축된 Microservice를 Eureka Client로 구성해 보았다. 다음 장에서는 Eureka Client로 구성된 Microservice에 Hystrix를 적용해 볼 것이다.

**다음 글**
* Hystrix에 대한 이해 & => https://github.com/phantasmicmeans/Spring-Cloud-Netflix-Hystrix
* Spring boot Microservice에 Hystrix적용하기* => https://github.com/phantasmicmeans/Spring-Cloud-Netflix-Hystrix-Tutorial



