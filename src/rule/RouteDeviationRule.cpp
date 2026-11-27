#include "rule/RouteDeviationRule.h"

#include <cmath>
#include <optional>
#include <string>

#include "config/Thresholds.h"

namespace wander {

RuleResult RouteDeviationRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    const auto& points = outing.points();
    if (points.empty()) return ok();

    // 최근 점부터 거꾸로 보며 "평소 경로에서 300m 넘게 벗어난" 연속 구간의 시작 시각 찾기
    std::optional<SimTime> deviationStart;
    for (auto it = points.rbegin(); it != points.rend(); ++it) {
        if (profile.distanceToUsualRoute(*it) <= thresholds::kRouteDeviationM) break;
        deviationStart = it->time();
    }
    if (!deviationStart) return ok();

    const Seconds deviatedFor = points.back().time() - *deviationStart;
    if (deviatedFor < thresholds::kRouteDeviationMin) return ok();

    const double d = profile.distanceToUsualRoute(points.back());
    return abnormal("평소 경로에서 약 " + std::to_string(std::lround(d)) + "m, " +
                    std::to_string(deviatedFor / minutes(1)) + "분째 이탈");
}

}  // namespace wander
