#include "pattern/FrequencyPatternLearner.h"

#include <algorithm>
#include <array>
#include <cmath>
#include <optional>
#include <unordered_map>
#include <unordered_set>
#include <vector>

#include "config/Thresholds.h"

namespace wander {

namespace {

// n일 중 ratio 이상이 되려면 최소 며칠이어야 하는지 (최소 1일)
int minDays(int n, double ratio) {
    return std::max(1, static_cast<int>(std::ceil(n * ratio)));
}

// 비어 있지 않은 values의 ratio 백분위 값 (0.9 = 하위 90% 지점)
Seconds percentile(std::vector<Seconds> values, double ratio) {
    std::sort(values.begin(), values.end());
    const auto rank = static_cast<std::size_t>(std::ceil(ratio * static_cast<double>(values.size())));
    const std::size_t index = std::min(values.size() - 1, rank == 0 ? 0 : rank - 1);
    return values[index];
}

}  // namespace

PatternProfile FrequencyPatternLearner::learn(const std::vector<Trajectory>& pastDays) const {
    if (pastDays.empty()) return PatternProfile::coldStart();

    using CellKey = PatternProfile::CellKey;
    std::unordered_map<CellKey, int> cellDays;   // 셀별: 지나간 날 수
    std::array<int, 24> hourDays{};              // 시각별: 밖에 있었던 날 수
    std::vector<Seconds> outingDurations;

    for (const Trajectory& day : pastDays) {
        std::unordered_set<CellKey> cellsToday;
        std::array<bool, 24> outToday{};
        std::optional<SimTime> outingStart;

        for (const LocationPoint& p : day) {
            const bool outside = p.distanceFromHome() > thresholds::kHomeRadiusM;
            if (outside) {
                cellsToday.insert(PatternProfile::cellKey(p));
                outToday[static_cast<std::size_t>(p.hour())] = true;
                if (!outingStart) outingStart = p.time();
            } else if (outingStart) {  // 귀가 -> 외출 1회 끝
                const Seconds d = p.time() - *outingStart;
                if (d >= thresholds::kMinOutingDuration) outingDurations.push_back(d);
                outingStart.reset();
            }
        }
        // 하루 기록이 밖에서 끝난 경우 (자정 넘긴 외출)
        if (outingStart && !day.empty()) {
            const Seconds d = day.latest()->time() - *outingStart;
            if (d >= thresholds::kMinOutingDuration) outingDurations.push_back(d);
        }

        for (CellKey c : cellsToday) ++cellDays[c];
        for (std::size_t h = 0; h < outToday.size(); ++h) {
            if (outToday[h]) ++hourDays[h];
        }
    }

    const int n = static_cast<int>(pastDays.size());

    std::unordered_set<CellKey> usualCells;
    const int cellCut = minDays(n, thresholds::kUsualCellMinDayRatio);
    for (const auto& [cell, days] : cellDays) {
        if (days >= cellCut) usualCells.insert(cell);
    }

    std::array<bool, 24> usualHours{};
    const int hourCut = minDays(n, thresholds::kUsualHourMinDayRatio);
    for (std::size_t h = 0; h < usualHours.size(); ++h) {
        usualHours[h] = hourDays[h] >= hourCut;
    }

    const Seconds typical =
        outingDurations.empty() ? thresholds::kDefaultOutingDuration
                               : percentile(outingDurations, thresholds::kTypicalOutingPercentile);

    return PatternProfile(n, std::move(usualCells), usualHours, typical);
}

}  // namespace wander
