package net.aspw.client.features.api;

import io.netty.buffer.Unpooled;
import net.aspw.client.Client;
import net.aspw.client.event.*;
import net.aspw.client.features.module.impl.combat.KillAura;
import net.aspw.client.features.module.impl.combat.TPAura;
import net.aspw.client.features.module.impl.other.ClientSpoof;
import net.aspw.client.features.module.impl.visual.Animations;
import net.aspw.client.features.module.impl.visual.Cape;
import net.aspw.client.protocol.ProtocolBase;
import net.aspw.client.util.EntityUtils;
import net.aspw.client.util.MinecraftInstance;
import net.aspw.client.util.PacketUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.raphimc.vialoader.util.VersionEnum;

import java.util.Objects;

public class PacketManager extends MinecraftInstance implements Listenable {

    public static int ticks;
    public static String selectedCape;
    public static int swing;
    public static boolean isVisualBlocking = false;
    private static boolean flagged = false;

    public static void update() {
        int maxFrames = 40;

        switch (Objects.requireNonNull(Client.moduleManager.getModule(Cape.class)).getStyleValue().get()) {
            case "Rise5":
                selectedCape = "rise5";
                maxFrames = 14;
                break;
            case "NightX":
                selectedCape = "nightx";
                maxFrames = 14;
                break;
        }

        if (mc.thePlayer.ticksExisted % 2 == 0)
            ticks++;

        if (ticks > maxFrames) {
            ticks = 1;
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        flagged = false;
    }

    @EventTarget
    public void onTeleport(TeleportEvent event) {
        flagged = true;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        for (Entity en : mc.theWorld.loadedEntityList) {
            if (shouldStopRender(en)) {
                en.renderDistanceWeight = 0.0;
            } else {
                en.renderDistanceWeight = 1.0;
            }
        }
    }

    public static boolean shouldStopRender(Entity entity) {
        return (EntityUtils.isMob(entity) ||
                EntityUtils.isAnimal(entity) ||
                entity instanceof EntityBoat ||
                entity instanceof EntityMinecart ||
                entity instanceof EntityItemFrame ||
                entity instanceof EntityTNTPrimed ||
                entity instanceof EntityArmorStand) &&
                entity != mc.thePlayer && mc.thePlayer.getDistanceToEntity(entity) > 45.0f;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        mc.leftClickCounter = 0;
        if (Animations.swingAnimValue.get().equals("Smooth") && event.getEventState() == EventState.PRE) {
            if (mc.thePlayer.swingProgressInt == 1) {
                swing = 9;
            } else {
                swing = Math.max(0, swing - 1);
            }
        }
        final KillAura killAura = Objects.requireNonNull(Client.moduleManager.getModule(KillAura.class));
        final TPAura tpAura = Objects.requireNonNull(Client.moduleManager.getModule(TPAura.class));
        if (Animations.swingLimitOnlyBlocking.get()) {
            if (mc.thePlayer.swingProgress >= 1f)
                mc.thePlayer.isSwingInProgress = false;
            if (mc.thePlayer.isBlocking() || (killAura.getState() && killAura.getTarget() != null && !killAura.getAutoBlockModeValue().get().equals("None") || tpAura.getState() && tpAura.isBlocking())) {
                if (mc.thePlayer.swingProgress >= Animations.swingLimit.get())
                    mc.thePlayer.isSwingInProgress = false;
            }
        } else if (mc.thePlayer.swingProgress >= Animations.swingLimit.get()) {
            mc.thePlayer.isSwingInProgress = false;
        }
        if (Animations.sigmaHeld.get())
            mc.thePlayer.renderArmPitch = mc.thePlayer.rotationPitch - 30;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();
        final ClientSpoof clientSpoof = Client.moduleManager.getModule(ClientSpoof.class);

        if (packet instanceof C03PacketPlayer && flagged) {
            event.cancelEvent();
            PacketUtils.sendPacketNoEvent(
                    new C03PacketPlayer.C06PacketPlayerPosLook(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            mc.thePlayer.rotationYaw,
                            mc.thePlayer.rotationPitch,
                            mc.thePlayer.onGround
                    )
            );
            flagged = false;
        }

        if (ProtocolBase.getManager().getTargetVersion().isNewerThanOrEqualTo(VersionEnum.r1_10)) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                ((C08PacketPlayerBlockPlacement) packet).facingX = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingY = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingZ = 0.5F;
            }
        }

        if (!MinecraftInstance.mc.isIntegratedServerRunning()) {
            if (packet instanceof C17PacketCustomPayload) {
                if (((C17PacketCustomPayload) event.getPacket()).getChannelName().equalsIgnoreCase("MC|Brand")) {
                    switch (Objects.requireNonNull(clientSpoof).modeValue.get()) {
                        case "Vanilla":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("vanilla")));
                            break;
                        case "Forge":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("FML")));
                            break;
                        case "OptiFine":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("optifine")));
                            break;
                        case "Fabric":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("fabric")));
                            break;
                        case "LabyMod":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("LMC")));
                            break;
                        case "CheatBreaker":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("CB")));
                            break;
                        case "PvPLounge":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("PLC18")));
                            break;
                        case "Geyser":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("eyser")));
                            break;
                        case "Lunar":
                            PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("REGISTER", (new PacketBuffer(Unpooled.buffer())).writeString("Lunar-Client")));
                            break;
                    }
                }
                event.cancelEvent();
            }
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}