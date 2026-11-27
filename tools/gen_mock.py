#!/usr/bin/env python3
"""
치매 배회 감지 앱 - 모의 위치 데이터 생성기 (코드A)

CSV 포맷 (data/FORMAT.md 참고)
  - 첫 줄 헤더: time,x,y
  - time : YYYY-MM-DDTHH:MM:SS (시간대 없음, 시뮬레이션 현지 시각)
  - x, y : 미터 단위 격자 좌표, 원점(0,0) = 집, x = 동쪽(+), y = 북쪽(+), 소수점 1자리
  - 1분 간격, 하루 00:00 ~ 23:59 = 1440줄 (집에 있을 때도 기록)
  - 파일 1개 = 하루 1개 = Trajectory 1개

사용법
  python3 tools/gen_mock.py sample            # data/sample/2026-11-24.csv (샘플 1일치)
  python3 tools/gen_mock.py full              # data/normal/ 30일 + data/scenario/ 이상 3종
"""
import math
import os
import random
import sys
from datetime import datetime, timedelta

WALK_M_PER_MIN = 50.0     # 어르신 보행 속도 약 3km/h
GPS_NOISE_M = 3.0         # 위치 오차
HOME = (0.0, 0.0)

# 자주 가는 곳 (격자 좌표, m)
PARK = (400.0, 250.0)       # 아침 산책 공원
SENIOR_CENTER = (-350.0, 600.0)  # 경로당
MART = (200.0, -300.0)      # 마트


def street_path(a, b):
    """격자 도로처럼 x 먼저, 그다음 y로 이동하는 경유점."""
    return [a, (b[0], a[1]), b]


def walk(waypoints):
    """경유점을 따라 1분 단위 위치 목록 생성 (출발점 제외, 도착점 포함)."""
    pts = []
    cur = waypoints[0]
    carry = 0.0
    for nxt in waypoints[1:]:
        seg = math.dist(cur, nxt)
        if seg == 0:
            continue
        d = WALK_M_PER_MIN - carry
        while d <= seg:
            t = d / seg
            pts.append((cur[0] + (nxt[0] - cur[0]) * t, cur[1] + (nxt[1] - cur[1]) * t))
            d += WALK_M_PER_MIN
        carry = seg - (d - WALK_M_PER_MIN)
        cur = nxt
    if not pts or math.dist(pts[-1], waypoints[-1]) > 1e-6:
        pts.append(waypoints[-1])
    return pts


def stay(pos, minutes):
    return [pos] * minutes


def round_trip(dest, linger_min):
    """집 → dest → (머무름) → 집"""
    go = walk(street_path(HOME, dest))
    back = walk(street_path(dest, HOME))
    return go + stay(dest, linger_min) + back


class Day:
    """하루 1440분 타임라인. 기본은 집, 외출 구간을 덮어쓴다."""

    def __init__(self, date, rng):
        self.date = date
        self.rng = rng
        self.pos = [HOME] * 1440

    def outing(self, start_hhmm, path):
        h, m = start_hhmm
        i = h * 60 + m
        for k, p in enumerate(path):
            if i + k < 1440:
                self.pos[i + k] = p

    def rows(self):
        base = datetime(self.date.year, self.date.month, self.date.day)
        out = []
        for i, (x, y) in enumerate(self.pos):
            t = base + timedelta(minutes=i)
            nx = x + self.rng.gauss(0, GPS_NOISE_M)
            ny = y + self.rng.gauss(0, GPS_NOISE_M)
            out.append(f"{t:%Y-%m-%dT%H:%M:%S},{nx:.1f},{ny:.1f}")
        return out


def jitter_min(rng, spread):
    return rng.randint(-spread, spread)


def normal_day(date, rng):
    """평소 하루: 아침 산책, (평일) 경로당, (가끔) 마트"""
    d = Day(date, rng)
    # 07:00 전후 공원 산책 (공원에서 10~20분)
    d.outing((7, 0 + jitter_min(rng, 10) + 10), round_trip(PARK, rng.randint(10, 20)))
    # 평일 10시 경로당 (1~2시간)
    if date.weekday() < 5:
        d.outing((10, rng.randint(0, 20)), round_trip(SENIOR_CENTER, rng.randint(60, 120)))
    # 이틀에 한 번꼴 오후 3시 마트
    if rng.random() < 0.5:
        d.outing((15, rng.randint(0, 30)), round_trip(MART, rng.randint(10, 25)))
    return d


