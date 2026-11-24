# 저장소 · 코딩 규칙 (11/24 초안, 민규) — C++17

## 1. 환경 · 빌드
- **C++17**, 빌드는 **CMake 3.16+**
  - macOS: `xcode-select --install` 후 `brew install cmake`
  - Windows: Visual Studio 2022 (C++ 데스크톱 개발) 또는 CLion
- 화면 라이브러리: 미정 (Qt / SFML / Dear ImGui 중 지후가 결정) → 정해지면 CMakeLists.txt에 추가
- 인코딩 UTF-8 (한글 문자열 때문에. MSVC는 CMake에서 `/utf-8` 이미 켜둠)
- 빌드 · 실행 · 테스트
  ```
  cmake -S . -B build
  cmake --build build
  ./build/wander                                  # 실행
  ctest --test-dir build --output-on-failure      # 테스트
  ```
  `src/` 아래 `.cpp`는 자동으로 빌드에 포함됨 (새 파일 만들면 `cmake -S . -B build` 한 번 다시)

## 2. 저장소
- GitHub: `G0od-JH/Adaptive-Dementia-Wandering-Detection-APP`, 팀원 전원 Collaborator
- 폴더 구조
  ```
  CMakeLists.txt
  src/
    model/     SimTime, LocationPoint, Trajectory, AlertLevel   (민규)
    config/    Thresholds.h                                     (코드B 수치)
    pattern/   PatternProfile, PatternLearner 구현               (코드B)
    rule/      Rule + 규칙들                                     (코드B)
    strategy/  DetectionStrategy + 구현                          (코드B)
    alert/     AlertState 4개, AlertEngine, AlertListener        (코드B)
    sim/       시뮬레이션 시계, 모의데이터 로더, 실종신고 생성       (코드A)
    ui/        보호자 화면, 어르신 안내 화면, 지도 패널             (지후, 지도 패널 코드A)
    main.cpp   진입점
  tests/       *_test.cpp  (파일 이름이 _test.cpp로 끝나면 자동 등록)
  data/        모의데이터 CSV                                   (코드A)
  docs/        UML, 보고서 자료                                  (민규)
  README.md    실행 방법                                        (현서)
  ```

## 3. 브랜치 · 커밋
- `main` = 항상 빌드·실행되는 상태. **main에 직접 push 금지** (첫 뼈대 커밋만 예외)
- 작업 브랜치: `이름/기능` 예) `geonyoung/route-rule`, `geumjin/mock-data`
- PR 올리고 **1명 이상 확인 후 merge** (인터페이스(.h) 바꾸는 PR은 민규 + 해당 코드 담당 확인)
- 커밋 메시지: `[태그] 한 줄 요약`
  - 태그: `feat` 기능, `fix` 버그, `refactor`, `test`, `docs`, `data`
  - 예) `[feat] 경로 이탈 규칙 구현`, `[fix] 자정 넘는 외출 시간 계산 오류`
- 매일 작업 시작 전 `git pull origin main`
- **12/2 코드 프리즈** 이후엔 `fix`만 merge

## 4. 코드 스타일
- 파일: 클래스 1개 = `클래스명.h` + `클래스명.cpp`, 헤더 맨 위 `#pragma once`
- include: `"폴더/파일.h"` 형식 (예: `#include "model/LocationPoint.h"`), 모든 코드는 `namespace wander { }` 안에
- 이름
  - 클래스·struct·enum: `PascalCase`
  - 함수·변수: `camelCase`
  - private 멤버 변수: 끝에 `_` (예: `points_`)
  - 상수: `k` + PascalCase (예: `kRouteDeviationM`)
- 들여쓰기 스페이스 4칸
- **`new` / `delete` 직접 쓰지 말 것** → `std::make_unique`, `std::vector` 사용
- 가상 함수 재정의엔 반드시 `override`. 상속받을 클래스엔 `virtual ~클래스() = default;`
- 큰 객체는 `const 타입&`로 넘기기, 안 바꾸는 멤버 함수엔 `const`
- **숫자 직접 쓰지 말 것** → `config/Thresholds.h` 상수 사용 (300, 10분 등)
- 단위 통일: 거리 **미터(double)**, 시각 `SimTime`, 기간 `Seconds`(초, `minutes(10)` 같이 쓰기)
- 주석: 한국어 OK. 헤더의 public 함수엔 한 줄 주석으로 무엇을 하는지
- 아직 안 만든 부분은 `// TODO(담당자): 내용` → 검색으로 남은 일 확인
- 잘못된 입력은 `std::invalid_argument` + 원인 메시지. `catch(...)` 후 무시 금지
- 디버그용 `std::cout`은 merge 전에 제거
- 컴파일 경고 0개 유지 (`-Wall -Wextra -Wpedantic` 켜져 있음)

## 5. 모듈 간 약속 (인터페이스)
- 위치 1개 입력 → 판단: `Judgement j = engine.update(point);`
- 단계 바뀔 때 알림 받기: `AlertListener` 상속 후 `engine.addListener(&화면객체);`
  - 엔진은 리스너를 **소유하지 않음** → 화면 객체가 먼저 없어지면 `removeListener` 먼저
- 규칙 추가: `Rule` 상속 → `LearnedPatternStrategy` 생성자에서 `addRule(std::make_unique<새규칙>())` (엔진 수정 X)
- 위 인터페이스와 `LocationPoint`, `Trajectory` **헤더(.h) 바꿀 땐 단톡에 먼저 공유**
  → 클래스 다이어그램도 같이 갱신해야 함 (민규에게 알려주기)

## 6. 테스트
- `tests/engine_smoke_test.cpp` 참고 (assert 기반, 추가 라이브러리 없음)
- 규칙마다 테스트 최소 2개 (정상 1, 이상 1)
- PR 전 `ctest --test-dir build` 통과 확인
