package xyz.kadr.yoko;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.monster.MonsterData;
import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterTrigger;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.world.Position;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.proto.PropChangeReasonOuterClass;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.SceneMonster;
import static emu.grasscutter.command.CommandHelpers.parseIntParameters;

import emu.grasscutter.server.packet.send.PacketAvatarFightPropUpdateNotify;
import emu.grasscutter.server.packet.send.PacketAvatarLifeStateChangeNotify;
import lombok.Setter;
import xyz.kadr.yoko.modules.DPSChallenge;
import xyz.kadr.yoko.modules.DPSEntity;
import xyz.kadr.yoko.modules.DPSTimeTrigger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.function.BiConsumer;



@Command(
    label = "dps",
    usage = "start|stop \n /dps start x[count of mobs] s[time in sec]\n\nDefault is 1 mob and 60 sec",
    targetRequirement = Command.TargetRequirement.PLAYER
)
public final class DPSCommand implements CommandHandler {

    public static xyz.kadr.yoko.modules.DPSChallenge DPSChallenge;
    public static List<DPSEntity> activeMonsters = new ArrayList<>(); // Current mobs
    private final int blazingAxeMitachurlID = 21020201;
    public static SpawnParameters param = new SpawnParameters(); // Custom parameters

    // Patterns
    public static final Pattern countRegex = Pattern.compile("x(\\d+)");
    public static final Pattern timeRegex = Pattern.compile("s(\\d+)");

    // Taken from SpawnCommand.java with edits made to match 'create'
    private static final Map<Pattern, BiConsumer<SpawnParameters, Integer>> intCommandHandlers = Map.ofEntries(
            Map.entry(countRegex, SpawnParameters::setEntityCount),
            Map.entry(timeRegex, SpawnParameters::setTime)
    );

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {

        // Check if the command was executed by a player.
        if (sender == null) {
            CommandHandler.sendMessage(null, "This command can only be executed by a player.");
            return;
        }

        String command = args.isEmpty() ? "start" : args.get(0);
        param = new SpawnParameters();
        parseIntParameters(args, param, intCommandHandlers);

        // Stop challenge
        if (command.equals("stop")) {
            if (DPSChallenge.inProgress()) {
                DPSChallenge.done();
            }
        }
        // Start new challenge
        else if (command.equals("start")) {

            if(DPSChallenge != null && DPSChallenge.inProgress()){
                CommandHandler.sendMessage(targetPlayer,
                        "Another challenge is currently in progress, please wait for the other challenge to finish!");
                return;
            }

            // Basic variables
            Scene scene = targetPlayer.getScene();
            Position pos = targetPlayer.getPosition();
            Position rot = targetPlayer.getRotation();

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            ArrayList<ChallengeTrigger> cTrigger = new ArrayList<>(); // Challenge triggers
            MonsterData monsterData = GameData.getMonsterDataMap().get(blazingAxeMitachurlID);

            // Define meta monster
            SceneGroup mobSG = new SceneGroup(); // Mobs scene group
            Map<Integer, SceneMonster> metaMonsters = new HashMap<>(); // Meta monsters
            SceneMonster monster = new SceneMonster();
            metaMonsters.put(param.entityCount, monster);
            mobSG.setMonsters(metaMonsters);

            // Generate monsters
            for (int i = 0; i < param.entityCount; i++){

                // Define entity monster
                DPSEntity entity = new DPSEntity(scene, monsterData, pos.nearby2d(2f), rot, 90);
                entity.setConfigId(0);
                entity.setMetaMonster(monster);
                activeMonsters.add(entity);

                // Challenge trigger by kill
                KillMonsterTrigger killMob = new KillMonsterTrigger(0);
                cTrigger.add(killMob);
            }


            // Challenge trigger by time
            DPSTimeTrigger timeMob = new DPSTimeTrigger();
            cTrigger.add(timeMob);

            // Refresh chars buttons
            targetPlayer.getTeamManager().getActiveTeam().forEach((entity) -> {
                boolean isAlive = entity.isAlive();
                entity.addEnergy(100.0F, PropChangeReasonOuterClass.PropChangeReason.PROP_CHANGE_REASON_ENERGY_BALL);
                entity.setFightProperty(FightProperty.FIGHT_PROP_NONEXTRA_SKILL_CD_MINUS_RATIO, 0);
                entity.getWorld().broadcastPacket(new PacketAvatarFightPropUpdateNotify(entity.getAvatar(), FightProperty.FIGHT_PROP_CUR_HP));
                if (!isAlive) {
                    entity.getWorld().broadcastPacket(new PacketAvatarLifeStateChangeNotify(entity.getAvatar()));
                }
            });

            // Define and start challenge
            List<Integer> paramList = List.of(param.entityCount, param.time);
            DPSChallenge = new DPSChallenge(scene, mobSG, 180, 180, paramList, param.time, param.entityCount, cTrigger, targetPlayer, activeMonsters, executor);
            DPSChallenge.start();
            scene.setChallenge(DPSChallenge);

            // Add mobs to the scene
            for (DPSEntity entity : activeMonsters) {
                scene.addEntity(entity);
            }

            // Notify the player.
            CommandHandler.sendMessage(sender, "DPS check started");

            final float[] dmg = {0};
            // Repeat once per second
            executor.scheduleAtFixedRate(() -> {
                // Counting damage per second
                float newDmg = 0;
                for (DPSEntity entity : activeMonsters) {
                    newDmg += entity.dmg;
                }
                float dps = newDmg - dmg[0];
                dmg[0] = newDmg;
                String message = String.format("DPS - %.0f", dps);
                CommandHandler.sendMessage(targetPlayer, message);

            }, 0, 1, TimeUnit.SECONDS);
        }
    }
    private static class SpawnParameters {
        @Setter public int entityCount = 1;
        @Setter public int time = 60;
    }
}
