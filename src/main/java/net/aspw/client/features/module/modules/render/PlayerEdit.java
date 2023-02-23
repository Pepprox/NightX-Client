package net.aspw.client.features.module.modules.render;

import net.aspw.client.features.module.Module;
import net.aspw.client.features.module.ModuleCategory;
import net.aspw.client.features.module.ModuleInfo;
import net.aspw.client.value.BoolValue;
import net.aspw.client.value.FloatValue;

@ModuleInfo(name = "PlayerEdit", spacedName = "Player Edit", category = ModuleCategory.RENDER)
public class PlayerEdit extends Module {

    public static BoolValue editPlayerSizeValue = new BoolValue("EditPlayerSize", false);
    public static FloatValue playerSizeValue = new FloatValue("PlayerSize", 0.5f, 0.01f, 5f, "m", () -> editPlayerSizeValue.get());
    public static BoolValue rotatePlayer = new BoolValue("RotatePlayer", true);
}