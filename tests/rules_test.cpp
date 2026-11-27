// 코드B 규칙 5개 + 패턴 학습기 테스트 (규칙마다 정상 1, 이상 1 이상). 작성: 건영
#undef NDEBUG
#include <array>
#include <cassert>
#include <iostream>
#include <string>
#include <unordered_set>
#include <vector>

#include "pattern/FrequencyPatternLearner.h"
#include "pattern/PatternProfile.h"
#include "rule/HomeRadiusRule.h"
#include "rule/LongOutingRule.h"
#include "rule/RepetitiveLoopRule.h"
#include "rule/RouteDeviationRule.h"
#include "rule/UnusualTimeRule.h"

using namespace wander;

namespace {

const SimTime kDay = SimTime::of(2026, 11, 24, 0, 0);

SimTime at(int h, int m) { return kDay.plus(hours(h) + minutes(m)); }

// 동쪽(x축)으로 0~1000m 산책로가 평소 동선, 낮 9~17시 외출, 평소 외출 40분인 학습 프로필
PatternProfile learnedEastRoute() {
    std::unordered_set<PatternProfile::CellKey> cells;
    for (int x = 0; x <= 1000; x += 50) cells.insert(PatternProfile::cellKey(x, 0));
    std::array<bool, 24> hours{};
    for (int h = 9; h < 17; ++h) hours[static_cast<std::size_t>(h)] = true;
    return PatternProfile(30, cells, hours, minutes(40));
}

// (x, y)로 1분마다 한 점씩, from 지점에서 to 지점까지 직선으로 걷기
void walk(Trajectory& t, SimTime& now, double fx, double fy, double tx, double ty, int steps) {
    for (int i = 1; i <= steps; ++i) {
        now = now.plus(minutes(1));
        t.add(LocationPoint(now, fx + (tx - fx) * i / steps, fy + (ty - fy) * i / steps));
    }
}

void testHomeRadius() {
    HomeRadiusRule rule;
    const auto cold = PatternProfile::coldStart();
    Trajectory near({LocationPoint(at(10, 0), 800, 0)});
    assert(!rule.evaluate(near, cold).abnormal);

    Trajectory far({LocationPoint(at(10, 0), 1200, 0)});
    auto r = rule.evaluate(far, cold);
    assert(r.abnormal && r.reason.find("집에서 약 1200m") != std::string::npos);

    // 경로당(1500, 0) 등록하면 그 근처 1200m 지점은 정상
    auto withPlace = cold.withKnownPlaces({{"경로당", 1500, 0}});
    assert(!rule.evaluate(far, withPlace).abnormal);
    Trajectory veryFar({LocationPoint(at(10, 0), 0, 2000)});
    assert(rule.evaluate(veryFar, withPlace).abnormal);

    assert(!rule.evaluate(Trajectory(), cold).abnormal);  // 기록 없으면 정상
}

void testUnusualTime() {
    UnusualTimeRule rule;
    const auto cold = PatternProfile::coldStart();
    Trajectory day({LocationPoint(at(14, 0), 100, 0)});
    assert(!rule.evaluate(day, cold).abnormal);

    Trajectory night({LocationPoint(at(23, 30), 100, 0)});
    auto r = rule.evaluate(night, cold);
    assert(r.abnormal && r.reason.find("23시") != std::string::npos);

    // 학습 프로필: 9~17시만 평소 -> 아침 7시는 이상
    Trajectory early({LocationPoint(at(7, 0), 100, 0)});
    assert(rule.evaluate(early, learnedEastRoute()).abnormal);
}

void testRouteDeviation() {
    RouteDeviationRule rule;
    const auto profile = learnedEastRoute();

    // 평소 산책로를 따라 1km 걸음 -> 정상 (멀어도 익숙한 길)
    Trajectory usual;
    SimTime now = at(10, 0);
    walk(usual, now, 0, 0, 1000, 0, 20);
    assert(!rule.evaluate(usual, profile).abnormal);

    // 북쪽으로 400m 벗어나 5분만 있음 -> 아직 정상 (10분 미만)
    Trajectory brief;
    now = at(10, 0);
    walk(brief, now, 0, 0, 0, 400, 8);
    for (int i = 0; i < 5; ++i) { now = now.plus(minutes(1)); brief.add(LocationPoint(now, 0, 400)); }
    assert(!rule.evaluate(brief, profile).abnormal);

    // 같은 곳에서 12분 넘게 -> 이상
    Trajectory lost = brief;
    for (int i = 0; i < 8; ++i) { now = now.plus(minutes(1)); lost.add(LocationPoint(now, 0, 400)); }
    auto r = rule.evaluate(lost, profile);
    assert(r.abnormal && r.reason.find("분째 이탈") != std::string::npos);

    // 등록 장소(0, 400)면 정상
    assert(!rule.evaluate(lost, profile.withKnownPlaces({{"병원", 0, 400}})).abnormal);
}

void testRepetitiveLoop() {
    RepetitiveLoopRule rule;
    const auto profile = learnedEastRoute();

    // 갔다가 같은 길로 돌아옴 -> 각 셀 2번씩 = 정상
    Trajectory outAndBack;
    SimTime now = at(10, 0);
    walk(outAndBack, now, 60, 0, 600, 0, 10);
    walk(outAndBack, now, 600, 0, 60, 0, 10);
    assert(!rule.evaluate(outAndBack, profile).abnormal);

    // 한 블록(200m x 200m)을 4바퀴 -> 이상
    Trajectory loops;
    now = at(10, 0);
    loops.add(LocationPoint(now, 225, 225));
    for (int lap = 0; lap < 4; ++lap) {
        walk(loops, now, 225, 225, 425, 225, 4);
        walk(loops, now, 425, 225, 425, 425, 4);
        walk(loops, now, 425, 425, 225, 425, 4);
        walk(loops, now, 225, 425, 225, 225, 4);
    }
    auto r = rule.evaluate(loops, profile);
    assert(r.abnormal && r.reason.find("반복") != std::string::npos);

    // 셀 경계에서 제자리 흔들림(±3m) -> 정상
    Trajectory jitter;
    now = at(10, 0);
    for (int i = 0; i < 30; ++i) {
        now = now.plus(minutes(1));
        jitter.add(LocationPoint(now, (i % 2 == 0) ? 248.0 : 252.0, 120.0));
    }
    assert(!rule.evaluate(jitter, profile).abnormal);
}

void testLongOuting() {
    LongOutingRule rule;
    const auto profile = learnedEastRoute();  // 평소 40분 -> 60분 넘으면 이상

    Trajectory shortOuting({LocationPoint(at(10, 0), 100, 0), LocationPoint(at(10, 50), 100, 0)});
    assert(!rule.evaluate(shortOuting, profile).abnormal);

    Trajectory longOuting({LocationPoint(at(10, 0), 100, 0), LocationPoint(at(11, 5), 100, 0)});
    auto r = rule.evaluate(longOuting, profile);
    assert(r.abnormal && r.reason.find("65분째") != std::string::npos);
}

void testLearner() {
    FrequencyPatternLearner learner;
    assert(!learner.learn({}).isReliable());  // 기록 없으면 콜드 스타트

    // 10일 동안 매일 10시에 동쪽 600m까지 갔다가 20분 뒤 돌아오는 하루
    std::vector<Trajectory> days;
    for (int d = 0; d < 10; ++d) {
        Trajectory day;
        SimTime now = kDay.plus(hours(24) * d + hours(9) + minutes(50));
        for (int i = 0; i < 10; ++i) { now = now.plus(minutes(1)); day.add(LocationPoint(now, 0, 0)); }
        walk(day, now, 0, 0, 600, 0, 10);
        walk(day, now, 600, 0, 0, 0, 10);
        for (int i = 0; i < 10; ++i) { now = now.plus(minutes(1)); day.add(LocationPoint(now, 0, 0)); }
        days.push_back(day);
    }
    // 하루만 밤 23시에 북쪽으로 나감 (드문 일 -> 평소 아님)
    Trajectory odd;
    SimTime now = kDay.plus(hours(24) * 10 + hours(23));
    odd.add(LocationPoint(now, 0, 0));
    walk(odd, now, 0, 0, 0, 500, 10);
    walk(odd, now, 0, 500, 0, 0, 10);
    days.push_back(odd);

    const PatternProfile p = learner.learn(days);
    assert(p.learnedDays() == 11 && p.isReliable());
    assert(p.isUsualOutingHour(10) && !p.isUsualOutingHour(23) && !p.isUsualOutingHour(3));
    assert(p.distanceToUsualRoute(LocationPoint(at(10, 0), 500, 0)) < 50);    // 평소 길
    assert(p.distanceToUsualRoute(LocationPoint(at(10, 0), 0, 450)) > 300);   // 하루만 간 길
    // 집 반경(50m)을 벗어난 순간 ~ 돌아온 순간이라 약 20분
    assert(p.typicalOutingDuration() >= minutes(18) && p.typicalOutingDuration() <= minutes(20));
}

}  // namespace

int main() {
    testHomeRadius();
    testUnusualTime();
    testRouteDeviation();
    testRepetitiveLoop();
    testLongOuting();
    testLearner();
    std::cout << "규칙·학습기 테스트 통과\n";
    return 0;
}
