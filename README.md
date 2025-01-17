# NEO
## 1. 개요
**NEO**는 **게임 서버 대여 및 간편 관리 서비스**입니다.   
**NEO**는3가지의 문제에 대해 집중하였습니다.
1. 처음 게임 서버를 개설하는 사람들은 스스로 게임 서버를 개설하기 위해 시간과 노력이 들어갑니다.
2. 게임 서버를 개설한 서버 관리자는 자신의 서버를 알리기 위해서 커뮤니티에 글을 올려야 합니다. 이는 서버와 커뮤니티가 따로 분리되어 관리해야하는 것을 의미합니다.
3. 서버 관리자는 게임 서버를 개설하고 게임을 즐기는 경우, 대체로 짧은 기간 동안만 게임을 하는 경우가 많습니다.

**NEO**는 각각의 문제에 맞서 
1. 처음 게임 서버를 개설하는 사람들에게 **단순한 UI**로 게임을 관리하게 합니다.
2. 서버 관리자가 **웹** 상에서 서버 관리와 서버 커뮤니티를 동시에 관리하게 합니다.
3. 서버 관리자는 시간에 따른 비용을 지불하는 **시간 요금제**를 통해 짧은 기간 동안 대여할 수 있습니다.

## 2. 주요 기능
**NEO**의 주요 기능은 3가지로 나뉩니다.

**1. 게임 서버 관리**
* 서버 시작, 정지, 게임 맵 & 모드 & 플러그인 추가, 게임 서버 로그,서버 상태 등의 서버 운영에 필요한 UI 제공합니다

**2. 게임 서버 커뮤니티**
* 서버 관리자가 연 서버 목록을 서버 관리자가 원할 경우, 다른 사용자가 볼 수 있습니다
* 서버 참여, 서버 참여를 희망한 인원의 승인/거절, 참여 인원등의 서버 인원에 대한 관리 UI를 제공합니다

**3. 게임 서버 대여**
* 저희가 선정한 게임 서버 용량(메모리) 선택지를 서버 관리자에게 추천합니다
* 서버 관리자가 사용했던 서버를 다시 대여 가능합니다
* 서버 대여시, 결제한 요금제에 따라 시간제일 경우 대여한 시간에 따라 가격을 받고, 기간제일 경우 기간에 맞게 요금을 받습니다

## 3. 시스템 구조

![전체시스템개요 소프트콘_Readme drawio](https://github.com/ajouNEO/NEO/assets/128200788/46e7d1d8-612e-4700-82de-f63dc49f0f50)

Main Server : 유저의 요청에 따라 백엔드 기능 및 Edge Server 내에 유저의 도커 서버와 게임 서버를 관리합니다.    
Edge Server : 유저의 게임 서버를 도커 위에 띄워서 실행시키는 서버, 여러 Edge Server를 둘 수 있으며, 이 중에 Main Server에서 메모리를 기준으로 Edge Server를 선택하고, 도커 서버를 할당하여 게임 서버를 Managing process를 통해 관리합니다.

***

### Teck Stack
|분야|기술|
|:---|:---|
|**DevOps**| Github, Github Actions|
|**Docker**| DockerAPI, Prometheus|
|**Database**|MySQL, Naver Cloud Storage|
|**FrontEnd**|Framer, React|
|**BackEnd**|Spring Boot, Gradle, Naver Cloud Web Server, Shell|

## 4. 팀원 소개

|이름|역할|이메일|깃허브|
|:---:|:---:|:---:|:---:|
|이선우|백엔드 개발|malenwater@ajou.ac.kr|https://github.com/malenwater|
|이은구|백엔드 개발 & 인프라 & DevOps|mueg12@ajou.ac.kr|https://github.com/mueg12|
|박병하|백엔드 개발|akdl4045@ajou.ac.kr|https://github.com/ArkBB|
|박건희|프론트 개발|gun9679@ajou.ac.kr|https://github.com/bobmari1004|
|권초염(QUANCHUYAN)|프론트 개발|shikuawo@ajou.ac.kr|https://github.com/shikuawo|

## 5. 프로젝트 포스터

2024-1학기 아주대학교 소프트콘에 제출한 포스터입니다.

![포스터최신버전1 1_page-0001](https://github.com/ajouNEO/NEO/assets/128200788/18021a57-b2b2-4406-8ea1-f3ddd0bf4024)

## 6. 백엔드 사용법 

### env.properties 세팅
 web/back/src/main/resources/env.properties 파일에 환경 설정을 세팅해야합니다.

    SPRING_JWT_SECRET=...

    NAVER_CLIENT_ID = ...
    NAVER_CLIENT_SECRET = ...
    GOOGLE_CLIENT_ID = ...
    GOOGLE_CLIENT_SECRET = ...

    edgeservers.ip=...
    edgeservers.id=...
    edgeservers.user.id=...
    edgeservers.externalIp=...
    edgeservers.password=...
    edgeservers.memoryTotal=...
    edgeservers.memoryUse=...
    edgeserver.number = ...
    
    KEY_STORE_PASSWORD = ...
    
    SPRING_MAIL_USERNAME = ...
    SPRING_MAIL_PASSWORD = ...
    
    MAIN_SERVER_IP = ...
    
### BackEnd 실행
 환결 설정 후, 아래 코드를 실행합니다.

    cd web/back/
    ./gradlew bootrun

### EdgeServer 실행
 Edge Server에 게임 서버 도커 이미지를 만들어야 Edge Server에 게임 서버를 생성할 수 있습니다.
 해당 Edge Server에 이동하고, 깃허브의 game-server/dockertest/ 폴더에 있는 모든 데이터를 옮겨와서
 
     docker build -t mc1.16.5 -f Dockerfile_mc1.16.5 .
     docker build -t mc1.19.2 -f Dockerfile_mc1.19.2 .
     docker build -t mc1.20.4 -f Dockerfile_mc1.20.4 .
     
와 같이

    docker build -t 폴더명 -f Dockerfile_폴더명 .
    
으로 도커이미지를 생성해줍니다.

## 7. 프론트 설명
프론트는 Framer를 이용하여 웹 디자인과 기능을 개발하였습니다.
특히 기능 부분은 Framer 안에서 React, Typescript, JSX를 통해 개발했습니다.

### Framer 사용 이유
디지털 화면에서 디자인 요소들은 상호작용을 통해 바뀌는 특징이 있습니다.
일반 그래픽 툴을 이용하여 디자인하면 상호작용하는 화면 요소들을 하나하나 다 그려내야만 합니다.
하지만 Framer는 이 상호작용 과정을 코드를 사용하지 않고 쉽게 도와줍니다.
그리고 Framer 툴 내에서 협업할 수 있는 특징이 있어, 개발할 때 사용하는 많은 툴들을 줄여줄 수 있는 특징이 있습니다.

### Framer URL
https://framer.com/projects/NEO--H1Fqms6Su09vkk1kjXSR-6Qbvu
