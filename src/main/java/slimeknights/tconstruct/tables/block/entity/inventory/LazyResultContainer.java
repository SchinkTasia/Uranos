package slimeknights.tconstruct.tables.block.entity.inventory;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

//SkyJourney
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import java.lang.reflect.Method;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * This class represents an output slot inventory for a crafting inventory.
 * It will calculate the result when requested based on the methods in {@link ILazyCrafter}, and update other slots on recipe take
 */
@RequiredArgsConstructor
public class LazyResultContainer implements Container {
  private final ILazyCrafter crafter;

  /** Cache of the last result */
  @Nullable
  private ItemStack result = null;
  private ItemStack lastModified = ItemStack.EMPTY;

  /**
   * Gets the result of this inventory, lazy loading it if not yet calculated
   * @return  Item stack result
   */
  public final ItemStack getResult() {
    return getResult(null);
  }

  /**
   * Gets the result of this inventory, lazy loading it if not yet calculated
   * @return  Item stack result
   */
  public ItemStack getResult(@Nullable Player player) {
    if (result == null) {
      result = Objects.requireNonNull(crafter.calcResult(player), "Result cannot be null");
    }

    if (!ItemStack.matches(result, lastModified)) {
      if (!result.isEmpty() && result.hasTag()) {
        ToolStack tool = ToolStack.from(result);
        ModifierId modId = new ModifierId("tconstruct", "blacksmith_expertise");

        boolean hasModifier = false;
        boolean hasTool = false;

        CompoundTag tag = result.getTag();
        if (tag != null && tag.contains("tinkertool")) {
          CompoundTag tinkerData = tag.getCompound("tinkertool");
          if (tinkerData.contains("modifiers")) {
            ListTag mods = tinkerData.getList("modifiers", 10);
            for (int i = 0; i < mods.size(); i++) {
              CompoundTag entry = mods.getCompound(i);

              System.out.println(entry.getString("name"));
              System.out.println(entry.getString("level"));

              if (entry.getString("name").equals(modId.toString())) {
                hasModifier = true;
                break;
              }
            }
          }
        }
        
        // Tag definieren: entspricht <tag:items:c:tools>
        TagKey<Item> toolsTag = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", "tools"));
  
        for (Slot slot : menu.getInputSlots()) {
          ItemStack input = slot.getItem();
          if (!input.isEmpty()) {
            Item item = input.getItem();
      
            if (item.builtInRegistryHolder().is(toolsTag)) {
              System.out.println("✅ Tag <c:tools> gefunden bei: " + input.getDisplayName().getString());
              hasTool = true;
            }
          }
        }

        if (!hasModifier && !hasTool) {
          tool.addModifier(modId, 1);
          tool.rebuildStats();
          tool.updateStack(result);
        }
        lastModified = result.copy();
      }
    }

    return result;
  }

  /* Inventory logic */

  @Override
  public ItemStack getItem(int index) {
    return getResult();
  }

  @Override
  public int getContainerSize() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return getResult().isEmpty();
  }

  /**
   * Gets the result of crafting, and consumes required items
   * @param amount  Number to craft
   */
  public void craftResult(Player player, int amount) {
    // get result and consume items
    crafter.onCraft(player, getResult().copy(), amount);
    // clear result cache, items changed
    clearContent();
  }

  /**
   * Returns the result stack from the inventory. This will not consume inputs
   * @param index  Unused
   * @return  Result stack
   * @deprecated use {@link #craftResult(Player, int)} or {@link #getResult()}
   */
  @Deprecated
  @Override
  public ItemStack removeItemNoUpdate(int index) {
    return getResult().copy();
  }

  /**
   * Returns the result stack from the inventory. This will not consume inputs OR edit size
   * @param index  Unused
   * @param count  Unused as output sizes should never change
   * @return  Result stack
   * @deprecated use {@link #craftResult(Player, int)} or {@link #getResult()}
   */
  @Deprecated
  @Override
  public ItemStack removeItem(int index, int count) {
    return getResult().copy();
  }

  /**
   * Clears the result cache, causing the result to be recalculated
   */
  @Override
  public void clearContent() {
    this.result = null;
  }

  /* Required methods */

  /** @deprecated Unsupported method */
  @Deprecated
  @Override
  public void setItem(int index, ItemStack stack) {}

  /** @deprecated Unused method */
  @Deprecated
  @Override
  public void setChanged() {}

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  /**
   * Logic to get results for the lazy results inventory
   */
  public interface ILazyCrafter {
    /**
     * Calculates the recipe result
     * @param  player  Player entity. May be null if not supported.
     *                 May not match the player used in {@link #onCraft(Player, ItemStack, int)} as this result is cached
     * @return  Item stack result
     */
    ItemStack calcResult(@Nullable Player player);

    /**
     * Called when an item is crafted to consume requirements
     * @param player  Player doing the crafting
     * @param result  Crafting result
     * @param amount  Amount to craft
     */
    void onCraft(Player player, ItemStack result, int amount);
  }
}
