#include "rule/RouteDeviationRule.h"

#include "config/Thresholds.h"

namespace wander {

RuleResult RouteDeviationRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    // TODO(코드B): 최근 점부터 거꾸로 보며 profile.distanceToUsualRoute(p) > thresholds::kRouteDeviationM 인
    //  연속 구간 길이 계산. 구간 길이 >= thresholds::kRouteDeviationMin 이면
    //  return abnormal("평소 경로에서 약 Nm, M분째 이탈");
    (void)outing;
    (void)profile;
    return ok();
}

}  // namespace wander
