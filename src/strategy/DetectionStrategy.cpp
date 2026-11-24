#include "strategy/DetectionStrategy.h"

#include "strategy/ColdStartStrategy.h"
#include "strategy/LearnedPatternStrategy.h"

namespace wander {

std::vector<RuleResult> DetectionStrategy::evaluate(const Trajectory& outing,
                                                    const PatternProfile& profile) const {
    std::vector<RuleResult> results;
    results.reserve(rules_.size());
    for (const auto& rule : rules_) {
        results.push_back(rule->evaluate(outing, profile));  // 가상 함수 호출 = 다형성
    }
    return results;
}

std::unique_ptr<DetectionStrategy> DetectionStrategy::forProfile(const PatternProfile& profile) {
    if (profile.isReliable()) {
        return std::make_unique<LearnedPatternStrategy>();
    }
    return std::make_unique<ColdStartStrategy>();
}

}  // namespace wander
