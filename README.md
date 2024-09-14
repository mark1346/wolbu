# 월부 백엔드 시스템
## 📺 개발환경
<img src="https://img.shields.io/badge/Framework-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"><img src="https://img.shields.io/badge/3.3.3-515151?style=for-the-badge">
<img src="https://img.shields.io/badge/Build-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white"><img src="https://img.shields.io/badge/8.10-515151?style=for-the-badge">
<img src="https://img.shields.io/badge/Language-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/java-%23ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"><img src="https://img.shields.io/badge/17-515151?style=for-the-badge">

## 애플리케이션 실행 절차
1. ### 프로젝트 클론
```
git clone https://github.com/mark1346/wolbu.git
cd wolbu
```
2. ### 환경 변수 설정
- application.yml 파일을 열어 환경변수를 설정합니다
- OpenAI API 키를 설정해야 GPT 기반 강의 추천 기능이 동작합니다.
3. ### 프로젝트 빌드
```
./gradlew build -x test
```
- 이 프로젝트의 모든 테스트는 성공적으로 구현되어 통과됐습니다. 
- 커맨드 라인에서 Gradle을 통해 빌드할 때는 환경 설정의 차이로 인해 테스트 실행에 문제가 발생할 수 있습니다. 따라서 커맨드 라인에서 빌드 시에는 테스트를 생략하는 것을 권장합니다.
- 일부 테스트는 순서대로 실행되어야 하며, OpenAI API 키가 필요한 테스트가 있습니다.
4. ### 애플리케이션 실행
```
./gradlew bootRun
```
- 위 명령어로 애플리케이션을 실행합니다. 
```
java -jar build/libs/wolbu-0.0.1-SNAPSHOT.jar
```
- 빌드된 JAR 파일을 직접 실행할 수도 있습니다.
5. ### API Specification
- 웹 브라우저에서 http://localhost:8080/swagger-ui.html 에 접속합니다.
- Swagger UI를 통해 API 명세를 확인할 수 있습니다.


## 데이터베이스 설정
- H2 인메모리 데이터베이스를 사용합니다.
- 필요한 경우, application.yml 파일에서 데이터베이스 설정을 변경할 수 있습니다.
- 현재 콘솔경로는 /h2 이며, url은 jdbc:h2:mem:test 입니다. 
## 주요 기능
1. 회원 가입
- 사용자는 학생 또는 강사로 회원가입할 수 있습니다
- 비밀번호는 암호화되어 저장되며, bcrypt password encoder가 사용됩니다.
- 이메일 주소를 통한 중복 가입을 방지합니다
2. 강의 개설
- 강사만 강의를 개설할 수 있습니다
- 강의명, 최대 수강 인원, 가격 등의 정보를 설정할 수 있습니다
3. 수강 신청
- 학생은 개설된 강의 목록을 조회하고 원하는 강의를 신청할 수 있습니다.
- 최근 등록순, 신청자 많은 순, 신청률 높은 순으로 강의 목록을 정렬할 수 있습니다.
- 한 번에 여러 강의를 동시에 신청할 수 있습니다. 
### 부하처리 (동시성 제어)
- 수강신청 시 발생할 수 있는 동시성 문제를 해결하기 위해 비관적 락(Pessimistic Lock)을 사용했습니다.
- Course 조회 시 비관적 락을 통해 설정된 최대 수강 인원만큼만 선착순으로 신청처리됩니다. (test/java/wb/wolbu/integration/ConcurrencyTest 참고)

## 추가 기능
1. 확장성을 고려한 CRUD 구현
- 회원가입, 강의등록, 수강신청 이외에도 실제로 사용될 확률이 높은 CRUD 엔드포인트들을 구현했습니다.
- 
  
