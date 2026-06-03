package moscow.rockstar.utility.sounds;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

public class ClientSoundInstance extends SimpleSoundInstance {
    private final String fileName;

    public ClientSoundInstance(String fileName, float volume) {
        super(Identifier.fromNamespaceAndPath(Rockstar.MOD_ID, fileName), SoundSource.MASTER, volume, 1.0f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
        this.fileName = fileName;
    }

    public ClientSoundInstance(String fileName, float volume, float pitch) {
        super(Identifier.fromNamespaceAndPath(Rockstar.MOD_ID, fileName), SoundSource.MASTER, volume, pitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
        this.fileName = fileName;
    }

    public void play(float volume) {
        Minecraft.getInstance().getSoundManager().play(new ClientSoundInstance(this.fileName, volume));
    }

    public void play(float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new ClientSoundInstance(this.fileName, volume, pitch));
    }

    @Generated
    public String getFileName() {
        return this.fileName;
    }
}
