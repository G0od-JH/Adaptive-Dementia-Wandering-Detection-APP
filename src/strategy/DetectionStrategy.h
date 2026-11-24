#pragma once

#include <memory>
#include <string>
#include <vector>

#include "model/Trajectory.h"
#include "pattern/PatternProfile.h"
#include "rule/Rule.h"
#include "rule/RuleResult.h"

namespace wander {

// 어떤 규칙 묶음으로 판단할지 (Strategy 패턴, 추상 클래스).
// - ColdStartStrategy     : 학습 데이터 부족 -> 집 반경 + 심야
// - LearnedPatternStrategy: 학습 완료 -> 경로 이탈·시간대·반복·긴 외출
// 전환 기준: PatternProfile::isReliable()  (AlertEngine::setProfile에서 자동 전환)
class DetectionStrategy {
public:
    virtual ~DetectionStrategy() = default;

    virtual std::string name() const = 0;

    const std::vector<std::unique_ptr<Rule>>& rules() const { return rules_; }

    // 모든 규칙 결과 (정상 포함). 순서 = rules() 순서
    std::vector<RuleResult> evaluate(const Trajectory& outing, const PatternProfile& profile) const;

    // profile 상태에 맞는 전략 고르기
    static std::unique_ptr<DetectionStrategy> forProfile(const PatternProfile& profile);

protected:
    void addRule(std::unique_ptr<Rule> rule) { rules_.push_back(std::move(rule)); }

private:
    std::vector<std::unique_ptr<Rule>> rules_;  // 전략이 규칙들을 소유
};

}  // namespace wander
