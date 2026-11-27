# 안드로이드 앱에서 C++ 판단 엔진(wander_core)을 쓰기 위한 빌드 설정.
# 엔진 소스는 레포 루트의 src/ 를 그대로 가져다 쓴다 (복사 X). 지후 담당.
cmake_minimum_required(VERSION 3.22.1)
project(wander_jni LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# app/src/main/cpp -> 레포 루트
get_filename_component(REPO_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../.." ABSOLUTE)

# 루트 CMakeLists.txt 의 wander_core 와 같은 폴더 목록
file(GLOB_RECURSE CORE_SOURCES CONFIGURE_DEPENDS
  ${REPO_ROOT}/src/model/*.cpp ${REPO_ROOT}/src/pattern/*.cpp ${REPO_ROOT}/src/rule/*.cpp
  ${REPO_ROOT}/src/strategy/*.cpp ${REPO_ROOT}/src/alert/*.cpp ${REPO_ROOT}/src/sim/*.cpp)

add_library(wander_jni SHARED wander_jni.cpp ${CORE_SOURCES})
target_include_directories(wander_jni PRIVATE ${REPO_ROOT}/src)
target_compile_options(wander_jni PRIVATE -Wall -Wextra)

find_library(log-lib log)
target_link_libraries(wander_jni ${log-lib})
