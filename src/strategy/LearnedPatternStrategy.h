#pragma once

#include "strategy/DetectionStrategy.h"

namespace wander {

// 학습 완료 후 판단: 평소 패턴 기준 4개 규칙
class LearnedPatternStrategy : public DetectionStrategy {
public:
    LearnedPatternStrategy();
    std::string name() const override { return "학습 패턴"; }
};

}  // namespace wander
