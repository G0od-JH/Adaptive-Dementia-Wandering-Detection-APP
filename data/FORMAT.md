# 모의데이터 포맷 (코드A, 11/24 확정안)

## 좌표계
- **격자 좌표, 단위 m**. 원점 (0, 0) = 어르신 집
- x = 동쪽(+), y = 북쪽(+)
- 위경도는 쓰지 않음 → 거리 계산이 `hypot(dx, dy)` 하나로 끝남

## CSV 한 줄
```
time,x,y
2026-11-24T08:30:00,120.5,340.0
```
- 첫 줄은 헤더 `time,x,y` → `MockDataLoader`가 건너뜀 (`LocationPoint::fromCsv`에는 데이터 줄만 넘김)
- time: `YYYY-MM-DDTHH:MM:SS`, 시간대 표기 없음 (`SimTime::parse` 대상)
- x, y: 소수점 1자리, GPS 오차 ±3m 정도 섞여 있음
- **1분 간격, 하루 1440줄**. 집에 있을 때도 (0, 0) 근처 값으로 기록

## 파일 구성
| 경로 | 내용 |
|---|---|
| `data/sample/2026-11-24.csv` | 샘플 1일치 (오늘 공유용) |
| `data/normal/2026-10-01.csv` ~ `2026-10-30.csv` | 정상 30일 (11/26 v1) |
| `data/scenario/route_deviation_*.csv` | 경로 이탈: 아침에 낯선 남서쪽으로 약 1.4km |
| `data/scenario/night_outing_*.csv` | 심야 외출: 02:10에 공원 쪽 외출 |
| `data/scenario/repetitive_loop_*.csv` | 같은 구역 반복: 공원 옆 100m 블록을 2시간 넘게 돎 |
| `data/scenario/normal_long_walk_*.csv` | 정상 산책(먼 길): 10시에 집에서 700m 떨어진 경로당까지, 전부 평소 길 → **경보 나오면 안 됨** (오경보 테스트) |

- **파일 1개 = 하루 = `Trajectory` 1개** → `MockDataLoader::load(dir)`가 파일별로 `Trajectory`를 만들어 `vector<Trajectory>`로 반환
- 이상 시나리오 정답은 파일 이름으로 구분 (CSV 열 추가 없음 → `fromCsv` 포맷 고정)

## 평소 생활 패턴 (학습 대상)
| 시간 | 행선지 | 좌표 |
|---|---|---|
| 07:00 전후 | 공원 산책 | (400, 250) |
| 평일 10시~ | 경로당 1~2시간 | (-350, 600) |
| 이틀에 한 번 15시~ | 마트 | (200, -300) |

도로는 격자처럼 x 방향으로 먼저, 그다음 y 방향으로 이동. 보행 속도는 50m/분.

## 생성
```
python3 tools/gen_mock.py sample   # 샘플 1일치
python3 tools/gen_mock.py full     # 정상 30일 + 시나리오 4종
```
seed가 고정돼 있어서 누가 돌려도 같은 파일이 나옴.

## 안드로이드 앱에서
`app/build.gradle.kts`에서 이 `data/` 폴더를 assets로 잡아 두었음 → 앱에서는 `normal/2026-10-01.csv`, `scenario/route_deviation_2026-10-31.csv` 경로로 읽음 (`com.adwd.app.sim.MockDataRepository`).
