package xyz.kadr.yoko;

import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.props.EntityType;
import emu.grasscutter.server.event.entity.EntityDeathEvent;

public final class EventListener {
    public static void EntityDeathEvent(EntityDeathEvent event) {
        GameEntity monster = event.getEntity();
        var entType = event.getEntity().getEntityType();
        if (EntityType.Monster.getValue() == entType.getValue()){
            DPSCommand.DPSChallenge.onMonsterDeath((EntityMonster) monster);
        }

    }
}
