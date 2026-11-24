#include "alert/AlertStates.h"

#include "config/Thresholds.h"

namespace wander {

std::unique_ptr<AlertState> NormalState::next(bool abnormal, Seconds /*abnormalFor*/) const {
    if (abnormal) return std::make_unique<CautionState>();
    return nullptr;
}

std::unique_ptr<AlertState> CautionState::next(bool abnormal, Seconds abnormalFor) const {
    if (!abnormal) return std::make_unique<NormalState>();
    if (abnormalFor >= thresholds::kCautionToWarning) return std::make_unique<WarningState>();
    return nullptr;
}

std::unique_ptr<AlertState> WarningState::next(bool abnormal, Seconds abnormalFor) const {
    if (!abnormal) return std::make_unique<NormalState>();
    if (abnormalFor >= thresholds::kWarningToEmergency) return std::make_unique<EmergencyState>();
    return nullptr;
}

std::unique_ptr<AlertState> EmergencyState::next(bool /*abnormal*/, Seconds /*abnormalFor*/) const {
    return nullptr;
}

}  // namespace wander
