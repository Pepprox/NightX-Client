package net.aspw.client.features.module.impl.player

import net.aspw.client.event.EventTarget
import net.aspw.client.event.JumpEvent
import net.aspw.client.event.MoveEvent
import net.aspw.client.event.UpdateEvent
import net.aspw.client.features.module.Module
import net.aspw.client.features.module.ModuleCategory
import net.aspw.client.features.module.ModuleInfo
import net.aspw.client.util.MovementUtils
import net.aspw.client.value.FloatValue
import net.aspw.client.value.ListValue
import java.util.*

@ModuleInfo(name = "HighJump", spacedName = "High Jump", description = "", category = ModuleCategory.PLAYER)
class HighJump : Module() {
    private val heightValue = FloatValue("Height", 5f, 1.0f, 10f, "m")
    private val modeValue =
        ListValue("Mode", arrayOf("Vanilla", "Damage", "AACv3", "DAC", "Mineplex"), "Vanilla")
    var tick = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "damage" -> if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) mc.thePlayer.motionY += (0.42f * heightValue.get()).toDouble()
            "aacv3" -> if (!mc.thePlayer.onGround) mc.thePlayer.motionY += 0.059
            "dac" -> if (!mc.thePlayer.onGround) mc.thePlayer.motionY += 0.049999
            "mineplex" -> if (!mc.thePlayer.onGround) MovementUtils.strafe(0.35f)
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent?) {
        if (!mc.thePlayer.onGround) {
            if ("mineplex".equals(modeValue.get(), ignoreCase = true)) {
                mc.thePlayer.motionY += if (mc.thePlayer.fallDistance == 0f) 0.0499 else 0.05
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "vanilla" -> event.motion = event.motion * heightValue.get()
            "mineplex" -> event.motion = 0.47f
        }
    }

    override val tag: String
        get() = modeValue.get()
}