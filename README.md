# ADWD · Adaptive Dementia Wandering Detection

평소 동선과 시간대를 학습해서, 벗어날 때만 단계별(0 정상 / 1 주의 / 2 경고 / 3 위험)로 알리는 치매 배회 감지 앱.
객체지향프로그래밍 4차 팀 프로젝트.

## 실행 방법

0. **프로젝트 폴더는 영어로만 된 경로에 두세요** (예: `D:\dev\Adaptive-Dementia-Wandering-Detection-APP`). 경로에 한글이 있으면 윈도우에서 `Your project path contains non-ASCII characters` 에러로 빌드가 멈춥니다.
1. Android Studio에서 이 폴더를 엽니다 (File → Open).
2. Gradle 동기화가 끝나면 `app`을 에뮬레이터나 기기에서 실행합니다.
   - JDK 17, minSdk 26 (Android 8.0), compileSdk 35
   - Android Studio가 AGP·Gradle 업데이트를 권하면 그대로 올려도 됩니다.
   - **처음 한 번 NDK 설치 필요** (C++ 엔진을 같이 빌드하기 때문): Settings → Languages & Frameworks → Android SDK → SDK Tools 탭 → **NDK (Side by side)** 와 **CMake 3.22.1** 체크 → Apply. 설치 안 하고 Sync하면 "NDK not configured" 같은 에러가 납니다.
   - `Incompatible Gradle JVM version` 에러가 나면: Settings → Build, Execution, Deployment → Build Tools → Gradle → **Gradle JDK를 21(또는 17)** 으로 바꾸고 다시 Sync. (Gradle 8.11은 JDK 25에서 못 돈다)

## 지금 들어 있는 것 (UI · 지후)

- Kotlin + Jetpack Compose로 만든 화면 15개와 화면 이동
- 디자인은 피그마 파일과 같습니다. 색·글자 스타일 이름도 피그마 Foundation 페이지와 같게 맞췄습니다.
- 데이터는 전부 가짜 데이터(`ui/model/FakeData.kt`)입니다.

### 발표 시연용 데모 패널(11) 여는 법

- 보호자 홈(05)에서 왼쪽 위 어르신 아바타를 **길게 누르기**
- 어르신 홈(09)에서 시각 글자를 **길게 누르기**

데모 패널에서 경보 단계를 누르면, 어르신 화면이 09 → 10 / 10-2 / 10-3으로 바뀌고 보호자 홈 상태 카드도 같이 바뀝니다.

## C++ 판단 엔진 연결 (JNI)

판단은 전부 레포 루트의 C++ 엔진(`src/`, 민규·건영)이 하고, 앱은 결과만 받아서 화면에 보여줍니다. 엔진 코드는 앱 때문에 바꾸지 않습니다.

```
data/*.csv (금진 모의데이터)
  → AdwdViewModel: 1줄씩 재생 (데모 패널의 1x/10x/60x)
  → WanderEngine.kt → JNI(app/src/main/cpp/wander_jni.cpp) → wander::AlertEngine::update()
  → 단계가 바뀌면 C++ AlertListener(Observer) → Kotlin AlertCallback → 화면 단계 전환
```

- 앱 빌드 때 `app/src/main/cpp/CMakeLists.txt`가 루트 `src/`의 엔진 소스를 그대로 같이 컴파일합니다. 엔진에 `.cpp`를 새로 추가해도 앱 쪽은 고칠 필요 없습니다.
- `data/` 폴더의 CSV는 앱 assets로 그대로 들어갑니다. 데모 시나리오 파일 이름은 `ui/state/AdwdViewModel.kt`의 `SimScenario` 참고 (없으면 샘플 1일치로 대신 재생).
- 보호자 홈 지도는 이 좌표(m, 집 = 0,0, 위쪽 = 북쪽)로 그립니다 (`components/TrajectoryMap.kt`).
- 3단계 긴급의 "괜찮아요"(보호자) → `AlertEngine::resolve()`.
- 규칙(`src/rule/*.cpp`)이 아직 TODO면 엔진은 항상 정상(0)을 돌려줍니다. 규칙이 채워지면 앱은 그대로 두고 경보가 뜹니다.

## 폴더 구조

```
app/src/main/cpp/   CMakeLists.txt, wander_jni.cpp (C++ 엔진 ↔ Kotlin 다리)
app/src/main/java/com/adwd/app/
├─ MainActivity.kt
├─ engine/          WanderEngine (JNI 창구), TrackPoint (CSV 한 줄)
└─ ui/
   ├─ theme/        AdwdColors, AdwdType(Pretendard), AdwdTheme
   ├─ components/   아이콘, 로고, 버튼, 경보 배지, 탭바, 입력칸, 모의 지도
   ├─ model/        AlertLevel(경보 단계 표시용), FakeData
   ├─ state/        AdwdViewModel (화면 상태, 경보 수신 입구)
   ├─ navigation/   AdwdNavHost (화면 이동)
   └─ screens/
      ├─ start/     01 스플래시, 02 로그인, 02-1 회원가입, 03 사용자 선택·동의, 04 어르신 연동
      ├─ guardian/  04-1 자주 가는 장소, 04-2 장소 추가·수정, 05 보호자 홈, 05-1 알림 목록,
      │             06 경보 상세(2단계) / 06-1 경보 상세(1단계), 07 실종 신고 정보, 08 설정, 08-1 어르신 집 위치
      ├─ elder/     09 어르신 홈, 10·10-2·10-3 귀가 안내(한 화면이 단계별로 바뀜)
      └─ demo/      11 시뮬레이션 데모 패널
```

## 다른 파트와 연결하는 곳

코드 안의 `TODO(이름)` 주석이 연결 지점입니다.

| 연결 | 파일 | 할 일 |
|---|---|---|
| 코드B 경보 판단 → 화면 | `src/rule/*.cpp` | **연결 끝.** 규칙 TODO만 채우면 앱에 바로 반영 |
| 코드B 판단 이유 | `model/FakeData.kt` `alertDetails` | 기록 id별 판단 이유·시간순 기록을 엔진 결과로 바꾸기 |
| 코드B 처음 30일 기준 | `model/Place.kt`, `AdwdViewModel.places` | 보호자가 넣은 집·자주 가는 장소를 콜드 스타트 판단에 쓰기 |
| 민규 장소 모델 | `model/Place.kt` | 확정된 Place 모델로 바꾸고 저장하기 |
| 코드A 모의데이터 | `data/` | 정상 여러 날 + 이상 시나리오 3종 CSV 넣기 (`SimScenario`의 파일 이름) |
| 코드A 실종 신고 정보 | `screens/guardian/MissingReportScreen.kt` | 지금은 재생 중인 경로로 채움. MissingReportGenerator가 생기면 교체 |
| 코드A 시뮬레이션 시계 | `screens/elder/ElderHomeScreen.kt` | 기기 시각 대신 시뮬레이션 시각 |
| 코드A 길 안내 | `screens/elder/ElderGuideScreen.kt` | `GuideStep`(방향, 남은 거리, 힌트) 넣기 |
| 민규 User·연동 모델 | `LoginScreen`, `SignupScreen`, `GuardianLinkScreen`, `SettingsScreen` | 로컬 계정 저장, 연결 코드 확인, 설정 저장 |

거리·시간·심야 시간대 같은 판단 기준은 보호자가 바꾸지 않습니다. 코드B 쪽 상수로 둡니다.

## 폰트

Pretendard (SIL Open Font License 1.1). 라이선스: `licenses/Pretendard-OFL.txt`
