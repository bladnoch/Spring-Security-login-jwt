# airbnb-login

## 설정 주의사항
* (http -> https) 시 LoginFilter 의 createCookie 에서 설정 범위와 setSecure 설정을 활성화 해야함 
* JWTFilter 의 doFilterInternal() 의 토큰 만료, 토큰이 access 인지 확인 하는 과정 에서 예외 처리를 어떻게 해야 할지 클라랑 결정을 해야함
  * ex) 예외 코드 400? 401?

### refresh/access ch.4
* form-data -> json 관련 언급 댓글 있음

### refresh/access ch.5
* JWTFilter 에 doFilterInternal 내부 코드 전부 바꿈(이전 코드는 모두 주석 처리)
* JWTFilter 수정 으로 access token 을 이용한 인증 로직 추가 