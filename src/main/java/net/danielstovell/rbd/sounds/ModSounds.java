package net.danielstovell.rbd.sounds;

import net.danielstovell.rbd.ReturnByDeath;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent RBD_OH = registerSoundEvent("rbd_oh");
    public static final SoundEvent RBD_UEH = registerSoundEvent("rbd_ueh");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(ReturnByDeath.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        ReturnByDeath.LOGGER.info("Registering sounds for: " + ReturnByDeath.MOD_ID);
    }
}
