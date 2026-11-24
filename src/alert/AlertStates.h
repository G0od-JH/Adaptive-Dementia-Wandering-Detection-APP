#pragma once

#include "alert/AlertState.h"

namespace wander {

class NormalState : public AlertState {
public:
    AlertLevel level() const override { return AlertLevel::Normal; }
    std::unique_ptr<AlertState> next(bool abnormal, Seconds abnormalFor) const override;
};

// 주의: 어르신 본인에게 큰 글씨·음성 귀가 안내
class CautionState : public AlertState {
public:
    AlertLevel level() const override { return AlertLevel::Caution; }
    std::unique_ptr<AlertState> next(bool abnormal, Seconds abnormalFor) const override;
};

// 경고: 보호자 알림
class WarningState : public AlertState {
public:
    AlertLevel level() const override { return AlertLevel::Warning; }
    std::unique_ptr<AlertState> next(bool abnormal, Seconds abnormalFor) const override;
};

// 긴급: 실종 신고 정보 생성. 자동으로 안 내려감 -> 보호자가 AlertEngine::resolve()로만 해제
class EmergencyState : public AlertState {
public:
    AlertLevel level() const override { return AlertLevel::Emergency; }
    std::unique_ptr<AlertState> next(bool abnormal, Seconds abnormalFor) const override;
};

}  // namespace wander
