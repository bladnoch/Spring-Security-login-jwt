# airbnb-login

## 설정 주의사항
* (http -> https) 시 LoginFilter 의 createCookie 에서 설정 범위와 setSecure 설정을 활성화 해야함 
* JWTFilter 의 doFilterInternal() 의 토큰 만료, 토큰이 access 인지 확인 하는 과정 에서 예외 처리를 어떻게 해야 할지 클라랑 결정을 해야함
  * ex) 예외 코드 400? 401?
* 토큰 만료시간 설정은 Loginfilter.successfulAuthentication 에서 설정 가능

### refresh/access ch.4
* form-data -> json 관련 언급 댓글 있음

### refresh/access ch.5
* JWTFilter : access token 유효성 검사
* JWTFilter 에 doFilterInternal 내부 코드 전부 바꿈(이전 코드는 모두 주석 처리)
* JWTFilter 수정 으로 access token 을 이용한 인증 로직 추가 

### refresh/access ch.6 
* +ReissueController : refresh token 이용해 access token 발급
* 리이슈 컨트롤러 내부 /reissue 서비스로 분리 리팩토링 필요
* 리이슈 할 때 header에 refresh token 이 담긴 쿠키를 담아 보내면 


### refresh/access ch.7
* reissue rotate 기능 추가해서 reissue 호출시 refresh token도 재발급

### refresh/access ch.7
* reissue 때 refresh token 재발급
* RefreshRepository에 저장
* 지금은 refresh token이 하나만 저장되는 문제가 있고
* 나중에 모든 refresh token을 저장할꺼라면 여러 refresh token을 어떤식으로 저장하고 관리할지 결정해야함