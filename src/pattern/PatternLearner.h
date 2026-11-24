#pragma once

#include <vector>

#include "model/Trajectory.h"
#include "pattern/PatternProfile.h"

namespace wander {

// 과거 기록(하루 = Trajectory 1개)으로 평소 패턴 학습 (인터페이스). 구현: 코드B.
// 예: FrequencyPatternLearner - 셀 방문 빈도 상위, 시간대별 외출 비율, 외출 길이 중앙값.
class PatternLearner {
public:
    virtual ~PatternLearner() = default;

    // pastDays가 비어 있으면 PatternProfile::coldStart() 반환
    virtual PatternProfile learn(const std::vector<Trajectory>& pastDays) const = 0;
};

}  // namespace wander
