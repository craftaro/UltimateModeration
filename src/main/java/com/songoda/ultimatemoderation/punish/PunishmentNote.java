package com.songoda.ultimatemoderation.punish;

import java.util.UUID;

public class PunishmentNote {

    private final UUID uuid;

    private final String note;
    private final UUID author;
    private final UUID subject;
    private final long creationDate;

    public PunishmentNote(UUID uuid, String note, UUID author, UUID subject, long creationDate) {
        this.uuid = uuid;
        this.note = note;
        this.author = author;
        this.subject = subject;
        this.creationDate = creationDate;
    }

    public PunishmentNote(String note, UUID author, UUID subject, long creationDate) {
        this.uuid = UUID.randomUUID();
        this.note = note;
        this.author = author;
        this.subject = subject;
        this.creationDate = creationDate;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getNote() {
        return note;
    }

    public UUID getAuthor() {
        return author;
    }

    public UUID getSubject() {
        return subject;
    }

    public long getCreationDate() {
        return creationDate;
    }
}
