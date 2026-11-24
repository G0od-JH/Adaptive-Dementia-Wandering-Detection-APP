#pragma once

#include <string>

#include "model/SimTime.h"

namespace wander {

// 한 시점의 위치 (불변 값 객체).
// 좌표: 평면 좌표, 단위 미터, 원점(0,0) = 어르신 집.  <- 좌표계는 코드A와 합의 필요
// CSV 한 줄 형식: "2026-11-24T08:30:00,120.5,340.0"  (시각, x, y)
class LocationPoint {
public:
    LocationPoint(SimTime time, double x, double y);  // x/y가 NaN이면 std::invalid_argument

    SimTime time() const { return time_; }
    double x() const { return x_; }
    double y() const { return y_; }
    int hour() const { return time_.hour(); }

    double distanceTo(const LocationPoint& other) const;  // 직선거리(m)
    double distanceFromHome() const;                      // 집(원점)까지 거리(m)

    // CSV 한 줄 -> LocationPoint. 형식 틀리면 std::invalid_argument
    static LocationPoint fromCsv(const std::string& line);
    std::string toCsv() const;

private:
    SimTime time_;
    double x_;
    double y_;
};

}  // namespace wander
