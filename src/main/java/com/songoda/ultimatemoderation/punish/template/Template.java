package com.songoda.ultimatemoderation.punish.template;

import com.songoda.ultimatemoderation.punish.Punishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Template extends Punishment {
    private final String templateName;
    private final UUID creator;

    public Template(PunishmentType punishmentType, long duration, String reason, Player creator, String name) {
        super(punishmentType, duration, reason);
        this.creator = creator.getUniqueId();
        this.templateName = name;
    }

    public Template(PunishmentType punishmentType, long duration, String reason, UUID creator, String name, int id) {
        super(punishmentType, duration, reason, id);
        this.creator = creator;
        this.templateName = name;
    }

    public Template(PunishmentType punishmentType, long duration, String reason, UUID creator, String name) {
        super(punishmentType, duration, reason);
        this.creator = creator;
        this.templateName = name;
    }

    public String getName() {
        return this.templateName;
    }

    public UUID getCreator() {
        return this.creator;
    }
}
