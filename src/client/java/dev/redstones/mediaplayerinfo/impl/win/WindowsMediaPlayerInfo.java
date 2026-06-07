/*
 * Decompiled with CFR 0.152.
 */
package dev.redstones.mediaplayerinfo.impl.win;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

public class WindowsMediaPlayerInfo
implements MediaPlayerInfo {
    @Override
    public native List<IMediaSession> getMediaSessions();

    static {
        System.out.println("[MPI] static init: attempting to load MediaPlayerInfo.dll");
        boolean loaded = false;
        Throwable lastError = null;
        try {
            System.loadLibrary("MediaPlayerInfo");
            System.out.println("[MPI] loadLibrary(MediaPlayerInfo) OK");
            loaded = true;
        }
        catch (Throwable throwable) {
            System.out.println("[MPI] loadLibrary failed (expected if not on java.library.path): " + throwable);
            lastError = throwable;
        }
        if (!loaded) {
            try {
                java.net.URL res = WindowsMediaPlayerInfo.class.getResource("/mediaplayerinfo/natives/win/MediaPlayerInfo.dll");
                System.out.println("[MPI] resource URL=" + res);
                Path tempDir = Files.createTempDirectory("mediaplayerinfo-", new FileAttribute[0]);
                Path dllFile = tempDir.resolve("MediaPlayerInfo.dll");
                try (InputStream inputStream = WindowsMediaPlayerInfo.class.getResourceAsStream("/mediaplayerinfo/natives/win/MediaPlayerInfo.dll");){
                    if (inputStream == null) {
                        throw new IOException("Resource not found: /mediaplayerinfo/natives/win/MediaPlayerInfo.dll");
                    }
                    byte[] bytes = inputStream.readAllBytes();
                    System.out.println("[MPI] extracted " + bytes.length + " bytes -> " + dllFile);
                    Files.write(dllFile, bytes, new OpenOption[0]);
                }
                System.load(dllFile.toAbsolutePath().toString());
                System.out.println("[MPI] System.load OK from " + dllFile);
                try {
                    Files.deleteIfExists(dllFile);
                    Files.deleteIfExists(tempDir);
                }
                catch (IOException ignored) {
                    dllFile.toFile().deleteOnExit();
                    tempDir.toFile().deleteOnExit();
                }
                loaded = true;
            }
            catch (Throwable throwable) {
                System.err.println("[MPI] FATAL: extraction/load failed:");
                throwable.printStackTrace();
                if (lastError != null) {
                    throwable.addSuppressed(lastError);
                }
                throw new RuntimeException("Failed to load MediaPlayerInfo.dll", throwable);
            }
        }
    }
}
