#pragma once

#include "rule/Rule.h"

namespace wander {

// 같은 구역(격자 셀)을 반복해서 도는 움직임 -> 이상. 구현: 코드B
class RepetitiveLoopRule : public Rule {
public:
    RepetitiveLoopRule() : Rule("반복 배회") {}
    RuleResult evaluate(const Trajectory& outing, const PatternProfile& profile) const override;
};

}  // namespace wander
