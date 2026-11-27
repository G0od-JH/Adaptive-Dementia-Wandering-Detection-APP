#pragma once

#include "model/SimTime.h"

// 판단 기준 수치 모음. 코드 안에 숫자 직접 쓰지 말고 여기 상수 사용.
// 11/28 코드B(건영) 확정. 오경보 튜닝(11/30~) 때 여기만 고치면 됨.
namespace wander::thresholds {

// 경로 이탈: 주요 동선에서 300m 이상, 10분 이상 지속
constexpr double kRouteDeviationM = 300.0;
constexpr Seconds kRouteDeviationMin = minutes(10);

// 평소 외출 안 하는 시간대 (콜드 스타트 기본값: 22시~05시)
constexpr int kNightStartHour = 22;
constexpr int kNightEndHour = 5;

// 반복 배회: 같은 격자 셀을 이 횟수 이상 재방문
constexpr int kLoopRevisitCount = 3;
// 반복 배회: 셀을 떠났다가 최소 이만큼 걸은 뒤 돌아와야 재방문으로 셈 (경계 근처 GPS 흔들림 무시)
constexpr double kLoopMinDetourM = 100.0;

// 긴 외출: 평소 외출 시간의 이 배수 초과
constexpr double kLongOutingFactor = 1.5;

// 콜드 스타트 (학습 데이터 부족할 때)
constexpr int kMinLearningDays = 7;
constexpr double kColdStartRadiusM = 1000.0;

// 집 판정 반경 (이 안이면 외출 종료로 보고 기록 초기화)
constexpr double kHomeRadiusM = 50.0;

// 패턴 학습 격자 크기
constexpr double kGridCellM = 50.0;

// 경보 단계 상승 시간 (최초 이상 감지 시점부터 연속 누적)
constexpr Seconds kCautionToWarning = minutes(10);
constexpr Seconds kWarningToEmergency = minutes(30);

// 경보 끄기 (집에 도착하면 셋 다 바로 풀리고 다시 감시)
constexpr Seconds kElderOkSnooze = minutes(15);          // 어르신 "괜찮아요" (주의 단계에서만)
constexpr Seconds kGuardianResolveQuiet = minutes(30);   // 보호자 "찾았어요" 해제 후
// 보호자 "알고 있는 외출"은 시간 제한 없이 귀가할 때까지

// 패턴 학습 (FrequencyPatternLearner)
constexpr double kUsualCellMinDayRatio = 0.2;   // 학습 기간 중 이 비율 이상의 날에 지나간 셀 = 평소 동선
constexpr double kUsualHourMinDayRatio = 0.2;   // 이 비율 이상의 날에 외출해 있던 시각 = 평소 외출 시간대
constexpr Seconds kMinOutingDuration = minutes(5);     // 이보다 짧은 외출은 외출 길이 학습에서 제외
// 평소 외출 길이 = 과거 외출 길이 중 이 백분위 (중앙값을 쓰면 짧은 아침 산책 때문에 긴 낮 외출이 오경보)
constexpr double kTypicalOutingPercentile = 0.9;
constexpr Seconds kDefaultOutingDuration = hours(1);   // 외출 기록이 없을 때 평소 외출 길이

}  // namespace wander::thresholds
