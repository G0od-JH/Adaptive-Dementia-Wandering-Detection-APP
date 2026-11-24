#pragma once

#include <string>
#include <utility>

#include "model/Trajectory.h"
#include "pattern/PatternProfile.h"
#include "rule/RuleResult.h"

namespace wander {

// 이상 판단 규칙 (상속·다형성 포인트, 추상 클래스).
// 새 규칙 추가 = Rule 상속 클래스 하나 만들고 Strategy 생성자에서 rules_에 넣기. 엔진 코드는 안 바뀜.
class Rule {
public:
    explicit Rule(std::string name) : name_(std::move(name)) {}
    virtual ~Rule() = default;

    Rule(const Rule&) = delete;
    Rule& operator=(const Rule&) = delete;

    const std::string& name() const { return name_; }

    // outing : 이번 외출의 위치 기록 (집 출발 ~ 현재). 비어 있을 수 있음
    // profile: 평소 패턴 (콜드 스타트면 PatternProfile::coldStart())
    virtual RuleResult evaluate(const Trajectory& outing, const PatternProfile& profile) const = 0;

protected:
    RuleResult ok() const { return RuleResult::normal(name_); }
    RuleResult abnormal(std::string reason) const { return RuleResult::makeAbnormal(name_, std::move(reason)); }

private:
    std::string name_;
};

}  // namespace wander
