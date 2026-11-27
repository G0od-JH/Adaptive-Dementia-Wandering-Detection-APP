#include "rule/HomeRadiusRule.h"

#include <cmath>
#include <string>

#include "config/Thresholds.h"

namespace wander {

RuleResult HomeRadiusRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    const auto p = outing.latest();
    if (!p) return ok();

    const double d = profile.distanceToNearestKnownPlace(*p);
    if (d <= thresholds::kColdStartRadiusM) return ok();

    const std::string where = profile.knownPlaces().empty() ? "집" : "집·등록 장소";
    return abnormal(where + "에서 약 " + std::to_string(std::lround(d)) + "m 떨어짐 (학습 전 기본 기준)");
}

}  // namespace wander
