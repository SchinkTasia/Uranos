package slimeknights.tconstruct.tables.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
//import slimeknights.tconstruct.library.tools.nbt.ModifierSlotManager;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.lang.reflect.Method;

/**
 * Slot for display of {@link LazyResultContainer}.
 */
@SuppressWarnings("WeakerAccess")
public class LazyResultSlot extends Slot {
  protected final LazyResultContainer inventory;
  protected int amountCrafted = 0;
  public LazyResultSlot(LazyResultContainer inventory, int xPosition, int yPosition) {
    super(inventory, 0, xPosition, yPosition);
    this.inventory = inventory;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return false;
  }

  @Override
  public ItemStack remove(int amount) {
    if (this.hasItem()) {
      this.amountCrafted += Math.min(amount, this.getItem().getCount());
    }

    return super.remove(amount);
  }

  @Override
  public void onTake(Player player, ItemStack stack) {
    inventory.craftResult(player, amountCrafted);
    amountCrafted = 0;

    if (player.containerMenu instanceof TinkerStationContainerMenu menu) {
      // Tag definieren: entspricht <tag:items:c:tools>
      TagKey<Item> toolsTag = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "tools"));
    
      for (Slot slot : menu.getInputSlots()) {
        ItemStack input = slot.getItem();
        if (!input.isEmpty()) {
          Item item = input.getItem();
    
          if (item.builtInRegistryHolder().is(toolsTag)) {
            System.out.println("✅ Tag <c:tools> gefunden bei: " + input.getDisplayName().getString());
          }else{
            System.out.println("❌ Tag <c:tools> nicht gefunden bei: " + input.getDisplayName().getString());
          }
        }
      }
    }
  }
  

  @Override
  protected void onQuickCraft(ItemStack stack, int amount) {
    this.amountCrafted += amount;
    this.checkTakeAchievements(stack);
  }

  @Override
  protected void onSwapCraft(int numItemsCrafted) {
    this.amountCrafted += numItemsCrafted;
  }
}
