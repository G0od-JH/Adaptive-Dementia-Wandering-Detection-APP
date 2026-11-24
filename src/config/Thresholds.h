#pragma once

#include "model/SimTime.h"

// 판단 기준 수치 모음. 코드 안에 숫자 직접 쓰지 말고 여기 상수 사용.
// 값은 초안 -> 코드B가 확정, 오경보 튜닝(11/30~) 때 여기만 고치면 됨.
namespace wander::thresholds {

// 경로 이탈: 주요 동선에서 300m 이상, 10분 이상 지속
constexpr double kRouteDeviationM = 300.0;
constexpr Seconds kRouteDeviationMin = minutes(10);

// 평소 외출 안 하는 시간대 (콜드 스타트 기본값: 22시~05시)
constexpr int kNightStartHour = 22;
constexpr int kNightEndHour = 5;

// 반복 배회: 같은 격자 셀을 이 횟수 이상 재방문
constexpr int kLoopRevisitCount = 3;

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

}  // namespace wander::thresholds
