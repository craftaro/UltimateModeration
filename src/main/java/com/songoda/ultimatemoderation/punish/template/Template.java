package com.songoda.ultimatemoderation.punish.template;

import com.songoda.ultimatemoderation.punish.Punishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Template extends Punishment {

    private final String templateName;
    private final UUID creator;

    public Template(PunishmentType punishmentType, long duration, String reason, Player creator, String templateName) {
        super(punishmentType, duration, reason);
        this.creator = creator.getUniqueId();
        this.templateName = templateName;
    }

    public Template(PunishmentType punishmentType, long duration, String reason, UUID creator, String templateName) {
        super(punishmentType, duration, reason);
        this.creator = creator;
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public UUID getCreator() {
        return creator;
    }
}
