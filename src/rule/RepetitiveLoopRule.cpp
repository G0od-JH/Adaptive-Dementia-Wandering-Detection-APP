#include "rule/RepetitiveLoopRule.h"

#include <algorithm>
#include <optional>
#include <string>
#include <unordered_map>

#include "config/Thresholds.h"

namespace wander {

RuleResult RepetitiveLoopRule::evaluate(const Trajectory& outing, const PatternProfile& /*profile*/) const {
    using CellKey = PatternProfile::CellKey;

    std::unordered_map<CellKey, int> entries;      // 셀별 진입 횟수 (연속으로 같은 셀은 1회)
    std::unordered_map<CellKey, double> leftAtM;   // 셀을 마지막으로 떠난 시점의 누적 이동 거리
    std::optional<CellKey> prevCell;
    std::optional<LocationPoint> prevPoint;
    double walkedM = 0;
    int maxEntries = 0;

    for (const auto& p : outing) {
        if (prevPoint) walkedM += prevPoint->distanceTo(p);
        const CellKey cell = PatternProfile::cellKey(p);

        if (!prevCell || *prevCell != cell) {
            if (prevCell) leftAtM[*prevCell] = walkedM;
            const auto left = leftAtM.find(cell);
            // 처음 들어온 셀이거나, 떠났다가 충분히 돌아다닌 뒤 다시 온 경우만 셈
            if (left == leftAtM.end() || walkedM - left->second >= thresholds::kLoopMinDetourM) {
                maxEntries = std::max(maxEntries, ++entries[cell]);
            }
        }
        prevCell = cell;
        prevPoint = p;
    }

    if (maxEntries - 1 < thresholds::kLoopRevisitCount) return ok();
    return abnormal("같은 구역을 " + std::to_string(maxEntries) + "번 반복해서 지나감");
}

}  // namespace wander
