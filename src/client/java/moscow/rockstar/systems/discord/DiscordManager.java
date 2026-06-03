/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jagrosh.discordipc.entities.RichPresence$Builder
 */
package moscow.rockstar.systems.discord;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.RichPresence;
import com.jagrosh.discordipc.entities.DiscordBuild;
import java.time.OffsetDateTime;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.interfaces.IMinecraft;

public class DiscordManager
implements IMinecraft {
    private final IPCClient client = new IPCClient(1368178867952422993L);

    private RichPresence.Builder getBuilder() {
        Rockstar client = Rockstar.getInstance();
        String title = client.getDisplayTitle();
        String hwid = client.getHwid();
        return new RichPresence.Builder()
            .setDetails(title)
            .setState("HWID: " + hwid)
            .setStartTimestamp(OffsetDateTime.now())
            .setLargeImage("animlogo", title);
    }

    public void connect() {
        try {
            this.client.setListener(new IPCListener(){

                public void onReady(IPCClient client) {
                    client.sendRichPresence(DiscordManager.this.getBuilder().build());
                }
            });
            this.client.connect(new DiscordBuild[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}
