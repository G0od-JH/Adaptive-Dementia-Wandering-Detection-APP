#pragma once

#include <memory>
#include <optional>
#include <vector>

#include "alert/AlertListener.h"
#include "alert/AlertState.h"
#include "alert/Judgement.h"
#include "model/Trajectory.h"
#include "pattern/PatternProfile.h"
#include "strategy/DetectionStrategy.h"

namespace wander {

// 판단 엔진 (State의 Context, Observer의 Subject). 담당: 코드B.
//
// 사용법 (코드A 메인 루프):
//   AlertEngine engine(profile);
//   engine.addListener(&guardianScreen);          // 지후 화면들 등록
//   for (const auto& p : 시뮬레이션) {
//       Judgement j = engine.update(p);           // 단계 + 이유
//   }
class AlertEngine {
public:
    explicit AlertEngine(PatternProfile profile);

    // 학습 결과 교체. 데이터 양에 따라 Strategy 자동 전환 (콜드 스타트 <-> 학습 패턴)
    void setProfile(PatternProfile profile);

    // 데모·테스트용 강제 지정
    void setStrategy(std::unique_ptr<DetectionStrategy> strategy);

    // 새 위치 1개 입력 -> 판단. 단계가 바뀌면 리스너들에게 AlertEvent 발송.
    // 집 반경 안으로 들어오면 외출 종료로 보고 기록 초기화.
    Judgement update(const LocationPoint& p);

    // 보호자가 "찾았음/확인함" 누름 -> Normal로 해제
    void resolve(const LocationPoint& at);

    void addListener(AlertListener* listener);
    void removeListener(AlertListener* listener);

    AlertLevel level() const { return state_->level(); }
    const DetectionStrategy& strategy() const { return *strategy_; }
    const PatternProfile& profile() const { return profile_; }

    // 이번 외출 기록 (실종 신고 정보의 "최근 위치 기록"으로 사용)
    const Trajectory& currentOuting() const { return outing_; }

private:
    void fire(const AlertEvent& e);

    PatternProfile profile_;
    std::unique_ptr<DetectionStrategy> strategy_;
    std::unique_ptr<AlertState> state_;
    Trajectory outing_;
    std::optional<SimTime> abnormalSince_;      // 이상 연속 시작 시각, 정상이면 없음
    std::vector<AlertListener*> listeners_;     // 소유 X
};

}  // namespace wander
