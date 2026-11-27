#include "alert/AlertEngine.h"

#include <algorithm>
#include <stdexcept>
#include <string>
#include <utility>

#include "alert/AlertStates.h"
#include "config/Thresholds.h"

namespace wander {

AlertEngine::AlertEngine(PatternProfile profile)
    : profile_(std::move(profile)),
      strategy_(DetectionStrategy::forProfile(profile_)),
      state_(std::make_unique<NormalState>()) {}

void AlertEngine::setProfile(PatternProfile profile) {
    profile_ = knownPlaces_.empty() ? std::move(profile) : profile.withKnownPlaces(knownPlaces_);
    strategy_ = DetectionStrategy::forProfile(profile_);
}

void AlertEngine::setKnownPlaces(std::vector<KnownPlace> places) {
    profile_ = profile_.withKnownPlaces(places);  // NaN 검사도 여기서
    knownPlaces_ = std::move(places);
}

void AlertEngine::setStrategy(std::unique_ptr<DetectionStrategy> strategy) {
    if (!strategy) throw std::invalid_argument("strategy가 nullptr");
    strategy_ = std::move(strategy);
}

Judgement AlertEngine::update(const LocationPoint& p) {
    if (p.distanceFromHome() <= thresholds::kHomeRadiusM) {
        outing_.clear();
        clearMute();  // 귀가 = 경보 끄기 끝, 다음 외출부터 다시 감시
    } else {
        outing_.add(p);
    }

    Judgement j{state_->level(), strategy_->evaluate(outing_, profile_)};

    if (isMuted(p.time())) {
        abnormalSince_.reset();  // 끄기가 끝나면 그때부터 다시 시간 셈
        j.muted = true;
        return j;
    }
    mutedUntil_.reset();

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

bool AlertEngine::elderOk(const LocationPoint& at) {
    if (state_->level() != AlertLevel::Caution) return false;
    forceNormal(at, "어르신이 '괜찮아요'를 누름 (" + std::to_string(thresholds::kElderOkSnooze / minutes(1)) +
                         "분간 알림 끔)");
    mutedUntil_ = at.time().plus(thresholds::kElderOkSnooze);
    return true;
}

void AlertEngine::guardianKnownOuting(const LocationPoint& at) {
    forceNormal(at, "보호자가 '알고 있는 외출'로 확인 (귀가할 때까지 알림 끔)");
    mutedUntilHome_ = true;
}

void AlertEngine::resolve(const LocationPoint& at) {
    forceNormal(at, "보호자 확인으로 해제 (" + std::to_string(thresholds::kGuardianResolveQuiet / minutes(1)) +
                         "분간 알림 끔, 귀가하면 다시 감시)");
    mutedUntil_ = at.time().plus(thresholds::kGuardianResolveQuiet);
}

bool AlertEngine::isMuted(SimTime now) const {
    return mutedUntilHome_ || (mutedUntil_ && now < *mutedUntil_);
}

void AlertEngine::forceNormal(const LocationPoint& at, const std::string& reason) {
    const AlertLevel from = state_->level();
    state_ = std::make_unique<NormalState>();
    abnormalSince_.reset();
    if (from != AlertLevel::Normal) {
        fire(AlertEvent{at.time(), from, AlertLevel::Normal, {reason}, at});
    }
}

void AlertEngine::clearMute() {
    mutedUntil_.reset();
    mutedUntilHome_ = false;
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
