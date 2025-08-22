package org.phileasfogg3.limitedLife.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.phileasfogg3.limitedLife.LimitedLife;

import java.util.HashSet;
import java.util.Set;

public class RecipesManager implements Listener {

    public void tntRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(LimitedLife.Instance, "life_tnt"), new ItemStack(Material.TNT));
        r.shape(
                "PSP",
                "SGS",
                "PSP"
        );
        r.setIngredient('P', Material.PAPER);
        r.setIngredient('S', Material.SAND);
        r.setIngredient('G', Material.GUNPOWDER);

        Bukkit.addRecipe(r);
    }

    public void spawnerRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(LimitedLife.Instance, "spawner"), new ItemStack(Material.SPAWNER));
        r.shape(
                "III",
                "I I",
                "III"
        );
        r.setIngredient('I', Material.IRON_BARS);

        Bukkit.addRecipe(r);
    }

    public void saddleRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(LimitedLife.Instance, "saddle"), new ItemStack(Material.SADDLE));
        r.shape(
                "   ",
                " L ",
                "L L"
        );
        r.setIngredient('L', Material.LEATHER);

        Bukkit.addRecipe(r);
    }

    public void nameTagRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(LimitedLife.Instance, "name_tag"), new ItemStack(Material.NAME_TAG));
        r.shape(
                "  S",
                " P ",
                "S  "
        );
        r.setIngredient('P', Material.PAPER);
        r.setIngredient('S', Material.STRING);

        Bukkit.addRecipe(r);
    }
}