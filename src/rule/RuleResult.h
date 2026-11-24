#pragma once

#include <string>
#include <utility>

namespace wander {

// 규칙 하나의 판단 결과. abnormal == true면 reason에 화면·알림에 그대로 보여줄 한 줄 이유
struct RuleResult {
    std::string ruleName;
    bool abnormal = false;
    std::string reason;

    static RuleResult normal(std::string name) { return {std::move(name), false, ""}; }
    static RuleResult makeAbnormal(std::string name, std::string why) {
        return {std::move(name), true, std::move(why)};
    }
};

}  // namespace wander
