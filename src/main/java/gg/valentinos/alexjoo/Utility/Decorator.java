package gg.valentinos.alexjoo.Utility;

import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.time.Duration;

public class Decorator {

    public static final String HARMLESS_METADATA_KEY = "vclans_harmless";

    public static void SummonLightning(Player player) {
        World world = player.getWorld();
        world.strikeLightningEffect(player.getLocation());
    }

    public static void SummonFirework(Player player, Color color, int power, boolean flicker, boolean trail, FireworkEffect.Type type) {
        Location location = player.getLocation().add(0, 1, 0);
        SummonFirework(location, color, power, flicker, trail, type);
    }

    public static void SummonFirework(Location location, Color color, int power, boolean flicker, boolean trail, FireworkEffect.Type type) {
        World world = location.getWorld();
        if (world == null) return;

        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        FireworkEffect.Builder builder = FireworkEffect.builder();
        builder.with(type)
                .flicker(flicker)
                .trail(trail)
                .withColor(color);

        fireworkMeta.clearEffects();
        fireworkMeta.addEffect(builder.build());
        fireworkMeta.setPower(power);

        firework.setFireworkMeta(fireworkMeta);

        firework.setMetadata(HARMLESS_METADATA_KEY, new FixedMetadataValue(VClans.getInstance(), true));
    }

    public static void Broadcast(Player player, Component title, Component subtitle, int duration) {
        player.showTitle(Title.title(
                title,
                subtitle,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(duration), Duration.ofMillis(500))
        ));
    }

    public static void PlaySound(Player player, Key soundKey, float volume) {
        Sound sound = Sound.sound()
                .type(soundKey)
                .source(Sound.Source.MASTER)
                .volume(volume)
                .pitch(1f)
                .build();
        player.playSound(sound, Sound.Emitter.self());

    }
}
