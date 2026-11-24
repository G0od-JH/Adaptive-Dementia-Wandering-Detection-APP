#include "model/Trajectory.h"

#include <stdexcept>

namespace wander {

Trajectory::Trajectory(const std::vector<LocationPoint>& initial) {
    for (const auto& p : initial) {
        add(p);
    }
}

void Trajectory::add(const LocationPoint& p) {
    if (!points_.empty() && p.time() < points_.back().time()) {
        throw std::invalid_argument("시간 역순 입력: " + p.time().toString());
    }
    points_.push_back(p);
}

std::optional<LocationPoint> Trajectory::first() const {
    if (points_.empty()) return std::nullopt;
    return points_.front();
}

std::optional<LocationPoint> Trajectory::latest() const {
    if (points_.empty()) return std::nullopt;
    return points_.back();
}

Trajectory Trajectory::since(SimTime from) const {
    Trajectory t;
    for (const auto& p : points_) {
        if (p.time() >= from) {
            t.add(p);
        }
    }
    return t;
}

Seconds Trajectory::duration() const {
    if (points_.size() < 2) return 0;
    return points_.back().time() - points_.front().time();
}

double Trajectory::totalDistance() const {
    double sum = 0;
    for (std::size_t i = 1; i < points_.size(); ++i) {
        sum += points_[i - 1].distanceTo(points_[i]);
    }
    return sum;
}

}  // namespace wander
