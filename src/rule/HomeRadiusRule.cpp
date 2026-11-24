#include "rule/HomeRadiusRule.h"

namespace wander {

RuleResult HomeRadiusRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    // TODO(코드B): outing.latest()->distanceFromHome() > thresholds::kColdStartRadiusM 면
    //  return abnormal("집에서 Nm 떨어짐 (학습 전 기본 기준)");
    (void)outing;
    (void)profile;
    return ok();
}

}  // namespace wander
