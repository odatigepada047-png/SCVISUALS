/*
 * Decompiled with CFR 0.152.
 */
package dev.redstones.mediaplayerinfo;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.impl.DummyMediaPlayerInfo;
import dev.redstones.mediaplayerinfo.impl.win.WindowsMediaPlayerInfo;
import java.util.List;

public interface MediaPlayerInfo {
    public static final MediaPlayerInfo INSTANCE = SystemMediaPlayerInfo.getInstance();

    public List<IMediaSession> getMediaSessions();

    public static class SystemMediaPlayerInfo {
        private static final MediaPlayerInfo instance = SystemMediaPlayerInfo.create();

        public static MediaPlayerInfo getInstance() {
            return instance;
        }

        private static MediaPlayerInfo create() {
            String osName = System.getProperty("os.name", "").toLowerCase();
            System.out.println("[MPI] os.name=" + osName + " arch=" + System.getProperty("os.arch"));
            try {
                java.net.URL src = WindowsMediaPlayerInfo.class.getProtectionDomain().getCodeSource().getLocation();
                System.out.println("[MPI] WindowsMediaPlayerInfo loaded from: " + src);
            } catch (Throwable t) {
                System.out.println("[MPI] could not resolve source location: " + t);
            }
            if (osName.contains("win")) {
                try {
                    WindowsMediaPlayerInfo impl = new WindowsMediaPlayerInfo();
                    System.out.println("[MPI] WindowsMediaPlayerInfo instantiated OK");
                    return impl;
                }
                catch (Throwable throwable) {
                    System.err.println("[MPI] FAILED to instantiate WindowsMediaPlayerInfo, falling back to Dummy:");
                    throwable.printStackTrace();
                    return new DummyMediaPlayerInfo();
                }
            }
            System.out.println("[MPI] non-Windows OS, using Dummy");
            return new DummyMediaPlayerInfo();
        }
    }
}
