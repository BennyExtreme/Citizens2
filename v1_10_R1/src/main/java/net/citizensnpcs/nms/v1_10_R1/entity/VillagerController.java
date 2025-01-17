package net.citizensnpcs.nms.v1_10_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftVillager;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_10_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_10_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityLightning;
import net.minecraft.server.v1_10_R1.EntityVillager;
import net.minecraft.server.v1_10_R1.EnumHand;
import net.minecraft.server.v1_10_R1.IBlockData;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.SoundEffect;
import net.minecraft.server.v1_10_R1.World;

public class VillagerController extends MobEntityController {
    public VillagerController() {
        super(EntityVillagerNPC.class);
    }

    @Override
    public Villager getBukkitEntity() {
        return (Villager) super.getBukkitEntity();
    }

    public static class EntityVillagerNPC extends EntityVillager implements NPCHolder {
        private boolean blockingATrade;

        private final CitizensNPC npc;

        public EntityVillagerNPC(World world) {
            this(world, null);
        }

        public EntityVillagerNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public void a(boolean flag) {
            if (npc == null) {
                super.a(flag);
                return;
            }
            NMSImpl.checkAndUpdateHeight(this, flag, super::a);
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public void a(Entity entity, float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, (evt) -> super.a(entity, (float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public boolean a(EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
            if (npc != null && npc.data().get(NPC.Metadata.VILLAGER_BLOCK_TRADES, true)) {
                blockingATrade = true;
            }
            return super.a(entityhuman, enumhand, itemstack);
        }

        @Override
        public int aY() {
            return NMS.getFallDistance(npc, super.aY());
        }

        @Override
        protected SoundEffect bV() {
            return NMSImpl.getSoundEffect(npc, super.bV(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        protected SoundEffect bW() {
            return NMSImpl.getSoundEffect(npc, super.bW(), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public void collide(net.minecraft.server.v1_10_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        public boolean dh() {
            if (blockingATrade) {
                blockingATrade = false;
                return true;
            }
            return super.dh();
        }

        @Override
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            }
        }

        @Override
        public void enderTeleportTo(double d0, double d1, double d2) {
            NMS.enderTeleportTo(npc, () -> super.enderTeleportTo(d0, d1, d2));
        }

        @Override
        public void g(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void g(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.g(f, f1);
            } else {
                NMSImpl.flyingMoveLogic(this, f, f1);
            }
        }

        @Override
        protected SoundEffect G() {
            return NMSImpl.getSoundEffect(npc, super.G(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder))
                bukkitEntity = new VillagerNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        protected void L() {
            if (npc == null) {
                super.L();
            }
        }

        @Override
        public void M() {
            super.M();
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        public boolean m_() {
            if (npc == null || !npc.isFlyable()) {
                return super.m_();
            } else {
                return false;
            }
        }

        @Override
        public void onLightningStrike(EntityLightning entitylightning) {
            if (npc == null) {
                super.onLightningStrike(entitylightning);
            }
        }
    }

    public static class VillagerNPC extends CraftVillager implements NPCHolder {
        private final CitizensNPC npc;

        public VillagerNPC(EntityVillagerNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}