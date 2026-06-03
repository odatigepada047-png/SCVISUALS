# scvis_native.dll

JNI library for Sound Cloud Visuals client.

## Build (Windows x64)

```powershell
.\build-native.ps1
```

Output:
- `native/build/Release/scvis_native.dll`
- `src/client/resources/native/win_x64/scvis_native.dll` (packed into mod jar)

## Runtime

Loader writes `C:\SCVisuals\client-session.json` and copies DLL to `C:\SCVisuals\native\scvis_native.dll` before Minecraft starts.

`Rockstar.java` reads HWID + login via `HwidNative` and sets display title:
`Sound Cloud Visuals (Beta) For <login>`
