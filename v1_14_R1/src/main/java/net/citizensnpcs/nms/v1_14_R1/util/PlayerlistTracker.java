package net.citizensnpcs.nms.v1_14_R1.util;

import java.lang.invoke.MethodHandle;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTrackerEntry;
import net.minecraft.server.v1_14_R1.PlayerChunkMap;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;

public class PlayerlistTracker extends PlayerChunkMap.EntityTracker {
    private EntityPlayer lastUpdatedPlayer;
    private final Entity tracker;

    public PlayerlistTracker(PlayerChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.tracker = entity;
    }

    public PlayerlistTracker(PlayerChunkMap map, EntityTracker entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getD(entry), getE(entry));
    }

    public void updateLastPlayer() {
        if (tracker.dead || lastUpdatedPlayer == null || tracker.getBukkitEntity().getType() != EntityType.PLAYER)
            return;
        final EntityPlayer entityplayer = lastUpdatedPlayer;
        NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        if (!Setting.DISABLE_TABLIST.asBoolean())
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
            }
        }, Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks());
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (!(entityplayer instanceof EntityHumanNPC)) {
            // prevent updates to NPC "viewers"
            if (tracker instanceof NPCHolder
                    && ((NPCHolder) tracker).getNPC().isHiddenFrom(entityplayer.getBukkitEntity()))
                return;
            lastUpdatedPlayer = entityplayer;
            super.updatePlayer(entityplayer);
        }
    }

    private static int getD(EntityTracker entry) {
        try {
            return (int) D.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean getE(EntityTracker entry) {
        try {
            return (boolean) E.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Entity getTracker(EntityTracker entry) {
        try {
            return (Entity) TRACKER.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getTrackingDistance(EntityTracker entry) {
        try {
            Entity entity = getTracker(entry);
            if (entity instanceof NPCHolder) {
                return ((NPCHolder) entity).getNPC().data().get(NPC.Metadata.TRACKING_RANGE,
                        (Integer) TRACKING_RANGE.invoke(entry));
            }
            return (Integer) TRACKING_RANGE.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static final MethodHandle D = NMS.getGetter(EntityTrackerEntry.class, "d");
    private static final MethodHandle E = NMS.getGetter(EntityTrackerEntry.class, "e");
    private static final MethodHandle TRACKER = NMS.getGetter(EntityTracker.class, "tracker");
    private static final MethodHandle TRACKER_ENTRY = NMS.getGetter(EntityTracker.class, "trackerEntry");
    private static final MethodHandle TRACKING_RANGE = NMS.getGetter(EntityTracker.class, "trackingDistance");
}
