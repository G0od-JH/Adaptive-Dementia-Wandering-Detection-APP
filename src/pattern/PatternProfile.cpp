#include "pattern/PatternProfile.h"

#include <algorithm>
#include <cmath>
#include <limits>
#include <stdexcept>
#include <utility>

#include "config/Thresholds.h"

namespace wander {

namespace {
int cellX(PatternProfile::CellKey key) {
    return static_cast<std::int32_t>(static_cast<std::uint32_t>(static_cast<std::uint64_t>(key) >> 32));
}
int cellY(PatternProfile::CellKey key) {
    return static_cast<std::int32_t>(static_cast<std::uint32_t>(static_cast<std::uint64_t>(key)));
}
}  // namespace

PatternProfile::PatternProfile(int learnedDays, std::unordered_set<CellKey> usualCells,
                               std::array<bool, 24> usualOutingHours, Seconds typicalOutingDuration)
    : learnedDays_(learnedDays),
      usualCells_(std::move(usualCells)),
      usualOutingHours_(usualOutingHours),
      typicalOutingDuration_(typicalOutingDuration) {
    if (learnedDays < 0 || typicalOutingDuration < 0) {
        throw std::invalid_argument("learnedDays / typicalOutingDuration 은 음수 불가");
    }
}

PatternProfile PatternProfile::coldStart() {
    std::array<bool, 24> outingHours{};
    for (int h = thresholds::kNightEndHour; h < thresholds::kNightStartHour; ++h) {
        outingHours[h] = true;
    }
    return PatternProfile(0, {}, outingHours, hours(1));
}

bool PatternProfile::isReliable() const { return learnedDays_ >= thresholds::kMinLearningDays; }

bool PatternProfile::isUsualOutingHour(int hour) const {
    if (hour < 0 || hour > 23) throw std::out_of_range("hour 0~23");
    return usualOutingHours_[hour];
}

double PatternProfile::distanceToUsualRoute(const LocationPoint& p) const {
    double best = std::numeric_limits<double>::infinity();
    for (CellKey key : usualCells_) {
        double cx = (cellX(key) + 0.5) * thresholds::kGridCellM;
        double cy = (cellY(key) + 0.5) * thresholds::kGridCellM;
        best = std::min(best, std::hypot(p.x() - cx, p.y() - cy));
    }
    return best;
}

PatternProfile::CellKey PatternProfile::cellKey(double x, double y) {
    auto cx = static_cast<std::int32_t>(std::floor(x / thresholds::kGridCellM));
    auto cy = static_cast<std::int32_t>(std::floor(y / thresholds::kGridCellM));
    auto hi = static_cast<std::uint64_t>(static_cast<std::uint32_t>(cx)) << 32;
    auto lo = static_cast<std::uint64_t>(static_cast<std::uint32_t>(cy));
    return static_cast<CellKey>(hi | lo);
}

}  // namespace wander
