package moscow.rockstar.utility.sounds;

public final class TimedLyric {
    private final long timeMs;
    private final String text;

    public TimedLyric(long timeMs, String text) {
        this.timeMs = timeMs;
        this.text = text;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public String getText() {
        return text;
    }
}
