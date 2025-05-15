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
    if (stack == null || stack.isEmpty() || !stack.hasTag()) {
      System.out.println("⚠️ Kein valider Tinker-Stack – Verarbeitung abgebrochen.");
      inventory.craftResult(player, amountCrafted);
      amountCrafted = 0;
      return;
    }
    
    ToolStack tool = ToolStack.from(stack);
    

    System.out.println("Item: "+tool.getItem().toString());
    System.out.println("Etappe 1: onTake gestartet");

    try {
      System.out.println("Etappe 2: Lade PowerHolderComponent");
      Class<?> componentClass = Class.forName("io.github.apace100.apoli.component.PowerHolderComponent");

      System.out.println("Etappe 3: Zugriff auf KEY-Feld");
      Object key = componentClass.getField("KEY").get(null);

      System.out.println("Etappe 4: Rufe get(player) auf");
      Method getMethod = key.getClass().getMethod("get", Object.class);
      Object powerContainer = getMethod.invoke(key, player);

      System.out.println("Etappe 5: Suche hasPower-Methode");
      Method hasPowerMethod = powerContainer.getClass().getMethod("hasPower", Class.forName("io.github.apace100.apoli.power.PowerType"));

      System.out.println("Etappe 6: Lade ClassPowerTypes");
      Class<?> powerTypes = Class.forName("io.github.apace100.originsclasses.power.ClassPowerTypes");

      System.out.println("Etappe 7: Hole QUALITY_EQUIPMENT-Power");
      Object power = powerTypes.getField("QUALITY_EQUIPMENT").get(null);

      System.out.println("Etappe 8: Rufe hasPower mit QUALITY_EQUIPMENT auf");
      boolean active = (boolean) hasPowerMethod.invoke(powerContainer, power);

      System.out.println("Etappe 9: Ergebnis - Power ist " + (active ? "AKTIV!" : "NICHT aktiv."));

    } catch (Exception e) {
      System.out.println("Etappe X: Fehler beim Zugriff auf Apoli oder OriginsClasses!");
      e.printStackTrace(); // optional: stack trace für Debug
    }

    inventory.craftResult(player, amountCrafted);

    if (!tool.isBroken()) {
  
      ModifierId modifierId = new ModifierId("tconstruct", "blacksmith_expertise");
      Modifier modifier = ModifierManager.INSTANCE.get(modifierId);
      tool.addModifier(new ModifierId("tconstruct", "blacksmith_expertise"), 1);
      tool.rebuildStats();
    }
  
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
