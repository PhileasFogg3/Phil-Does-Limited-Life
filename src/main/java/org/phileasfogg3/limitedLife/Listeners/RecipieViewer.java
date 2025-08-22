package org.phileasfogg3.limitedLife.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Set;

public class RecipieViewer implements Listener {

    private static final Set<Material> BLOCKED_RECIPES = Set.of(
            Material.BOOKSHELF,
            Material.ENCHANTING_TABLE
    );

    public void openRecipeGUI(Player player, String recipeName) {

        Inventory gui = Bukkit.createInventory(null, InventoryType.WORKBENCH, ChatColor.RED + "Limited Life Crafting Tweaks");

        ItemStack result = null;

        switch (recipeName.toLowerCase()) {
            case "tnt":
                gui.setItem(1, new ItemStack(Material.PAPER));
                gui.setItem(2, new ItemStack(Material.SAND));
                gui.setItem(3, new ItemStack(Material.PAPER));
                gui.setItem(4, new ItemStack(Material.SAND));
                gui.setItem(5, new ItemStack(Material.GUNPOWDER));
                gui.setItem(6, new ItemStack(Material.SAND));
                gui.setItem(7, new ItemStack(Material.PAPER));
                gui.setItem(8, new ItemStack(Material.SAND));
                gui.setItem(9, new ItemStack(Material.PAPER));
                result = new ItemStack(Material.TNT);
                System.out.println("showing tnt recipe");
                break;
            case "spawner":
                gui.setItem(1, new ItemStack(Material.IRON_BARS));
                gui.setItem(2, new ItemStack(Material.IRON_BARS));
                gui.setItem(3, new ItemStack(Material.IRON_BARS));
                gui.setItem(4, new ItemStack(Material.IRON_BARS));
                gui.setItem(6, new ItemStack(Material.IRON_BARS));
                gui.setItem(7, new ItemStack(Material.IRON_BARS));
                gui.setItem(8, new ItemStack(Material.IRON_BARS));
                gui.setItem(9, new ItemStack(Material.IRON_BARS));
                result = new ItemStack(Material.SPAWNER);
                System.out.println("showing spawner recipe");
                break;
            case "enchanting_table":
                gui.setItem(2, new ItemStack(Material.BOOK));
                gui.setItem(4, new ItemStack(Material.DIAMOND));
                gui.setItem(5, new ItemStack(Material.OBSIDIAN));
                gui.setItem(6, new ItemStack(Material.DIAMOND));
                gui.setItem(7, new ItemStack(Material.OBSIDIAN));
                gui.setItem(8, new ItemStack(Material.OBSIDIAN));
                gui.setItem(9, new ItemStack(Material.OBSIDIAN));
                result = new ItemStack(Material.ENCHANTING_TABLE);
                System.out.println("showing enchanter recipe");
                break;
            case "saddle":
                gui.setItem(5, new ItemStack(Material.LEATHER));
                gui.setItem(7, new ItemStack(Material.LEATHER));
                gui.setItem(9, new ItemStack(Material.LEATHER));
                result = new ItemStack(Material.SADDLE);
                System.out.println("showing saddle recipe");
                break;
            case "name_tag":
                gui.setItem(3, new ItemStack(Material.STRING));
                gui.setItem(5, new ItemStack(Material.PAPER));
                gui.setItem(7, new ItemStack(Material.STRING));
                result = new ItemStack(Material.NAME_TAG);
                System.out.println("showing name tag recipe");
                break;
            case "bookshelf":
                gui.setItem(1, new ItemStack(Material.OAK_PLANKS));
                gui.setItem(2, new ItemStack(Material.OAK_PLANKS));
                gui.setItem(3, new ItemStack(Material.OAK_PLANKS));
                gui.setItem(4, new ItemStack(Material.BOOK));
                gui.setItem(5, new ItemStack(Material.BOOK));
                gui.setItem(6, new ItemStack(Material.BOOK));
                gui.setItem(7, new ItemStack(Material.OAK_PLANKS));
                gui.setItem(8, new ItemStack(Material.OAK_PLANKS));
                gui.setItem(9, new ItemStack(Material.OAK_PLANKS));
                result = new ItemStack(Material.BOOKSHELF);
                System.out.println("showing bookshelf recipe");
                break;
        }

        if (result != null) {
            ItemStack displayItem;

            if (recipeName.equalsIgnoreCase("bookshelf") || recipeName.equalsIgnoreCase("enchanting_table")) {
                // Show barrier block instead of real result
                displayItem = new ItemStack(Material.BARRIER);
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + "Unavailable");
                    meta.setLore(List.of(ChatColor.GRAY + "You cannot craft this on Limited Life"));
                    displayItem.setItemMeta(meta);
                }
            } else {
                // Show real result
                displayItem = result;
                ItemMeta meta = displayItem.getItemMeta();
                String meta2 = displayItem.getItemMeta().getDisplayName();
                if (meta != null) {
                    meta.setDisplayName(meta2);
                    displayItem.setItemMeta(meta);
                }
            }

            gui.setItem(0, displayItem);
        }

        player.openInventory(gui);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.RED + "Limited Life Crafting Tweaks")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result != null && BLOCKED_RECIPES.contains(result.getType())) {
            event.getInventory().setResult(null); // Block crafting
        }
    }

}
