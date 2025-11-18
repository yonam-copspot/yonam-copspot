# Admin Sidebar Node.js Server

간단한 Express 서버입니다. `project-root` 폴더의 정적 파일을 제공하며 `/api/health` 엔드포인트를 노출합니다.

사용법:

1. 디렉터리로 이동:

   cd Admin/sidebar

2. 의존성 설치:

   npm install

3. 서버 시작:

   npm start

로컬에서 http://localhost:3000 을 열어 확인하세요.

실행(Windows PowerShell):

```powershell
cd C:/Users/okjunseo/Documents/yonam-copspot/Admin/sidebar; npm install
npm start
```

헬스 체크 예상 응답:

GET http://localhost:3000/api/health

응답 예시:

```
{ "status": "ok", "env": "development" }
```
