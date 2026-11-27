// 경보 끄기 3종(어르신 괜찮아요 / 알고 있는 외출 / 보호자 해제) + 등록 장소 테스트. 작성: 건영
#undef NDEBUG
#include <cassert>
#include <iostream>
#include <memory>
#include <vector>

#include "alert/AlertEngine.h"
#include "pattern/PatternProfile.h"

using namespace wander;

namespace {

class Recorder : public AlertListener {
public:
    std::vector<AlertEvent> events;
    void onAlertChanged(const AlertEvent& e) override { events.push_back(e); }
};

const SimTime t0 = SimTime::of(2026, 11, 24, 10, 0);

// 콜드 스타트에서 집 1.2km 지점에 계속 서 있음 -> 매 분 이상
LocationPoint farAt(int minute) { return LocationPoint(t0.plus(minutes(minute)), 1200, 0); }
LocationPoint homeAt(int minute) { return LocationPoint(t0.plus(minutes(minute)), 0, 0); }

void testElderOk() {
    AlertEngine engine(PatternProfile::coldStart());
    engine.update(farAt(0));
    assert(engine.level() == AlertLevel::Caution);

    assert(engine.elderOk(farAt(1)));                 // 주의에서 괜찮아요 -> 정상
    assert(engine.level() == AlertLevel::Normal);
    for (int m = 2; m < 16; ++m) {                    // 15분 동안은 이상이어도 안 울림
        Judgement j = engine.update(farAt(m));
        assert(j.muted && j.isAbnormal() && engine.level() == AlertLevel::Normal);
    }
    engine.update(farAt(16));                         // 15분 지나면 다시 주의
    assert(engine.level() == AlertLevel::Caution);

    engine.update(farAt(26));                         // 경고 단계에선 괜찮아요 불가
    assert(engine.level() == AlertLevel::Warning);
    assert(!engine.elderOk(farAt(27)) && engine.level() == AlertLevel::Warning);
}

void testGuardianKnownOuting() {
    AlertEngine engine(PatternProfile::coldStart());
    Recorder rec;
    engine.addListener(&rec);
    engine.update(farAt(0));
    engine.update(farAt(10));
    assert(engine.level() == AlertLevel::Warning);

    engine.guardianKnownOuting(farAt(11));            // 어느 단계든 -> 정상
    assert(engine.level() == AlertLevel::Normal && rec.events.back().to == AlertLevel::Normal);
    for (int m = 12; m < 120; m += 5) {               // 귀가 전까지 몇 시간이든 안 울림
        engine.update(farAt(m));
        assert(engine.level() == AlertLevel::Normal);
    }
    engine.update(homeAt(120));                       // 귀가
    engine.update(farAt(130));                        // 다음 외출은 다시 감시
    assert(engine.level() == AlertLevel::Caution && !engine.isMuted(t0.plus(minutes(130))));
}

void testResolveQuietThenHome() {
    AlertEngine engine(PatternProfile::coldStart());
    engine.update(farAt(0));
    engine.update(farAt(10));
    engine.update(farAt(40));
    assert(engine.level() == AlertLevel::Emergency);

    engine.resolve(farAt(41));                        // 보호자 해제 -> 30분 조용히
    assert(engine.level() == AlertLevel::Normal);
    engine.update(farAt(42));
    engine.update(farAt(70));
    assert(engine.level() == AlertLevel::Normal);
    engine.update(farAt(71));                         // 30분 지나면 다시 주의
    assert(engine.level() == AlertLevel::Caution);

    // 해제 후 30분 안에 집에 오면 바로 다시 감시
    engine.resolve(farAt(72));
    engine.update(homeAt(80));
    engine.update(farAt(85));
    assert(engine.level() == AlertLevel::Caution);
}

void testKnownPlacesSurviveSetProfile() {
    AlertEngine engine(PatternProfile::coldStart());
    engine.setKnownPlaces({{"경로당", 1200, 0}});
    engine.update(farAt(0));                          // 경로당 바로 앞 -> 정상
    assert(engine.level() == AlertLevel::Normal);

    engine.setProfile(PatternProfile::coldStart());   // 학습 결과가 바뀌어도 등록 장소 유지
    engine.update(farAt(1));
    assert(engine.level() == AlertLevel::Normal && engine.profile().knownPlaces().size() == 1);
}

}  // namespace

int main() {
    testElderOk();
    testGuardianKnownOuting();
    testResolveQuietThenHome();
    testKnownPlacesSurviveSetProfile();
    std::cout << "경보 끄기·등록 장소 테스트 통과\n";
    return 0;
}
