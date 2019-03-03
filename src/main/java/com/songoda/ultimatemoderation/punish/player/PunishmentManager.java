package com.songoda.ultimatemoderation.punish.player;

import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishmentManager {

    private Map<UUID, PlayerPunishData> punishments = new HashMap<>();

    public Map<UUID, PlayerPunishData> getPunishments() {
        return Collections.unmodifiableMap(punishments);
    }

    public PlayerPunishData getPlayer(OfflinePlayer player) {
        return getPlayer(player.getUniqueId());
    }

    public PlayerPunishData getPlayer(UUID player) {
        return punishments.computeIfAbsent(player, PlayerPunishData::new);
    }
}
