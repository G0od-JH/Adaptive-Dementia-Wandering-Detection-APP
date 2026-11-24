#include "rule/LongOutingRule.h"

namespace wander {

RuleResult LongOutingRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    // TODO(코드B): outing.duration() > profile.typicalOutingDuration() * thresholds::kLongOutingFactor 면
    //  return abnormal("평소보다 긴 외출 (N분째)");
    (void)outing;
    (void)profile;
    return ok();
}

}  // namespace wander
