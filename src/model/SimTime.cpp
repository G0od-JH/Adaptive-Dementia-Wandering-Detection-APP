#include "model/SimTime.h"

#include <cstdio>
#include <stdexcept>

namespace wander {

namespace {

// 그레고리력 날짜 <-> 1970-01-01 기준 일수 (H. Hinnant 알고리즘)
std::int64_t daysFromCivil(std::int64_t y, unsigned m, unsigned d) {
    y -= m <= 2;
    const std::int64_t era = (y >= 0 ? y : y - 399) / 400;
    const unsigned yoe = static_cast<unsigned>(y - era * 400);
    const unsigned doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
    const unsigned doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    return era * 146097 + static_cast<std::int64_t>(doe) - 719468;
}

void civilFromDays(std::int64_t z, int& y, int& m, int& d) {
    z += 719468;
    const std::int64_t era = (z >= 0 ? z : z - 146096) / 146097;
    const unsigned doe = static_cast<unsigned>(z - era * 146097);
    const unsigned yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
    const unsigned doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    const unsigned mp = (5 * doy + 2) / 153;
    d = static_cast<int>(doy - (153 * mp + 2) / 5 + 1);
    m = static_cast<int>(mp < 10 ? mp + 3 : mp - 9);
    y = static_cast<int>(static_cast<std::int64_t>(yoe) + era * 400 + (m <= 2));
}

std::int64_t floorDiv(std::int64_t a, std::int64_t b) {
    std::int64_t q = a / b;
    return (a % b != 0 && (a < 0) != (b < 0)) ? q - 1 : q;
}

std::int64_t floorMod(std::int64_t a, std::int64_t b) { return a - floorDiv(a, b) * b; }

}  // namespace

SimTime SimTime::of(int year, int month, int day, int hour, int minute, int second) {
    if (month < 1 || month > 12 || day < 1 || day > 31 || hour < 0 || hour > 23 ||
        minute < 0 || minute > 59 || second < 0 || second > 59) {
        throw std::invalid_argument("잘못된 날짜/시각 값");
    }
    std::int64_t days = daysFromCivil(year, static_cast<unsigned>(month), static_cast<unsigned>(day));
    return SimTime(days * 86400 + hour * 3600 + minute * 60 + second);
}

SimTime SimTime::parse(const std::string& iso) {
    int y, mo, d, h, mi, s;
    char tail;
    if (std::sscanf(iso.c_str(), "%d-%d-%dT%d:%d:%d%c", &y, &mo, &d, &h, &mi, &s, &tail) != 6) {
        throw std::invalid_argument("시각 형식 오류(YYYY-MM-DDTHH:MM:SS): " + iso);
    }
    return of(y, mo, d, h, mi, s);
}

int SimTime::hour() const { return static_cast<int>(floorMod(secs_, 86400) / 3600); }

int SimTime::minute() const { return static_cast<int>(floorMod(secs_, 3600) / 60); }

std::string SimTime::toString() const {
    int y, m, d;
    civilFromDays(floorDiv(secs_, 86400), y, m, d);
    std::int64_t rest = floorMod(secs_, 86400);
    char buf[32];
    std::snprintf(buf, sizeof buf, "%04d-%02d-%02dT%02d:%02d:%02d", y, m, d,
                  static_cast<int>(rest / 3600), static_cast<int>(rest % 3600 / 60),
                  static_cast<int>(rest % 60));
    return buf;
}

}  // namespace wander
