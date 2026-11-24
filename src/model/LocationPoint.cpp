#include "model/LocationPoint.h"

#include <cmath>
#include <sstream>
#include <stdexcept>
#include <vector>

namespace wander {

LocationPoint::LocationPoint(SimTime time, double x, double y) : time_(time), x_(x), y_(y) {
    if (std::isnan(x) || std::isnan(y)) {
        throw std::invalid_argument("좌표가 NaN");
    }
}

double LocationPoint::distanceTo(const LocationPoint& other) const {
    return std::hypot(x_ - other.x_, y_ - other.y_);
}

double LocationPoint::distanceFromHome() const { return std::hypot(x_, y_); }

LocationPoint LocationPoint::fromCsv(const std::string& line) {
    std::vector<std::string> fields;
    std::stringstream ss(line);
    std::string f;
    while (std::getline(ss, f, ',')) {
        fields.push_back(f);
    }
    if (fields.size() != 3) {
        throw std::invalid_argument("CSV 필드 수 오류(3개 필요): " + line);
    }
    auto toDouble = [](const std::string& s) {
        std::size_t used = 0;
        double v = std::stod(s, &used);
        if (s.find_first_not_of(" \t\r\n", used) != std::string::npos) {
            throw std::invalid_argument("숫자 뒤에 이상한 문자");
        }
        return v;
    };
    try {
        auto t = fields[0];
        t.erase(0, t.find_first_not_of(" \t"));
        return LocationPoint(SimTime::parse(t), toDouble(fields[1]), toDouble(fields[2]));
    } catch (const std::exception&) {
        throw std::invalid_argument("CSV 파싱 실패: " + line);
    }
}

std::string LocationPoint::toCsv() const {
    std::ostringstream os;
    os << time_.toString() << ',' << x_ << ',' << y_;
    return os.str();
}

}  // namespace wander
