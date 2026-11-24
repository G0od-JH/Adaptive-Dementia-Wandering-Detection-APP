// 엔진·상태·Observer가 이어지는지 확인하는 기본 테스트 (assert 기반, 라이브러리 불필요)
// 실행: cmake --build build && ctest --test-dir build --output-on-failure
#undef NDEBUG  // Release 빌드에서도 assert 동작
#include <cassert>
#include <iostream>
#include <memory>
#include <vector>

#include "alert/AlertEngine.h"
#include "model/LocationPoint.h"
#include "pattern/PatternProfile.h"
#include "strategy/DetectionStrategy.h"

using namespace wander;

namespace {

// 테스트용 규칙: 집에서 500m 넘으면 이상
class FarRule : public Rule {
public:
    FarRule() : Rule("테스트-멀리") {}
    RuleResult evaluate(const Trajectory& outing, const PatternProfile&) const override {
        auto p = outing.latest();
        if (p && p->distanceFromHome() > 500) return abnormal("집에서 500m 초과");
        return ok();
    }
};

class TestStrategy : public DetectionStrategy {
public:
    TestStrategy() { addRule(std::make_unique<FarRule>()); }
    std::string name() const override { return "테스트"; }
};

// 리스너: 받은 이벤트 기록
class Recorder : public AlertListener {
public:
    std::vector<AlertEvent> events;
    void onAlertChanged(const AlertEvent& e) override { events.push_back(e); }
};

void testCsvRoundTrip() {
    auto p = LocationPoint::fromCsv("2026-11-24T08:30:00,120.5,340.0");
    assert(p.time().toString() == "2026-11-24T08:30:00");
    assert(p.hour() == 8 && p.x() == 120.5 && p.y() == 340.0);
    bool threw = false;
    try { LocationPoint::fromCsv("2026-11-24T08:30:00,abc,1"); } catch (const std::invalid_argument&) { threw = true; }
    assert(threw);
}

void testTrajectoryRejectsReverseTime() {
    Trajectory t;
    t.add(LocationPoint(SimTime::of(2026, 11, 24, 9, 0), 0, 0));
    bool threw = false;
    try { t.add(LocationPoint(SimTime::of(2026, 11, 24, 8, 59), 0, 0)); } catch (const std::invalid_argument&) { threw = true; }
    assert(threw);
}

void testStrategyAutoSwitch() {
    AlertEngine engine(PatternProfile::coldStart());
    assert(engine.strategy().name() == "콜드 스타트(기본 기준)");
    std::array<bool, 24> hours{};
    engine.setProfile(PatternProfile(10, {}, hours, minutes(40)));
    assert(engine.strategy().name() == "학습 패턴");
}

void testEscalationAndResolve() {
    AlertEngine engine(PatternProfile::coldStart());
    engine.setStrategy(std::make_unique<TestStrategy>());
    Recorder rec;
    engine.addListener(&rec);

    const SimTime t0 = SimTime::of(2026, 11, 24, 9, 0);
    for (int m = 0; m < 60; ++m) {                       // 1분마다 20m씩 멀어짐
        engine.update(LocationPoint(t0.plus(minutes(m)), 100 + 20.0 * m, 0));
    }
    // 9:21 Caution -> 9:31 Warning(10분) -> 9:51 Emergency(30분)
    assert(rec.events.size() == 3);
    assert(rec.events[0].to == AlertLevel::Caution && rec.events[0].time.toString() == "2026-11-24T09:21:00");
    assert(rec.events[1].to == AlertLevel::Warning && rec.events[1].time.toString() == "2026-11-24T09:31:00");
    assert(rec.events[2].to == AlertLevel::Emergency && rec.events[2].time.toString() == "2026-11-24T09:51:00");

    engine.update(LocationPoint(t0.plus(minutes(61)), 0, 0));   // 집에 와도 Emergency 유지
    assert(engine.level() == AlertLevel::Emergency);

    engine.resolve(LocationPoint(t0.plus(minutes(62)), 0, 0));  // 보호자 해제
    assert(engine.level() == AlertLevel::Normal && rec.events.size() == 4);
}

}  // namespace

int main() {
    testCsvRoundTrip();
    testTrajectoryRejectsReverseTime();
    testStrategyAutoSwitch();
    testEscalationAndResolve();
    std::cout << "모든 테스트 통과\n";
    return 0;
}
