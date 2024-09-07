package xyz.kadr.yoko.modules;

import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class DPSChallenge extends WorldChallenge {

    private final Player targetPlayer;
	private final List<DPSEntity> activeMonsters;
    private final ScheduledExecutorService executor;
    
    public Player getTargetPlayer() {
		return targetPlayer;
	}

	public List<DPSEntity> getActiveMonsters() {
		return activeMonsters;
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}


    public DPSChallenge(
        Scene scene,
        SceneGroup group,
        int challengeId,
        int challengeIndex,
        List<Integer> paramList,
        int timeLimit, int goal,
        List<ChallengeTrigger> challengeTriggers,
        Player targetPlayer,
        List<DPSEntity> activeMonsters,
        ScheduledExecutorService executor
    ) {
        super(scene, group, challengeId, challengeIndex, paramList, timeLimit, goal, challengeTriggers);
        this.targetPlayer = targetPlayer;
        this.activeMonsters = activeMonsters;
        this.executor = executor;
    }

    public void done() {
        super.done();
        this.executor.shutdown();

        // Counting all damage
        float dmg = 0;
        for (DPSEntity entity : this.activeMonsters) {
            dmg += entity.dmg;
            entity.getScene().killEntity(entity, 0);
        }
        this.activeMonsters.clear();
        String message = String.format(
            "DPS check finished! Your result:\nTotal time - %d sec\nTotal DMG - %.0f\nAverage DPS - %.2f",
            getFinishedTime(), dmg, dmg/getFinishedTime()
        );
        CommandHandler.sendMessage(targetPlayer, message);
    }


}
