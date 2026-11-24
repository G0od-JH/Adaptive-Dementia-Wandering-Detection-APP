#pragma once

#include <array>
#include <cstdint>
#include <unordered_set>

#include "model/LocationPoint.h"

namespace wander {

// 학습된 "평소 패턴" (불변).
// - usualCells: 최근 30일 주요 동선이 지나간 격자 셀 집합 (셀 키 = cellKey(x, y))
// - usualOutingHours[h]: h시에 평소 외출하는지
// - typicalOutingDuration: 평소 외출 1회 길이(초)
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

    // p에서 가장 가까운 주요 동선 셀 중심까지 거리(m). 동선 없으면 무한대
    double distanceToUsualRoute(const LocationPoint& p) const;

    // 격자 셀 키 (학습기·규칙에서 같이 씀)
    static CellKey cellKey(double x, double y);
    static CellKey cellKey(const LocationPoint& p) { return cellKey(p.x(), p.y()); }

private:
    int learnedDays_;
    std::unordered_set<CellKey> usualCells_;
    std::array<bool, 24> usualOutingHours_;
    Seconds typicalOutingDuration_;
};

}  // namespace wander
