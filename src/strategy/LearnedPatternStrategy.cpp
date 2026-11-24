#include "strategy/LearnedPatternStrategy.h"

#include "rule/LongOutingRule.h"
#include "rule/RepetitiveLoopRule.h"
#include "rule/RouteDeviationRule.h"
#include "rule/UnusualTimeRule.h"

namespace wander {

LearnedPatternStrategy::LearnedPatternStrategy() {
    addRule(std::make_unique<RouteDeviationRule>());
    addRule(std::make_unique<UnusualTimeRule>());
    addRule(std::make_unique<RepetitiveLoopRule>());
    addRule(std::make_unique<LongOutingRule>());
}

}  // namespace wander
