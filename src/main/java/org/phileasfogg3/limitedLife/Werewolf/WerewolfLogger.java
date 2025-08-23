package org.phileasfogg3.limitedLife.Werewolf;

import net.nexia.nexiaapi.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WerewolfLogger {

    private final File logFile;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public WerewolfLogger(JavaPlugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        logFile = new File(plugin.getDataFolder(), "Werewolf.log");
    }

    public void log(String message) {

        String time = LocalDateTime.now().format(formatter);
        String line = "[" + time + "] " + message;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
