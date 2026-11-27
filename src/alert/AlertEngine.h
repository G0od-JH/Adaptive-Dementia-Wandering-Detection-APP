#pragma once

#include <memory>
#include <optional>
#include <string>
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

    // 보호자가 등록한 자주 가는 장소. 학습 전 1km 기준·경로 이탈 판단에 같이 씀 (setProfile 뒤에도 유지)
    void setKnownPlaces(std::vector<KnownPlace> places);

    // 새 위치 1개 입력 -> 판단. 단계가 바뀌면 리스너들에게 AlertEvent 발송.
    // 집 반경 안으로 들어오면 외출 종료로 보고 기록 초기화 + 경보 끄기도 모두 풀림.
    // 경보 끄기 중이면 규칙 결과는 그대로 돌려주지만 단계는 올리지 않음 (Judgement::muted = true)
    Judgement update(const LocationPoint& p);

    // 어르신 "괜찮아요": 주의 단계에서만 가능 -> 정상, 15분간 다시 안 울림. 주의가 아니면 false
    bool elderOk(const LocationPoint& at);

    // 보호자 "알고 있는 외출": 어느 단계든 -> 정상, 집에 돌아올 때까지 안 울림
    void guardianKnownOuting(const LocationPoint& at);

    // 보호자 "찾았어요/괜찮아요" 해제 (긴급은 이것만 가능): 어느 단계든 -> 정상,
    // 30분간 조용히. 그 전에 집에 도착하면 바로 다시 감시
    void resolve(const LocationPoint& at);

    // 지금 경보 끄기 중인지
    bool isMuted(SimTime now) const;

    void addListener(AlertListener* listener);
    void removeListener(AlertListener* listener);

    AlertLevel level() const { return state_->level(); }
    const DetectionStrategy& strategy() const { return *strategy_; }
    const PatternProfile& profile() const { return profile_; }

    // 이번 외출 기록 (실종 신고 정보의 "최근 위치 기록"으로 사용)
    const Trajectory& currentOuting() const { return outing_; }

private:
    void fire(const AlertEvent& e);
    void forceNormal(const LocationPoint& at, const std::string& reason);  // 버튼 해제 공통 처리
    void clearMute();

    PatternProfile profile_;
    std::unique_ptr<DetectionStrategy> strategy_;
    std::unique_ptr<AlertState> state_;
    Trajectory outing_;
    std::optional<SimTime> abnormalSince_;      // 이상 연속 시작 시각, 정상이면 없음
    std::vector<KnownPlace> knownPlaces_;       // 보호자 등록 장소 (setProfile 때 다시 붙임)
    std::optional<SimTime> mutedUntil_;         // 이 시각 전까지 경보 끄기
    bool mutedUntilHome_ = false;               // 귀가할 때까지 경보 끄기
    std::vector<AlertListener*> listeners_;     // 소유 X
};

}  // namespace wander
