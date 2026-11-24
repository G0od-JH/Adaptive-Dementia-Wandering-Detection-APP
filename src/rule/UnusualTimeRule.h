#pragma once

#include "rule/Rule.h"

namespace wander {

// 평소 외출하지 않는 시간대 외출 -> 이상. 콜드 스타트면 기본 심야(22~05시) 기준. 구현: 코드B
class UnusualTimeRule : public Rule {
public:
    UnusualTimeRule() : Rule("평소와 다른 시간대") {}
    RuleResult evaluate(const Trajectory& outing, const PatternProfile& profile) const override;
};

}  // namespace wander
