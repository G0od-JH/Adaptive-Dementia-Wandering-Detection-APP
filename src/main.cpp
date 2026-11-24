#include <iostream>

#include "alert/AlertEngine.h"
#include "pattern/PatternProfile.h"

// 진입점. TODO(코드A): 모의데이터 읽기 -> 시뮬레이션 시계로 한 점씩 engine.update() -> 화면 갱신
int main() {
    wander::AlertEngine engine(wander::PatternProfile::coldStart());
    std::cout << "판단 전략: " << engine.strategy().name() << "\n";
    std::cout << "현재 단계: " << wander::label(engine.level()) << "\n";
    return 0;
}
