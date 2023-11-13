package xyz.kadr.yoko;

import emu.grasscutter.plugin.Plugin;
import emu.grasscutter.server.event.EventHandler;
import emu.grasscutter.server.event.HandlerPriority;
import emu.grasscutter.server.event.entity.EntityDeathEvent;

import lombok.Getter;

public final class DPSMeter extends Plugin {

    @Getter private static DPSMeter instance;

    @Override
    public void onLoad() {
        DPSMeter.instance = this;

        this.getLogger().info("Loaded DPS meter.");
    }

    @Override
    public void onEnable() {
        // Register events.
        new EventHandler<>(EntityDeathEvent.class)
                .priority(HandlerPriority.NORMAL)
                .listener(EventListener::EntityDeathEvent)
                .register(this);

        // Register commands.
        this.getHandle().registerCommand(new DPSCommand());

        this.getLogger().info("Enabled DPS meter.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled DPS meter.");
    }
}
