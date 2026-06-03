/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package moscow.rockstar.systems.friends;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.network.chat.Component;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class FriendManager
implements IMinecraft {
    private final List<String> friends = new ArrayList<String>();

    public void add(String name) {
        if (Rockstar.getInstance().getTargetManager().getTarget().contains(name)) {
            MessageUtility.error(Component.literal((String)Localizator.translate("commands.friends.target")));
            return;
        }
        if (this.friends.contains(name)) {
            MessageUtility.info(Component.literal((String)Localizator.translate("commands.friends.exists", name)));
            return;
        }
        if (name.equalsIgnoreCase(mc.getUser().getName())) {
            MessageUtility.error(Component.literal((String)Localizator.translate("commands.friends.self")));
            return;
        }
        this.friends.add(name);
        MessageUtility.info(Component.literal((String)Localizator.translate("commands.friends.added", name)));
        if (EntityUtility.isInGame()) {
            Rockstar.getInstance().getFileManager().writeFile("client");
        }
    }

    public void remove(String name) {
        if (this.friends.contains(name)) {
            this.friends.remove(name);
            MessageUtility.info(Component.literal((String)Localizator.translate("commands.friends.removed", name)));
        } else {
            MessageUtility.info(Component.literal((String)Localizator.translate("commands.friends.not_exists", name)));
        }
        Rockstar.getInstance().getFileManager().writeFile("client");
    }

    @Compile
    public void clear() {
        if (this.friends.isEmpty()) {
            MessageUtility.error(Component.literal((String)Localizator.translate("commands.friends.empty")));
        } else {
            this.friends.clear();
            MessageUtility.info(Component.literal((String)"\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043e\u0447\u0438\u0449\u0435\u043d!"));
            Rockstar.getInstance().getFileManager().writeFile("client");
        }
    }

    public List<String> listFriends() {
        return Collections.unmodifiableList(this.friends);
    }

    public boolean isFriend(String name) {
        return this.friends.contains(name);
    }
}

