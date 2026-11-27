#include "rule/UnusualTimeRule.h"

#include <string>

namespace wander {

RuleResult UnusualTimeRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    const auto p = outing.latest();
    if (!p) return ok();

    const int h = p->hour();
    if (profile.isUsualOutingHour(h)) return ok();
    return abnormal("평소 외출하지 않는 " + std::to_string(h) + "시에 외출 중");
}

}  // namespace wander
