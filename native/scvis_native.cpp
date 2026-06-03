#include <jni.h>
#include <windows.h>
#include <wincrypt.h>
#include <wbemidl.h>
#include <comdef.h>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <iomanip>

#pragma comment(lib, "wbemuuid.lib")
#pragma comment(lib, "ole32.lib")
#pragma comment(lib, "oleaut32.lib")

namespace {

constexpr wchar_t kSessionPath[] = L"C:\\SCVisuals\\client-session.json";

std::string wideToUtf8(const std::wstring& ws) {
    if (ws.empty()) return {};
    const int size = WideCharToMultiByte(CP_UTF8, 0, ws.c_str(), -1, nullptr, 0, nullptr, nullptr);
    if (size <= 0) return {};
    std::string out(static_cast<size_t>(size - 1), '\0');
    WideCharToMultiByte(CP_UTF8, 0, ws.c_str(), -1, out.data(), size, nullptr, nullptr);
    return out;
}

std::string queryWmi(const std::wstring& wmiClass, const std::wstring& property) {
    std::string result;
    HRESULT hr = CoInitializeEx(nullptr, COINIT_MULTITHREADED);
    const bool coInit = SUCCEEDED(hr) || hr == RPC_E_CHANGED_MODE;

    CoInitializeSecurity(
        nullptr, -1, nullptr, nullptr,
        RPC_C_AUTHN_LEVEL_DEFAULT, RPC_C_IMP_LEVEL_IMPERSONATE,
        nullptr, EOAC_NONE, nullptr);

    IWbemLocator* locator = nullptr;
    hr = CoCreateInstance(CLSID_WbemLocator, nullptr, CLSCTX_INPROC_SERVER, IID_IWbemLocator, (void**)&locator);
    if (FAILED(hr) || !locator) {
        if (coInit) CoUninitialize();
        return result;
    }

    IWbemServices* services = nullptr;
    hr = locator->ConnectServer(_bstr_t(L"ROOT\\CIMV2"), nullptr, nullptr, 0, 0, 0, 0, &services);
    if (FAILED(hr) || !services) {
        locator->Release();
        if (coInit) CoUninitialize();
        return result;
    }

    CoSetProxyBlanket(
        services, RPC_C_AUTHN_WINNT, RPC_C_AUTHZ_NONE, nullptr,
        RPC_C_AUTHN_LEVEL_CALL, RPC_C_IMP_LEVEL_IMPERSONATE, nullptr, EOAC_NONE);

    std::wstring query = L"SELECT " + property + L" FROM " + wmiClass;
    IEnumWbemClassObject* enumerator = nullptr;
    hr = services->ExecQuery(
        bstr_t(L"WQL"), bstr_t(query.c_str()),
        WBEM_FLAG_FORWARD_ONLY | WBEM_FLAG_RETURN_IMMEDIATELY,
        nullptr, &enumerator);

    if (SUCCEEDED(hr) && enumerator) {
        IWbemClassObject* obj = nullptr;
        ULONG returned = 0;
        while (enumerator->Next(WBEM_INFINITE, 1, &obj, &returned) == S_OK && returned) {
            VARIANT value;
            VariantInit(&value);
            if (SUCCEEDED(obj->Get(property.c_str(), 0, &value, nullptr, nullptr)) &&
                value.vt == VT_BSTR && value.bstrVal) {
                result = wideToUtf8(value.bstrVal);
            }
            VariantClear(&value);
            obj->Release();
            if (!result.empty()) break;
        }
        enumerator->Release();
    }

    services->Release();
    locator->Release();
    if (coInit) CoUninitialize();

    while (!result.empty() && (result.back() == ' ' || result.back() == '\t' || result.back() == '\r' || result.back() == '\n')) {
        result.pop_back();
    }
    return result;
}

std::string sha256First16Hex(const std::string& input) {
    HCRYPTPROV prov = 0;
    HCRYPTHASH hash = 0;
    std::string out = "0000000000000000";

    if (!CryptAcquireContext(&prov, nullptr, nullptr, PROV_RSA_AES, CRYPT_VERIFYCONTEXT)) {
        return out;
    }
    if (!CryptCreateHash(prov, CALG_SHA_256, 0, 0, &hash)) {
        CryptReleaseContext(prov, 0);
        return out;
    }
    if (!CryptHashData(hash, reinterpret_cast<const BYTE*>(input.data()), static_cast<DWORD>(input.size()), 0)) {
        CryptDestroyHash(hash);
        CryptReleaseContext(prov, 0);
        return out;
    }

    BYTE rgb[32];
    DWORD cb = 32;
    if (CryptGetHashParam(hash, HP_HASHVAL, rgb, &cb, 0)) {
        std::ostringstream ss;
        for (DWORD i = 0; i < cb; ++i) {
            ss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(rgb[i]);
        }
        out = ss.str().substr(0, 16);
    }

    CryptDestroyHash(hash);
    CryptReleaseContext(prov, 0);
    return out;
}

std::string computeHwidHash() {
    std::string board = queryWmi(L"Win32_BaseBoard", L"SerialNumber");
    if (board.empty()) {
        board = queryWmi(L"Win32_ComputerSystemProduct", L"UUID");
    }
    const std::string cpu = queryWmi(L"Win32_Processor", L"ProcessorId");
    return sha256First16Hex(board + "_" + cpu);
}

std::string readFileUtf8(const wchar_t* path) {
    std::ifstream file(path, std::ios::binary);
    if (!file) return {};
    return std::string((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
}

std::string extractJsonString(const std::string& json, const char* key) {
    const std::string needle = std::string("\"") + key + "\"";
    size_t pos = json.find(needle);
    if (pos == std::string::npos) return {};
    pos = json.find(':', pos);
    if (pos == std::string::npos) return {};
    pos = json.find('"', pos);
    if (pos == std::string::npos) return {};
    size_t start = pos + 1;
    size_t end = json.find('"', start);
    if (end == std::string::npos) return {};
    return json.substr(start, end - start);
}

std::string readSessionLogin() {
    const std::string json = readFileUtf8(kSessionPath);
    if (json.empty()) return {};
    return extractJsonString(json, "login");
}

std::string buildDisplayTitle(const std::string& login) {
    if (login.empty()) {
        return "Sound Cloud Visuals (Beta)";
    }
    return "Sound Cloud Visuals (Beta) For " + login;
}

jstring toJString(JNIEnv* env, const std::string& value) {
    return env->NewStringUTF(value.c_str());
}

} // namespace

extern "C" {

JNIEXPORT jstring JNICALL
Java_moscow_rockstar_protection_HwidNative_nativeGetHwidHash(JNIEnv* env, jclass) {
    return toJString(env, computeHwidHash());
}

JNIEXPORT jstring JNICALL
Java_moscow_rockstar_protection_HwidNative_nativeGetSessionLogin(JNIEnv* env, jclass) {
    return toJString(env, readSessionLogin());
}

JNIEXPORT jstring JNICALL
Java_moscow_rockstar_protection_HwidNative_nativeGetDisplayTitle(JNIEnv* env, jclass) {
    return toJString(env, buildDisplayTitle(readSessionLogin()));
}

} // extern "C"