def scenario_route_deviation(date, rng):
    """경로 이탈: 아침 산책 중 공원 대신 낯선 남서쪽으로 계속 걸어감"""
    d = Day(date, rng)
    lost = [HOME, (0.0, -200.0), (-500.0, -200.0), (-500.0, -700.0), (-900.0, -700.0), (-900.0, -1100.0)]
    # 길을 잃고 2시간가량 헤매다가 발견되어 집으로 돌아옴 (순간이동 없이)
    wander = [lost[-1], (-1000.0, -1100.0), (-1000.0, -1200.0), (-900.0, -1200.0), lost[-1]]
    path = walk(lost) + stay(lost[-1], 20)
    for _ in range(4):
        path += walk(wander)
    path += walk(list(reversed(lost)))
    d.outing((7, 5), path)
    return d


def scenario_night_outing(date, rng):
    """심야 외출: 새벽 2시 10분에 집을 나가 공원 쪽으로 (익숙한 경로지만 시간대 이상)"""
    d = normal_day(date, rng)
    d.outing((2, 10), round_trip(PARK, 25))
    return d


def scenario_repetitive_loop(date, rng):
    """같은 구역 반복: 공원 근처 한 블록(100m x 100m)을 60분 넘게 빙빙 돎"""
    d = Day(date, rng)
    go = walk(street_path(HOME, PARK))
    block = [PARK, (PARK[0] + 100, PARK[1]), (PARK[0] + 100, PARK[1] + 100), (PARK[0], PARK[1] + 100), PARK]
    loops = []
    for _ in range(18):  # 한 바퀴 400m = 8분, 18바퀴 ≈ 144분
        loops += walk(block)
    back = walk(street_path(PARK, HOME))
    d.outing((14, 0), go + loops + back)
    return d


def scenario_normal_long_walk(date, rng):
    """정상 산책(먼 길): 집에서 700m 떨어진 경로당까지 평소 다니던 길로만 다녀옴 (경보 X 기대)
    - 동쪽(공원)과 서쪽(경로당)은 집 앞 길로만 이어져 있어서, 집에 안 들르는 긴 경로는 만들 수 없음
    - 평소 외출 시간대(10시)에 출발 → 시간대 규칙에도 안 걸려야 함"""
    d = Day(date, rng)
    d.outing((10, 5), round_trip(SENIOR_CENTER, 15))
    return d


def write(path, day):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("time,x,y\n")
        f.write("\n".join(day.rows()))
        f.write("\n")
    print("wrote", path)


def main():
    mode = sys.argv[1] if len(sys.argv) > 1 else "sample"
    out = sys.argv[2] if len(sys.argv) > 2 else "data"
    if mode == "sample":
        rng = random.Random(1124)
        write(f"{out}/sample/2026-11-24.csv", normal_day(datetime(2026, 11, 24), rng))
    elif mode == "full":
        rng = random.Random(42)
        start = datetime(2026, 10, 1)
        for i in range(30):
            date = start + timedelta(days=i)
            write(f"{out}/normal/{date:%Y-%m-%d}.csv", normal_day(date, rng))
        write(f"{out}/scenario/route_deviation_2026-10-31.csv", scenario_route_deviation(datetime(2026, 10, 31), rng))
        write(f"{out}/scenario/night_outing_2026-10-31.csv", scenario_night_outing(datetime(2026, 10, 31), rng))
        write(f"{out}/scenario/repetitive_loop_2026-10-31.csv", scenario_repetitive_loop(datetime(2026, 10, 31), rng))
        write(f"{out}/scenario/normal_long_walk_2026-10-31.csv", scenario_normal_long_walk(datetime(2026, 10, 31), rng))
    else:
        print(__doc__)
        sys.exit(1)


if __name__ == "__main__":
    main()
