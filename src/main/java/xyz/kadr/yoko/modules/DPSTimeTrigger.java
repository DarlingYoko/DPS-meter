package xyz.kadr.yoko.modules;

import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class DPSTimeTrigger extends ChallengeTrigger {
    public DPSTimeTrigger() {
    }

    public void onBegin(WorldChallenge challenge) {
        Scene scene = challenge.getScene();
        scene.broadcastPacket(new PacketChallengeDataNotify(challenge, 2, challenge.getTimeLimit() + scene.getSceneTimeSeconds()));
    }

    public void onCheckTimeout(WorldChallenge challenge) {
        DungeonManager dungeonManager = challenge.getScene().getDungeonManager();
        if (dungeonManager == null || !dungeonManager.isTowerDungeon()) {
            int current = challenge.getScene().getSceneTimeSeconds();
            if ((long)current - challenge.getStartedAt() > (long)challenge.getTimeLimit()) {
                challenge.done();
            }

        }
    }
}
