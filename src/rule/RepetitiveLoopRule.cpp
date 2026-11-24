#include "rule/RepetitiveLoopRule.h"

namespace wander {

RuleResult RepetitiveLoopRule::evaluate(const Trajectory& outing, const PatternProfile& profile) const {
    // TODO(코드B): PatternProfile::cellKey(p)로 셀 진입 횟수 세기 (연속으로 같은 셀은 1회)
    //  어떤 셀이든 재방문 >= thresholds::kLoopRevisitCount 면 return abnormal("같은 구역을 N번 반복");
    (void)outing;
    (void)profile;
    return ok();
}

}  // namespace wander
