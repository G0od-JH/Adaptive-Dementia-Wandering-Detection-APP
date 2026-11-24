#pragma once

#include "strategy/DetectionStrategy.h"

namespace wander {

// 학습 전(설치 초기) 판단: 집 반경 1km + 기본 심야 시간대
class ColdStartStrategy : public DetectionStrategy {
public:
    ColdStartStrategy();
    std::string name() const override { return "콜드 스타트(기본 기준)"; }
};

}  // namespace wander
