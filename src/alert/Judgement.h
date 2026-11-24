#pragma once

#include <string>
#include <vector>

#include "model/AlertLevel.h"
#include "rule/RuleResult.h"

namespace wander {

// AlertEngine::update() 반환값: 현재 경보 단계 + 규칙별 결과
struct Judgement {
    AlertLevel level = AlertLevel::Normal;
    std::vector<RuleResult> results;

    bool isAbnormal() const {
        for (const auto& r : results) {
            if (r.abnormal) return true;
        }
        return false;
    }

    // 이상 규칙의 이유 문장만
    std::vector<std::string> reasons() const {
        std::vector<std::string> out;
        for (const auto& r : results) {
            if (r.abnormal) out.push_back(r.reason);
        }
        return out;
    }
};

}  // namespace wander
