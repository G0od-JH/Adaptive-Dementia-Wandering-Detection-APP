#include "rule/LongOutingRule.h"

#include <string>

#include "config/Thresholds.h"

namespace wander {

RuleResult LongOutingRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    const Seconds elapsed = outing.duration();
    const double limit = static_cast<double>(profile.typicalOutingDuration()) * thresholds::kLongOutingFactor;
    if (static_cast<double>(elapsed) <= limit) return ok();

    return abnormal("평소보다 긴 외출 (" + std::to_string(elapsed / minutes(1)) + "분째, 평소 약 " +
                    std::to_string(profile.typicalOutingDuration() / minutes(1)) + "분)");
}

}  // namespace wander
