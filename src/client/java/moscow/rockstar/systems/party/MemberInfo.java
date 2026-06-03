package moscow.rockstar.systems.party;

public class MemberInfo {
    public double x, y, z;
    public float hp;
    public String server;   // название сервера/мира
    public int anarchy;     // номер анархии, -1 если нет
    public int prevAnarchy; // предыдущая анархия для детекции перехода
    public long lastUpdate;

    public MemberInfo(double x, double y, double z, float hp, String server, int anarchy) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hp = hp;
        this.server = server;
        this.anarchy = anarchy;
        this.prevAnarchy = anarchy;
        this.lastUpdate = System.currentTimeMillis();
    }

    // Обновление с детекцией смены анархии
    public void update(double x, double y, double z, float hp, String server, int anarchy) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hp = hp;
        this.server = server;
        this.prevAnarchy = this.anarchy;
        this.anarchy = anarchy;
        this.lastUpdate = System.currentTimeMillis();
    }

    public boolean isStale() {
        return System.currentTimeMillis() - lastUpdate > 10000;
    }
}
