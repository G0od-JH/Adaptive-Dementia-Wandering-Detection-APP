cmake_minimum_required(VERSION 3.16)
project(WanderingDetection LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

if(MSVC)
  add_compile_options(/W4 /utf-8)
else()
  add_compile_options(-Wall -Wextra -Wpedantic)
endif()

# 판단 로직 (화면과 분리된 핵심 라이브러리)
file(GLOB_RECURSE CORE_SOURCES CONFIGURE_DEPENDS
  src/model/*.cpp src/pattern/*.cpp src/rule/*.cpp
  src/strategy/*.cpp src/alert/*.cpp src/sim/*.cpp)
add_library(wander_core ${CORE_SOURCES})
target_include_directories(wander_core PUBLIC src)

# 실행 파일 (화면 라이브러리는 지후가 정하면 여기 추가)
add_executable(wander src/main.cpp)
target_link_libraries(wander PRIVATE wander_core)

# 테스트
enable_testing()
file(GLOB TEST_SOURCES CONFIGURE_DEPENDS tests/*_test.cpp)
foreach(test_src ${TEST_SOURCES})
  get_filename_component(test_name ${test_src} NAME_WE)
  add_executable(${test_name} ${test_src})
  target_link_libraries(${test_name} PRIVATE wander_core)
  add_test(NAME ${test_name} COMMAND ${test_name})
endforeach()
