package com.adwd.app.engine

/**
 * C++ 판단 엔진(wander::AlertEngine)의 Kotlin 창구. 실제 판단은 전부 C++ 쪽에서 한다.
 * 연결 코드: app/src/main/cpp/wander_jni.cpp
 *
 * 엔진이 경보 단계를 바꾸면 C++의 AlertListener(Observer)가 [AlertCallback]을 부른다.
 * 단계 숫자는 C++ AlertLevel과 같다: 0 정상, 1 주의, 2 경고, 3 긴급.
 */
fun interface AlertCallback {
    fun onAlertChanged(from: Int, to: Int, reasons: Array<String>, x: Double, y: Double, time: String)
}

class WanderEngine(callback: AlertCallback) : AutoCloseable {
    private var handle: Long = nativeCreate(callback)

    /** 위치 1개 넣고 현재 단계(0~3)를 받는다. 단계가 바뀌면 콜백이 먼저 불린다. */
    fun update(point: TrackPoint): Int {
        check(handle != 0L) { "이미 닫힌 엔진" }
        return nativeUpdate(handle, point.raw)
    }

    /** 마지막 update()에서 이상으로 판단된 규칙들의 이유 문장 */
    fun lastReasons(): List<String> = nativeLastReasons(handle).toList()

    /** 3단계 긴급에서 보호자가 "괜찮아요"를 누름 → 정상으로 해제 */
    fun resolve() = nativeResolve(handle)

    /** 지금 쓰는 판단 방식 이름 (콜드 스타트 / 학습 패턴) */
    val strategyName: String get() = nativeStrategyName(handle)

    val level: Int get() = nativeLevel(handle)

    override fun close() {
        if (handle != 0L) {
            nativeDestroy(handle)
            handle = 0L
        }
    }

    private external fun nativeCreate(callback: AlertCallback): Long
    private external fun nativeDestroy(handle: Long)
    private external fun nativeUpdate(handle: Long, csvLine: String): Int
    private external fun nativeLastReasons(handle: Long): Array<String>
    private external fun nativeResolve(handle: Long)
    private external fun nativeStrategyName(handle: Long): String
    private external fun nativeLevel(handle: Long): Int

    companion object {
        /** NDK 빌드가 안 돼서 라이브러리가 없으면 false. 그래도 지도·재생은 돌아간다. */
        val isAvailable: Boolean by lazy {
            try {
                System.loadLibrary("wander_jni")
                true
            } catch (e: UnsatisfiedLinkError) {
                false
            }
        }
    }
}
