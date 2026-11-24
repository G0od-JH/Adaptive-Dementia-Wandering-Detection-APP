#pragma once

#include <cstdint>
#include <string>

namespace wander {

// 기간 단위: 초. (예: 10분 = 10 * 60)
using Seconds = std::int64_t;

constexpr Seconds minutes(std::int64_t m) { return m * 60; }
constexpr Seconds hours(std::int64_t h) { return h * 3600; }

// 시뮬레이션 시각 (시간대 없음, 초 단위). 내부값 = 1970-01-01 00:00:00 기준 경과 초.
// CSV/화면 문자열 형식: "2026-11-24T08:30:00"
class SimTime {
public:
    SimTime() = default;
    explicit SimTime(Seconds secondsSinceEpoch) : secs_(secondsSinceEpoch) {}

    static SimTime of(int year, int month, int day, int hour, int minute, int second = 0);

    // "YYYY-MM-DDTHH:MM:SS" 파싱. 형식 틀리면 std::invalid_argument
    static SimTime parse(const std::string& iso);

    Seconds seconds() const { return secs_; }
    int hour() const;    // 0~23
    int minute() const;  // 0~59
    std::string toString() const;

    SimTime plus(Seconds s) const { return SimTime(secs_ + s); }

    friend Seconds operator-(SimTime a, SimTime b) { return a.secs_ - b.secs_; }
    friend bool operator<(SimTime a, SimTime b) { return a.secs_ < b.secs_; }
    friend bool operator>(SimTime a, SimTime b) { return a.secs_ > b.secs_; }
    friend bool operator<=(SimTime a, SimTime b) { return a.secs_ <= b.secs_; }
    friend bool operator>=(SimTime a, SimTime b) { return a.secs_ >= b.secs_; }
    friend bool operator==(SimTime a, SimTime b) { return a.secs_ == b.secs_; }
    friend bool operator!=(SimTime a, SimTime b) { return a.secs_ != b.secs_; }

private:
    Seconds secs_ = 0;
};

}  // namespace wander
