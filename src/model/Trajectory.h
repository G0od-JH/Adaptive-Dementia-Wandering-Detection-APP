#pragma once

#include <optional>
#include <vector>

#include "model/LocationPoint.h"

namespace wander {

// 시간순 위치 기록 (한 번의 외출 또는 하루치).
// 규칙: 시간이 역행하는 점은 추가 불가.
class Trajectory {
public:
    Trajectory() = default;
    explicit Trajectory(const std::vector<LocationPoint>& initial);

    void add(const LocationPoint& p);  // 직전 점보다 이른 시각이면 std::invalid_argument
    void clear() { points_.clear(); }

    bool empty() const { return points_.empty(); }
    std::size_t size() const { return points_.size(); }

    std::optional<LocationPoint> first() const;
    std::optional<LocationPoint> latest() const;
    const std::vector<LocationPoint>& points() const { return points_; }

    Trajectory since(SimTime from) const;  // from 이후(포함) 점만 담은 새 Trajectory
    Seconds duration() const;              // 첫 점 ~ 마지막 점. 점 2개 미만이면 0
    double totalDistance() const;          // 누적 이동 거리(m)

    // range-for 지원: for (const auto& p : trajectory)
    auto begin() const { return points_.begin(); }
    auto end() const { return points_.end(); }

private:
    std::vector<LocationPoint> points_;
};

}  // namespace wander
