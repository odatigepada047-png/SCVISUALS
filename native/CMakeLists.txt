cmake_minimum_required(VERSION 3.20)
project(scvis_native LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(JNI REQUIRED)

add_library(scvis_native SHARED scvis_native.cpp)
target_include_directories(scvis_native PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(scvis_native PRIVATE ${JNI_LIBRARIES} wbemuuid ole32 oleaut32 ws2_32)

if(WIN32)
    target_compile_definitions(scvis_native PRIVATE UNICODE _UNICODE)
endif()
