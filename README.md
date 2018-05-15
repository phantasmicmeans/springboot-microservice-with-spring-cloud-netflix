Alarm Service 
==============

> **NOTE** 
> - 여기서는 MSA에서의 Service중 하나인 Alarm Service를 구축하여 본다.
> - Service는 API Server로 구성된다.
> - Alarm Service는 Spring boot Project로 구현된다.
> - 기존 Spring에서는 Maven, Gradle등의 dependency tool을 이용해 WAR파일을 생성한 후 tomcat같은 WAS에 배포하여
웹 어플리케이션을 구동하였으나, Spring boot는 JAR파일에 내장 tomcat이 존재하여, 단순히 JAR파일을 빌드하고 실행하는 것 만으로 웹 어플리케이션 구동이 가능하다.
> - API Server를 구축하고, 생성된 JAR파일을 Docker container로 띄워 서비스한다. 

**Service는 "Service Register & Discovery" Server인 Eureka Server의 Client이다.**


## 1. REST API 설계 ##

HTTP METHOD | URI | description 
------------|-----|------------
GET | /notice/ | 전체 알림정보 제공
GET | /notice/{receiver_ID} | 해당 receiver 에 대한 알림 정보 제공
GET | /notice/latest/{receiver_ID} | 해당 receiver 에 대한 최근 10개 알림정보 제공
GET | /notice/previous/{receiver_ID}/{id} | 해당 receiver 에 대한 정보들 중 {id}값을 기준으로 이전 10개 정보 제공
POST | /notice/ | 알림 정보 입력
