#pragma once

namespace wander {

// 경보 단계 4개. 단계별 대응: 본인 귀가 안내 -> 보호자 알림 -> 실종 신고 정보 생성
enum class AlertLevel { Normal = 0, Caution = 1, Warning = 2, Emergency = 3 };

inline const char* label(AlertLevel level) {
    switch (level) {
        case AlertLevel::Normal: return "정상";
        case AlertLevel::Caution: return "주의 - 본인 귀가 안내";
        case AlertLevel::Warning: return "경고 - 보호자 알림";
        case AlertLevel::Emergency: return "긴급 - 실종 신고 정보 생성";
    }
    return "?";
}

inline bool isHigher(AlertLevel a, AlertLevel b) { return static_cast<int>(a) > static_cast<int>(b); }

}  // namespace wander
