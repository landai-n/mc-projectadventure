package com.talkingnewt.minecraft.projectadventure.configuration.entities;

import org.bukkit.GameMode;
import org.bukkit.Location;

import java.io.Serializable;
import java.util.UUID;

public class Ghost implements Serializable {
    private final GameMode m_gameMode;
    private final GsonLocation m_corpseLocation;
    private UUID m_corpseUUID;
    private final GsonLocation m_graveLocation;

    public Ghost(GameMode gameMode, Location corpseLocation, UUID corpseUUID, Location graveLocation) {
        m_gameMode = gameMode;
        m_corpseLocation = new GsonLocation(corpseLocation);
        m_corpseUUID = corpseUUID;
        m_graveLocation = new GsonLocation(graveLocation);
    }

    public Location getCorpseLocation() { return m_corpseLocation.toBukkitLocation(); }
    public GameMode getGameMode() { return m_gameMode; }
    public UUID getCorpseUUID() { return m_corpseUUID; }
    public Location getGraveLocation() { return m_graveLocation.toBukkitLocation(); }

    public void setCorpseUUID(UUID uuid) { m_corpseUUID = uuid; }
}