package slimeknights.tconstruct.tools.modifiers.upgrades.general;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

public class BlacksmithExpertise extends Modifier {

  @Override
  public void addVolatileData(ToolRebuildContext context, int level, ModDataNBT volatileData) {
    volatileData.addSlots(SlotType.UPGRADE, level); // +1 Upgrade Slot pro Stufe
  }
}