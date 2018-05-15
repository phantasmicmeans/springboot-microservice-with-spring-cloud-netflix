Alarm Service 
==============

by S.M.Lee


![image](https://user-images.githubusercontent.com/20153890/40037190-cfd1ca38-5846-11e8-8443-a00a08a57fb5.png)

> **NOTE** 
> - 여기서는 MSA에서의 Service중 하나인 Alarm Service를 구축하여 본다.
> - Alarm Service는 API Server로 구성되고, Spring Cloud Netflix의 여러 instance들을 사용한다. 
> - Alarm Service는 Spring boot Project로 구현된다. 생성된 JAR파일을 Docker container로 띄워 서비스한다.
> - 기존 Spring에서는 Maven, Gradle등의 dependency tool을 이용해 WAR파일을 생성한 후 tomcat같은 WAS에 배포하여
웹 어플리케이션을 구동하였으나, Spring boot는 JAR파일에 내장 tomcat이 존재하여, 단순히 JAR파일을 빌드하고 실행하는 것 만으로 웹 어플리케이션 구동이 가능하다.
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

3. docker build를 위한 dockerfile-maven-plugin, repository 추가 

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

## 4. JPA ##

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

## 4.1 JPA Entity ##

SpringBoot에서는 JPA로 데이터를 접근하게끔 유도하고 있다. 

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


## 4.2 Repository ##  

Entity class를 생성했다면, Repository Interface를 생성해야 한다. Spring에서는 Entity의 기본적 insert, delete, update 등이 가능하도록
CrudRepository라는 interface를 제공한다.

**NoticeRepository.java**
```java

public interface NoticeRepository extends CrudRepository<Notice, String>{


        @Query("SELECT n FROM Notice n WHERE receiver_id=:receiver_id ORDER BY id DESC")
        List<Notice> findLatestNoticeByReceiverId(@Param("receiver_id") String title, Pageable pageable);

        @Query("SELECT n FROM Notice n WHERE n.receiver_id=:receiver_id AND n.id < :id ORDER BY n.id DESC")
        List<Notice> findPreviousNoticeByReceiverId(@Param("receiver_id")String title, @Param("id") int id, Pageable pageable);


}
```

위 코드는 실제 Notice Entity를 이용하기 위한 Repository이다. 기본적인 CRUD외에 필자가 필요한 메소드를 @Query를 이용해 기존의 SQL처럼 사용하도록 지정해 놓은 상태이다.

## 5. Service ## 

이제 실제 필요한 Service interface를 만들어 볼 차례이다.

    ├── rest   
    │   ├── NoticeController.java         
    ├── service
        ├── NoticeService.java          
        └── NoticeServiceImpl.java     
        
먼저 NoticeService.java 와 NoticeServiceImpl.java파일을 생성한다. NoticeService는 interface로 생성할 것이고, 이에대한 명세는 NoticeServiceImpl.java에서 구현한다.

**NoticeService.java **

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

        private List<Notice> result;

        @Autowired
        private NoticeRepository noticeRepository;

        @Override
        public List<Notice> findAllNotice()
        {
                List<Notice> AllNotice = new ArrayList<>();

                Iterable<Notice> allNoticeIter = noticeRepository.findAll();

                Consumer<Notice> noticeList = (notice) -> {
                        AllNotice.add(notice);
                };

                allNoticeIter.forEach(noticeList);

                return AllNotice;
        }

        @Override
        public List<Notice> findAllNoticeByReceiverId(String receiver_id)
        {
                result = new ArrayList<>();

                Iterable<Notice>notiIter = noticeRepository.findAll();

                notiIter.forEach(result::add);
                List<Notice> matchingResult = result.stream()
                                              .filter(receiver-> receiver.getReceiver_id()
                                              .equals(receiver_id))
                                              .collect(Collectors.toList());
                return matchingResult;
        }

        @Override
        public List<Notice> findLatestNoticeByReceiverId(String receiver_id)
        {
                return noticeRepository.findLatestNoticeByReceiverId(receiver_id,PageRequest.of(0, 10));
        }
```

## 6. Rest Controller ## 

이제 API를 만들어 보자. rest package를 따로 만들고 그곳에 RestController들을 정의한다.

    ├── rest   
    │   ├── NoticeController.java 
    
위처럼 NoticeController.java를 만들고, @RestControler annotation을 설정하여 RestController를 만든다.

**NoticeController.java 일부**


```java
@RestController
public class NoticeController {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static List<Notice> Temp;

        @Autowired
        private NoticeService noticeService;

        @RequestMapping(value = "/notice", method=RequestMethod.GET)
        public ResponseEntity<List<Notice>> getAllNotice(){

                final List<Notice> allMembers = noticeService.findAllNotice();

                if (allMembers.isEmpty()) {
                        return new ResponseEntity<List<Notice>>(HttpStatus.NO_CONTENT);
                }

                return new ResponseEntity<List<Notice>>(allMembers, HttpStatus.OK);
        }

        @RequestMapping(value="/notice/{receiver_id}", method = RequestMethod.GET)
        public ResponseEntity<List<Notice>> getAllNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id)
        {

                final List<Notice> selectedNotice = noticeService.findAllNoticeByReceiverId(receiver_id);

                if (selectedNotice.isEmpty())
                {
                        logger.info("404 Not Found"); ;
                        return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
                }

                return new ResponseEntity<List<Notice>>(selectedNotice, HttpStatus.OK);

        }
        
        @RequestMapping(value="/notice/latest/{receiver_id}",method = RequestMethod.GET)
        public ResponseEntity<List<Notice>> getLastNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id)
        {

                final List<Notice> LastNotice = noticeService.findLatestNoticeByReceiverId(receiver_id);

                if(LastNotice.isEmpty())
                {
                        logger.info("GetLastNoriceByReceiverId, 404 Not Found");
                        return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);

                }

                return new ResponseEntity<List<Notice>>(LastNotice , HttpStatus.OK);
        }
        
        @RequestMapping(value="/notice/previous/{receiver_id}/{id}",method = RequestMethod.GET)
        public ResponseEntity<List<Notice>> getPreviousNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id,   @PathVariable("id") final int id)
        {

                final List<Notice> PreviousNotice = noticeService.findPreviousNoticeByReceiverId(receiver_id,id);

                if(PreviousNotice.isEmpty())
                {
                        logger.info("GetPreviousNoticeByReceiverId, 404 Not Found");
                        return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);

                }

                return new ResponseEntity<List<Notice>>(PreviousNotice , HttpStatus.OK);
        }

```

## 7. docker build ## 

이제 docker image를 만들어 볼 차례이다. 먼저 jar파일을 생성하고 Dockerfile을 추가한다.

> -       $mvn package 


**Dockerfile**
```
     FROM openjdk:8-jdk-alpine
     VOLUME /tmp
     ARG JAR_FILE
     ADD ${JAR_FILE} app.jar
     ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```


이제 docker image를 빌드한다. 

> -       $mvn dockerfile:build

빌드가 완료 되었다면 $docker images 명령어를 통해 docker image를 확인할 수 있다. 


### 이상으로 Spring Cloud Netflix의 여러 instance를 이용해 MSA 구축에 필요한 Service를 API Server형태로 구현해 보았다. ###


