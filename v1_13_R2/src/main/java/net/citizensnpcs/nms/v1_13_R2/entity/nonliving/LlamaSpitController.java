package net.citizensnpcs.nms.v1_13_R2.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLlamaSpit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_13_R2.util.NMSImpl;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.EntityLlama;
import net.minecraft.server.v1_13_R2.EntityLlamaSpit;
import net.minecraft.server.v1_13_R2.FluidType;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.Tag;
import net.minecraft.server.v1_13_R2.World;
import net.minecraft.server.v1_13_R2.WorldServer;

public class LlamaSpitController extends AbstractEntityController {
    public LlamaSpitController() {
        super(EntityLlamaSpitNPC.class);
    }

    @Override
    protected Entity createEntity(Location at, NPC npc) {
        WorldServer ws = ((CraftWorld) at.getWorld()).getHandle();
        final EntityLlamaSpitNPC handle = new EntityLlamaSpitNPC(ws, npc);
        handle.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getPitch(), at.getYaw());
        return handle.getBukkitEntity();
    }

    @Override
    public LlamaSpit getBukkitEntity() {
        return (LlamaSpit) super.getBukkitEntity();
    }

    public static class EntityLlamaSpitNPC extends EntityLlamaSpit implements NPCHolder {
        private final CitizensNPC npc;

        public EntityLlamaSpitNPC(World world) {
            this(world, null);
        }

        public EntityLlamaSpitNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
        }

        public EntityLlamaSpitNPC(World world, NPC npc, EntityLlama entity) {
            super(world, entity);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public boolean b(Tag<FluidType> tag) {
            return NMSImpl.fluidPush(npc, this, () -> super.b(tag));
        }

        @Override
        public void collide(net.minecraft.server.v1_13_R2.Entity entity) {
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
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder)) {
                bukkitEntity = new LlamaSpitNPC(this);
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void tick() {
            if (npc != null) {
                npc.update();
                if (!npc.isProtected()) {
                    super.tick();
                }
            } else {
                super.tick();
            }
        }
    }

    public static class LlamaSpitNPC extends CraftLlamaSpit implements NPCHolder {
        private final CitizensNPC npc;

        public LlamaSpitNPC(EntityLlamaSpitNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}