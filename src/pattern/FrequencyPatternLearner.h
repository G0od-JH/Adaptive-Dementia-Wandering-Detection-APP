#pragma once

#include "pattern/PatternLearner.h"

namespace wander {

// 빈도 기반 패턴 학습기. 구현: 코드B(건영)
// - 평소 동선: 학습한 날 중 20% 이상의 날에 지나간 격자 셀
// - 평소 외출 시간대: 20% 이상의 날에 그 시각(h시)에 밖에 있었으면 평소 시간대
// - 평소 외출 길이: 외출(집 반경 밖으로 나갔다 돌아오기) 길이의 90% 백분위 = "평소 긴 외출" (5분 미만은 제외)
// 비율·기준은 config/Thresholds.h
class FrequencyPatternLearner : public PatternLearner {
public:
    // pastDays: 하루 = Trajectory 1개 (그날 위치 기록 전체, 집에 있는 시간 포함)
    PatternProfile learn(const std::vector<Trajectory>& pastDays) const override;
};

}  // namespace wander
