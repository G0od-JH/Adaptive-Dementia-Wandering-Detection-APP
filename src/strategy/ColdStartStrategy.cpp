#include "strategy/ColdStartStrategy.h"

#include "rule/HomeRadiusRule.h"
#include "rule/UnusualTimeRule.h"

namespace wander {

ColdStartStrategy::ColdStartStrategy() {
    addRule(std::make_unique<HomeRadiusRule>());
    addRule(std::make_unique<UnusualTimeRule>());
}

}  // namespace wander
