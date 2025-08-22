package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class GUIManager {

    private Config playerData;
    private Config gameMgr;
    private Config werewolf;

    public GUIManager(Config playerData, Config gameMgr, Config werewolf) {
        this.playerData = playerData;
        this.gameMgr = gameMgr;
        this.werewolf = werewolf;
    }

    public static void open(Player viewer, String title, List<Player> players) {
        viewer.openInventory(create(title, players));
    }

    public static Inventory create(String title, List<Player> players) {

        int rows = (int) Math.ceil(players.size() / 9.0);
        if (rows == 0) rows = 1;
        if (rows > 6) rows = 6;
        int size = rows  * 9;

        Inventory inv = Bukkit.createInventory(null, size, title);

        int placed = 0;

        for (Player p : players) {
            if (placed >= size) break;

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            if (meta !=null) {
                meta.setOwningPlayer(p);
                meta.setDisplayName(ChatColor.RED + "Accuse " + ChatColor.GREEN + p.getName());
                skull.setItemMeta(meta);
            }

            int row = placed / 9;
            int indexInRow = placed % 9;
            int playersInThisRow = Math.min(players.size() - row * 9, 9);
            int start = (9 - playersInThisRow) / 2;

            inv.setItem(row * 9 + start + indexInRow, skull);
            placed++;

        }

        return inv;

    }
}
