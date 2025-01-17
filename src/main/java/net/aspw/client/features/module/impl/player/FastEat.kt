package net.aspw.client.features.module.impl.player

import net.aspw.client.event.EventTarget
import net.aspw.client.event.UpdateEvent
import net.aspw.client.features.module.Module
import net.aspw.client.features.module.ModuleCategory
import net.aspw.client.features.module.ModuleInfo
import net.aspw.client.util.timer.MSTimer
import net.aspw.client.value.FloatValue
import net.aspw.client.value.IntegerValue
import net.aspw.client.value.ListValue
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C03PacketPlayer
import java.util.*

@ModuleInfo(name = "FastEat", spacedName = "Fast Eat", description = "", category = ModuleCategory.PLAYER)
class FastEat : Module() {

    private val modeValue = ListValue("Mode", arrayOf("NCP", "AAC", "AAC4", "Matrix", "Grim", "Delayed"), "NCP")

    private val delayValue = IntegerValue("CustomDelay", 0, 0, 300) { modeValue.get().equals("delayed", true) }
    private val customSpeedValue =
        IntegerValue("CustomSpeed", 2, 0, 35, " packet") { modeValue.get().equals("delayed", true) }
    private val customTimer =
        FloatValue("CustomTimer", 1.1f, 0.5f, 2f, "x") { modeValue.get().equals("delayed", true) }

    private val msTimer = MSTimer()
    private var usedTimer = false

    override val tag: String
        get() = modeValue.get()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (!mc.thePlayer.isUsingItem) {
            msTimer.reset()
            return
        }

        val usingItem = mc.thePlayer.itemInUse.item

        if (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion) {
            when (modeValue.get().lowercase(Locale.getDefault())) {
                "ncp" -> if (mc.thePlayer.itemInUseDuration > 14) {
                    repeat(20) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }

                "grim" -> {
                    mc.timer.timerSpeed = 0.3F
                    usedTimer = true
                    repeat(34) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                }

                "matrix" -> {
                    mc.timer.timerSpeed = 0.5f
                    usedTimer = true
                }

                "aac" -> {
                    mc.timer.timerSpeed = 1.1F
                    usedTimer = true
                }

                "delayed" -> {
                    mc.timer.timerSpeed = customTimer.get()
                    usedTimer = true

                    if (!msTimer.hasTimePassed(delayValue.get().toLong()))
                        return

                    repeat(customSpeedValue.get()) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    msTimer.reset()
                }

                "aac4" -> {
                    mc.timer.timerSpeed = 0.49F
                    usedTimer = true
                    if (mc.thePlayer.itemInUseDuration > 13) {
                        repeat(23) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                        }

                        mc.playerController.onStoppedUsingItem(mc.thePlayer)
                    }
                }
            }
        }
    }

    override fun onDisable() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
    }
}