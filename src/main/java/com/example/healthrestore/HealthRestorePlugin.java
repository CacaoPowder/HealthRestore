package com.example.healthrestore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class HealthRestorePlugin extends JavaPlugin implements Listener {

    // PDC에 저장할 체력 키
    private static final NamespacedKey HEALTH_KEY = new NamespacedKey("healthsave", "health");

    @Override
    public void onEnable() {
        // 플러그인이 활성화될 때 이벤트 리스너를 등록
        getServer().getPluginManager().registerEvents(this, this);

        // 기본 설정 파일 생성
        this.saveDefaultConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 플레이어가 저장된 체력을 PDC 및 config에서 불러오기
        double health = getPlayerHealth(player);

        // 플레이어의 최대 체력을 체력에 맞게 설정
        if (health > 20) {
            player.setMaxHealth(health); // 체력이 20 이상일 경우 최대 체력 설정
        } else {
            player.setMaxHealth(20); // 최대 체력을 20으로 설정 (기본값)
        }

        // 불러온 체력 값을 플레이어에게 설정
        player.setHealth(health);

        // 체력을 불러왔다는 메시지를 플레이어에게 전송
        player.sendMessage("§a당신의 체력이 성공적으로 복구되었습니다! 현재 체력: " + health);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 플레이어의 체력을 PDC 및 config에 저장
        savePlayerHealth(player);
    }

    // 체력 값을 PDC에 저장하는 메서드
    private void savePlayerHealth(Player player) {
        double health = player.getHealth();

        // PDC에 체력 값 저장
        player.getPersistentDataContainer().set(HEALTH_KEY, PersistentDataType.DOUBLE, health);

        // config.yml에 체력 값 저장
        FileConfiguration config = getConfig();
        config.set("players." + player.getUniqueId().toString(), health);
        saveConfig();  // config.yml에 변경 사항 저장
    }

    // PDC 및 config에서 체력 값을 불러오는 메서드
    private double getPlayerHealth(Player player) {
        // PDC에서 체력 값 가져오기, 없으면 기본값 20.0
        double healthFromPDC = player.getPersistentDataContainer().getOrDefault(HEALTH_KEY, PersistentDataType.DOUBLE, 20.0);

        // config에서 체력 값 가져오기, 없으면 기본값 20.0
        FileConfiguration config = getConfig();
        double healthFromConfig = config.getDouble("players." + player.getUniqueId().toString(), 20.0);

        // PDC에서 불러온 값 우선, 없으면 config에서 가져옴
        return (healthFromPDC != 20.0) ? healthFromPDC : healthFromConfig;
    }
}