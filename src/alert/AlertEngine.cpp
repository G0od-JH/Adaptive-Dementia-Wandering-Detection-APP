#include "alert/AlertEngine.h"

#include <algorithm>
#include <stdexcept>
#include <utility>

#include "alert/AlertStates.h"
#include "config/Thresholds.h"

namespace wander {

AlertEngine::AlertEngine(PatternProfile profile)
    : profile_(std::move(profile)),
      strategy_(DetectionStrategy::forProfile(profile_)),
      state_(std::make_unique<NormalState>()) {}

void AlertEngine::setProfile(PatternProfile profile) {
    profile_ = std::move(profile);
    strategy_ = DetectionStrategy::forProfile(profile_);
}

void AlertEngine::setStrategy(std::unique_ptr<DetectionStrategy> strategy) {
    if (!strategy) throw std::invalid_argument("strategy가 nullptr");
    strategy_ = std::move(strategy);
}

Judgement AlertEngine::update(const LocationPoint& p) {
    if (p.distanceFromHome() <= thresholds::kHomeRadiusM) {
        outing_.clear();
    } else {
        outing_.add(p);
    }

    Judgement j{state_->level(), strategy_->evaluate(outing_, profile_)};
    const bool abnormal = j.isAbnormal();

    if (abnormal && !abnormalSince_) {
        abnormalSince_ = p.time();
    } else if (!abnormal) {
        abnormalSince_.reset();
    }
    const Seconds abnormalFor = abnormal ? p.time() - *abnormalSince_ : 0;

    if (auto nextState = state_->next(abnormal, abnormalFor)) {
        const AlertLevel from = state_->level();
        state_ = std::move(nextState);
        if (state_->level() != from) {
            fire(AlertEvent{p.time(), from, state_->level(), j.reasons(), p});
        }
    }
    j.level = state_->level();
    return j;
}

void AlertEngine::resolve(const LocationPoint& at) {
    const AlertLevel from = state_->level();
    state_ = std::make_unique<NormalState>();
    abnormalSince_.reset();
    if (from != AlertLevel::Normal) {
        fire(AlertEvent{at.time(), from, AlertLevel::Normal, {"보호자 확인으로 해제"}, at});
    }
}

void AlertEngine::addListener(AlertListener* listener) {
    if (!listener) throw std::invalid_argument("listener가 nullptr");
    if (std::find(listeners_.begin(), listeners_.end(), listener) == listeners_.end()) {
        listeners_.push_back(listener);
    }
}

void AlertEngine::removeListener(AlertListener* listener) {
    listeners_.erase(std::remove(listeners_.begin(), listeners_.end(), listener), listeners_.end());
}

void AlertEngine::fire(const AlertEvent& e) {
    auto snapshot = listeners_;  // 알림 도중 add/remove 해도 안전하게 복사본으로 돌림
    for (AlertListener* l : snapshot) {
        l->onAlertChanged(e);
    }
}

}  // namespace wander
