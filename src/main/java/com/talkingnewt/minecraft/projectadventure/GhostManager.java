package com.talkingnewt.minecraft.projectadventure;

import com.talkingnewt.minecraft.projectadventure.configuration.entities.Ghost;
import com.talkingnewt.minecraft.projectadventure.helpers.DistanceComparator2D;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GhostManager implements Listener {
    private final Plugin m_plugin;
    private final GraveManager m_graveManager;
    private final Map<UUID, Ghost> m_ghosts;
    private final Map<UUID, NPC> m_corpses = new HashMap<>();
    private final Map<UUID, Integer> m_rescueCounter = new HashMap<>();

    public GhostManager(Plugin plugin, Map<UUID, Ghost> ghosts, GraveManager graveHandler) {
        m_plugin = plugin;
        m_ghosts = ghosts;
        m_graveManager = graveHandler;
    }

    private void spawnCorpse(Player player, Ghost ghost) {
        NPC corpse;

        if (ghost.getCorpseUUID() == null) {
            corpse = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getDisplayName());
            ghost.setCorpseUUID(corpse.getUniqueId());
        } else {
            if (m_corpses.get(ghost.getCorpseUUID()) != null) {
                return;
            }
            corpse = CitizensAPI.getNPCRegistry().getByUniqueId(ghost.getCorpseUUID());
        }

        corpse.spawn(ghost.getCorpseLocation());
        m_corpses.put(ghost.getCorpseUUID(), corpse);
    }

    private void destroyCorpse(Ghost ghost) {
        var corpse = CitizensAPI.getNPCRegistry().getByUniqueId(ghost.getCorpseUUID());

        m_corpses.remove(ghost.getCorpseUUID());
        if (corpse != null) {
            corpse.destroy();
            CitizensAPI.getNPCRegistry().deregister(corpse);
        }
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (isGhost(player)) {
            setGhostMode(player, isGhost(player), null);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final var player = event.getPlayer();
        final var graveLocationOpt = m_graveManager.nearestGrave(player);

        if (graveLocationOpt.isPresent()) {
            event.setRespawnLocation(calculateRespawnPoint(graveLocationOpt.get()));
        } else {
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
        }

        setGhostMode(player, true, event.getRespawnLocation());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isGhost(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isGhost(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        if (isGhost((Player) event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isGhost(event.getPlayer())) {
            return;
        }

        var ghost = m_ghosts.get(event.getPlayer().getUniqueId());

        if (DistanceComparator2D.distance(event.getPlayer().getLocation(), ghost.getCorpseLocation()) < 3) {
            setGhostMode(event.getPlayer(), false, null);
        }
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }

        Player player = event.getPlayer();

        if (event.getItem().getType() == Material.COMPASS) {
            var counter = m_rescueCounter.get(player.getUniqueId());

            if (counter != null) {
                if (counter > 0) {
                    setNoClipGhost(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(m_plugin, () -> setNormalGhost(player), 30L);
                    m_rescueCounter.put(player.getUniqueId(), counter - 1);
                }
            }
        }

        if (event.getItem().getType() == Material.BELL) {
            respawnToGrave(player);
        }
    }

    private boolean isGhost(Player player) {
        if (player == null) { return false; }

        return m_ghosts.get(player.getUniqueId()) != null;
    }

    private void respawnToGrave(Player player) {
        var ghost = m_ghosts.get(player.getUniqueId());

        if (ghost != null) {
            player.teleport(ghost.getGraveLocation());
            setGhostMode(player, false, null);
        }
    }

    private void setNormalGhost(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setCollidable(false);
        player.setInvulnerable(true);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCanPickupItems(false);
        player.setGlowing(true);
    }

    private void setNoClipGhost(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void setGhostMode(Player player, boolean isGhost, Location graveLocation) {
        if (isGhost) {
            var ghost = m_ghosts.get(player.getUniqueId());

            if (ghost == null) {
                ghost = new Ghost(player.getGameMode(), player.getLocation(), null, graveLocation);
                m_ghosts.put(player.getUniqueId(), ghost);
            }
            spawnCorpse(player, ghost);
            m_rescueCounter.put(player.getUniqueId(), 3);

            final Location corpseLocation = ghost.getCorpseLocation();
            Bukkit.getScheduler().scheduleSyncDelayedTask(m_plugin, () -> {
                var bodyCompass = new ItemStack(Material.COMPASS);
                var bodyCompassMeta = Objects.requireNonNull(bodyCompass.getItemMeta());
                bodyCompassMeta.setDisplayName("Find your body (Right click if you are stuck)");
                bodyCompass.setItemMeta(bodyCompassMeta);

                var graveBell = new ItemStack(Material.BELL);
                var graveBellMeta = Objects.requireNonNull(graveBell.getItemMeta());
                graveBellMeta.setDisplayName("Respawn at your grave.");
                graveBell.setItemMeta(graveBellMeta);

                player.setGameMode(GameMode.ADVENTURE);
                player.setCollidable(false);
                player.setInvulnerable(true);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setCanPickupItems(false);
                player.setGlowing(true);
                player.getInventory().clear();
                player.setHealth(1);
                player.setFoodLevel(0);
                player.setPlayerTime(18000, false);
                player.setPlayerWeather(WeatherType.DOWNFALL);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 36000, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 36000, 0, false, false, false));
                player.getInventory().addItem(bodyCompass);
                player.getInventory().addItem(graveBell);
                player.setCompassTarget(corpseLocation);
            });

        } else {
            player.setGameMode(GameMode.SURVIVAL);
            player.setCollidable(true);
            player.setInvulnerable(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setCanPickupItems(true);
            player.setGlowing(false);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.resetPlayerWeather();
            player.resetPlayerTime();
            player.getInventory().clear();

            var ghost = m_ghosts.get(player.getUniqueId());
            if (ghost != null) {
                player.setGameMode(ghost.getGameMode());
                m_ghosts.remove(player.getUniqueId());
                m_rescueCounter.remove(player.getUniqueId());
                destroyCorpse(ghost);
            }
        }
    }

    public Location calculateRespawnPoint(Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY() + 2, location.getZ() - 3);
    }
}
