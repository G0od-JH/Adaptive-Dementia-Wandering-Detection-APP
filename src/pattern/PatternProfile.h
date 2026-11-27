#pragma once

#include <array>
#include <cstdint>
#include <string>
#include <unordered_set>
#include <vector>

#include "model/LocationPoint.h"

namespace wander {

// 보호자가 등록한 자주 가는 장소 (경로당·병원 등). 좌표는 LocationPoint와 같은 평면 좌표(m, 집 = 원점)
struct KnownPlace {
    std::string name;
    double x = 0;
    double y = 0;
};

// 학습된 "평소 패턴" (불변).
// - usualCells: 최근 30일 주요 동선이 지나간 격자 셀 집합 (셀 키 = cellKey(x, y))
// - usualOutingHours[h]: h시에 평소 외출하는지
// - typicalOutingDuration: 평소 외출 1회 길이(초)
// - knownPlaces: 보호자가 등록한 자주 가는 장소 (학습 전후 모두 "익숙한 곳"으로 취급)
// PatternLearner 구현체(코드B)가 만들어 줌. 학습 전엔 coldStart() 사용.
class PatternProfile {
public:
    using CellKey = std::int64_t;

    PatternProfile(int learnedDays, std::unordered_set<CellKey> usualCells,
                   std::array<bool, 24> usualOutingHours, Seconds typicalOutingDuration);

    // 학습 데이터 없을 때 기본값: 동선 없음, 05~22시 외출 정상, 평소 외출 1시간
    static PatternProfile coldStart();

    int learnedDays() const { return learnedDays_; }
    bool isReliable() const;  // 학습 데이터 충분한지 (Strategy 전환 기준)
    bool isUsualOutingHour(int hour) const;
    Seconds typicalOutingDuration() const { return typicalOutingDuration_; }
    const std::unordered_set<CellKey>& usualCells() const { return usualCells_; }

    // 등록 장소를 바꾼 복사본 (x/y가 NaN이면 std::invalid_argument)
    PatternProfile withKnownPlaces(std::vector<KnownPlace> places) const;
    const std::vector<KnownPlace>& knownPlaces() const { return knownPlaces_; }

    // p에서 집·등록 장소 중 가장 가까운 곳까지 거리(m). 콜드 스타트 반경 판단용
    double distanceToNearestKnownPlace(const LocationPoint& p) const;

    // p에서 가장 가까운 "익숙한 곳"까지 거리(m): 주요 동선 셀 중심, 집, 등록 장소 중 최소
    double distanceToUsualRoute(const LocationPoint& p) const;

    // 격자 셀 키 (학습기·규칙에서 같이 씀)
    static CellKey cellKey(double x, double y);
    static CellKey cellKey(const LocationPoint& p) { return cellKey(p.x(), p.y()); }

private:
    int learnedDays_;
    std::unordered_set<CellKey> usualCells_;
    std::array<bool, 24> usualOutingHours_;
    Seconds typicalOutingDuration_;
    std::vector<KnownPlace> knownPlaces_;
};

}  // namespace wander
