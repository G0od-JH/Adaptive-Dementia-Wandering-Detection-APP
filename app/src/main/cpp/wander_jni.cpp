// C++ 판단 엔진(AlertEngine) <-> Kotlin(WanderEngine.kt) 다리. 담당: 지후.
//
// 엔진 코드는 한 줄도 안 바꾸고, 여기서 AlertListener 를 상속한 JniAlertListener 를
// 엔진에 등록한다 (Observer). 엔진이 단계를 바꾸면 Kotlin 의 AlertCallback 이 호출된다.
//
//   Kotlin WanderEngine.update(point)
//     -> nativeUpdate -> AlertEngine::update(LocationPoint)
//          -> (단계가 바뀌면) JniAlertListener::onAlertChanged -> Kotlin AlertCallback.onAlertChanged

#include <jni.h>

#include <memory>
#include <optional>
#include <stdexcept>
#include <string>
#include <vector>

#include "alert/AlertEngine.h"
#include "alert/AlertListener.h"
#include "model/LocationPoint.h"
#include "pattern/PatternProfile.h"

namespace {

jobjectArray toJavaStrings(JNIEnv* env, const std::vector<std::string>& items) {
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray arr = env->NewObjectArray(static_cast<jsize>(items.size()), stringClass, nullptr);
    for (jsize i = 0; i < static_cast<jsize>(items.size()); ++i) {
        jstring s = env->NewStringUTF(items[static_cast<size_t>(i)].c_str());
        env->SetObjectArrayElement(arr, i, s);
        env->DeleteLocalRef(s);
    }
    env->DeleteLocalRef(stringClass);
    return arr;
}

// 엔진의 Observer 구독자. 받은 AlertEvent 를 Kotlin 콜백으로 넘긴다.
class JniAlertListener : public wander::AlertListener {
public:
    JniAlertListener(JNIEnv* env, jobject callback) : callback_(env->NewGlobalRef(callback)) {
        jclass cls = env->GetObjectClass(callback);
        method_ = env->GetMethodID(cls, "onAlertChanged", "(II[Ljava/lang/String;DDLjava/lang/String;)V");
        env->DeleteLocalRef(cls);
    }

    void release(JNIEnv* env) { env->DeleteGlobalRef(callback_); }

    // 엔진을 부르는 JNI 함수가 매번 자기 env 를 넣어준다 (콜백은 같은 스레드에서 바로 실행됨)
    void setEnv(JNIEnv* env) { env_ = env; }

    void onAlertChanged(const wander::AlertEvent& e) override {
        if (!env_ || !method_) return;
        jobjectArray reasons = toJavaStrings(env_, e.reasons);
        jstring time = env_->NewStringUTF(e.time.toString().c_str());
        env_->CallVoidMethod(callback_, method_, static_cast<jint>(e.from), static_cast<jint>(e.to), reasons,
                             e.location.x(), e.location.y(), time);
        env_->DeleteLocalRef(reasons);
        env_->DeleteLocalRef(time);
    }

private:
    jobject callback_;
    jmethodID method_ = nullptr;
    JNIEnv* env_ = nullptr;
};

struct Session {
    Session(JNIEnv* env, jobject callback)
        : engine(wander::PatternProfile::coldStart()), listener(env, callback) {
        engine.addListener(&listener);
    }
    wander::AlertEngine engine;
    JniAlertListener listener;
    std::optional<wander::LocationPoint> last;
    std::vector<std::string> lastReasons;
};

Session* session(jlong handle) { return reinterpret_cast<Session*>(handle); }

void throwJava(JNIEnv* env, const char* cls, const char* msg) {
    jclass c = env->FindClass(cls);
    if (c) env->ThrowNew(c, msg);
}

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_adwd_app_engine_WanderEngine_nativeCreate(JNIEnv* env, jobject /*thiz*/, jobject callback) {
    return reinterpret_cast<jlong>(new Session(env, callback));
}

JNIEXPORT void JNICALL
Java_com_adwd_app_engine_WanderEngine_nativeDestroy(JNIEnv* env, jobject /*thiz*/, jlong handle) {
    Session* s = session(handle);
    if (!s) return;
    s->engine.removeListener(&s->listener);  // 규칙: 리스너가 먼저 없어지면 removeListener 먼저
    s->listener.release(env);
    delete s;
}

// CSV 한 줄 ("2026-11-24T08:30:00,120.5,340.0") 을 넣고 현재 경보 단계(0~3)를 돌려준다.
JNIEXPORT jint JNICALL
Java_com_adwd_app_engine_WanderEngine_nativeUpdate(JNIEnv* env, jobject /*thiz*/, jlong handle, jstring csvLine) {
    Session* s = session(handle);
    const char* chars = env->GetStringUTFChars(csvLine, nullptr);
    std::string line(chars);
    env->ReleaseStringUTFChars(csvLine, chars);
    try {
        wander::LocationPoint p = wander::LocationPoint::fromCsv(line);
        s->listener.setEnv(env);
        wander::Judgement j = s->engine.update(p);
        s->listener.setEnv(nullptr);
        s->last = p;
        s->lastReasons = j.reasons();
        return static_cast<jint>(j.level);
    } catch (const std::invalid_argument& e) {
        s->listener.setEnv(nullptr);
        throwJava(env, "java/lang/IllegalArgumentException", e.what());
        return 0;
    }
}

// 마지막 update() 에서 이상으로 판단된 규칙들의 이유 문장
JNIEXPORT jobjectArray JNICALL
Java_com_adwd_app_engine_WanderEngine_nativeLastReasons(JNIEnv* env, jobject /*thiz*/, jlong handle) {
    return toJavaStrings(env, session(handle)->lastReasons);
}

// 보호자 "괜찮아요" -> AlertEngine::resolve (3단계는 보호자 수동 해제만)
JNIEXPORT void JNICALL
Java_com_adwd_app_engine_WanderEngine_nativeResolve(JNIEnv* env, jobject /*thiz*/, jlong handle) {
    Session* s = session(handle);
    if (!s->last) return;
    s->listener.setEnv(env);
    s->engine.resolve(*s->last);
    s->listener.setEnv(nullptr);
}

JNIEXPORT jstring JNICALL
Java_com_adwd_app_engine_WanderEngine_nativeStrategyName(JNIEnv* env, jobject /*thiz*/, jlong handle) {
    return env->NewStringUTF(session(handle)->engine.strategy().name().c_str());
}

JNIEXPORT jint JNICALL
Java_com_adwd_app_engine_WanderEngine_nativeLevel(JNIEnv* /*env*/, jobject /*thiz*/, jlong handle) {
    return static_cast<jint>(session(handle)->engine.level());
}

}  // extern "C"
