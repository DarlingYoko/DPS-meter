package xyz.kadr.yoko.modules;


import emu.grasscutter.data.excels.monster.MonsterData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ElementType;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.props.PlayerProperty;
import emu.grasscutter.game.world.Position;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.net.proto.*;
import emu.grasscutter.scripts.data.SceneMonster;
import emu.grasscutter.utils.helpers.ProtoHelper;

import javax.annotation.Nullable;


public class DPSEntity extends EntityMonster{

    @Nullable
    private SceneMonster metaMonster;
    public float dmg;
    public DPSEntity(Scene scene, MonsterData monsterData, Position pos, Position rot, int level) {
        super(scene, monsterData, pos, rot, level);
        this.dmg = 0;
    }

    public void damage(float amount, int killerId, ElementType attackType) {
        this.dmg += amount;
        
        //Scale Total Damage to HP bar
        
        super.damage(this.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP)-(float)(73060*Math.max(0,1-Math.log10(this.dmg/100+1)/8)+1), killerId, attackType);
    }

    public SceneEntityInfoOuterClass.SceneEntityInfo toProto() {
        MonsterData data = this.getMonsterData();
        EntityAuthorityInfoOuterClass.EntityAuthorityInfo authority = EntityAuthorityInfoOuterClass.EntityAuthorityInfo.newBuilder().setAbilityInfo(AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo.newBuilder()).setRendererChangedInfo(EntityRendererChangedInfoOuterClass.EntityRendererChangedInfo.newBuilder()).setAiInfo(SceneEntityAiInfoOuterClass.SceneEntityAiInfo.newBuilder().setIsAiOpen(true).setBornPos(this.getBornPos().toProto())).setBornPos(this.getBornPos().toProto()).build();
        SceneEntityInfoOuterClass.SceneEntityInfo.Builder entityInfo = SceneEntityInfoOuterClass.SceneEntityInfo.newBuilder().setEntityId(this.getId()).setEntityType(ProtEntityTypeOuterClass.ProtEntityType.PROT_ENTITY_TYPE_MONSTER).setMotionInfo(this.getMotionInfo()).addAnimatorParaList(AnimatorParameterValueInfoPairOuterClass.AnimatorParameterValueInfoPair.newBuilder()).setEntityClientData(EntityClientDataOuterClass.EntityClientData.newBuilder()).setEntityAuthorityInfo(authority).setLifeState(this.getLifeState().getValue());
        this.addAllFightPropsToEntityInfo(entityInfo);
        entityInfo.addPropList(PropPairOuterClass.PropPair.newBuilder().setType(PlayerProperty.PROP_LEVEL.getId()).setPropValue(ProtoHelper.newPropValue(PlayerProperty.PROP_LEVEL, this.getLevel())).build());
        SceneMonsterInfoOuterClass.SceneMonsterInfo.Builder monsterInfo = SceneMonsterInfoOuterClass.SceneMonsterInfo.newBuilder().setMonsterId(this.getMonsterId()).setGroupId(this.getGroupId()).setConfigId(this.getConfigId()).addAllAffixList(data.getAffix()).setAuthorityPeerId(this.getWorld().getHostPeerId()).setPoseId(this.getPoseId()).setBlockId(this.getScene().getId()).setBornType(MonsterBornTypeOuterClass.MonsterBornType.MONSTER_BORN_TYPE_DEFAULT);
        if (this.metaMonster != null) {
            if (this.metaMonster.special_name_id != 0) {
                monsterInfo.setTitleId(this.metaMonster.title_id).setSpecialNameId(this.metaMonster.special_name_id);
            } else if (data.getDescribeData() != null) {
                monsterInfo.setTitleId(data.getDescribeData().getTitleId()).setSpecialNameId(data.getSpecialNameId());
            }
        }

        monsterInfo.setAiConfigId(12001001);

        entityInfo.setMonster(monsterInfo);
        return entityInfo.build();
    }

    private int getMonsterId() {
        return this.getMonsterData().getId();
    }

}
