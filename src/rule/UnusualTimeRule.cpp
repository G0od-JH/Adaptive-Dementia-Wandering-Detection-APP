#include "rule/UnusualTimeRule.h"

namespace wander {

RuleResult UnusualTimeRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    // TODO(코드B): outing.latest()가 있고 !profile.isUsualOutingHour(p.hour()) 면
    //  return abnormal("평소 외출하지 않는 h시에 외출 중");
    (void)outing;
    (void)profile;
    return ok();
}

}  // namespace wander
