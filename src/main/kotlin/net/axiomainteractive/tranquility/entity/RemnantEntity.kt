package net.axiomainteractive.tranquility.entity

import net.axiomainteractive.tranquility.entity.ai.RemnantJumpAttackGoal
import net.axiomainteractive.tranquility.entity.ai.RemnantHealGoal
import net.axiomainteractive.tranquility.entity.ai.RemnantAttackGoal
import net.axiomainteractive.tranquility.entity.ai.RemnantCampfireGoal
import net.axiomainteractive.tranquility.entity.ai.RemnantMineGoal
import net.axiomainteractive.tranquility.entity.ai.RemnantPillarGoal
import net.axiomainteractive.tranquility.entity.ai.RemnantBridgeGoal
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.world.World

class RemnantEntity(entityType: EntityType<out HostileEntity>, world: World) : HostileEntity(entityType, world) {

    init {
        this.setPathfindingPenalty(net.minecraft.entity.ai.pathing.PathNodeType.DAMAGE_FIRE, -1.0f)
        this.setPathfindingPenalty(net.minecraft.entity.ai.pathing.PathNodeType.DANGER_FIRE, 16.0f)
    }

    override fun cannotDespawn(): Boolean {
        return true
    }

    var isMining: Boolean = false
    var isBuilding: Boolean = false
    var isCooking: Boolean = false
    var isChopping: Boolean = false

    enum class SquadRole { LEADER, BUILDER, GUARD }

    // Squad State
    var squadLeader: RemnantEntity? = null
    var isSquadLeader: Boolean = false
    var squadRole: SquadRole = SquadRole.GUARD
    var homePos: net.minecraft.util.math.BlockPos? = null
    var invasionPhase: String = "SCOUTING" 
    var campTicks: Int = 0

    companion object {
        fun createRemnantAttributes(): DefaultAttributeContainer.Builder {
            return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.STEP_HEIGHT, 1.0)
        }
    }
    
    override fun initGoals() {
        this.goalSelector.add(1, SwimGoal(this))
        this.goalSelector.add(1, net.axiomainteractive.tranquility.entity.ai.RemnantBuildGoal(this)) // Priority 1: BUILD (Override everything)
        this.goalSelector.add(2, RemnantHealGoal(this))
        this.goalSelector.add(2, net.axiomainteractive.tranquility.entity.ai.RemnantSquadGoal(this))
        this.goalSelector.add(3, RemnantJumpAttackGoal(this, 1.0, 10.0))
        this.goalSelector.add(4, RemnantPillarGoal(this))
        this.goalSelector.add(4, RemnantBridgeGoal(this))
        this.goalSelector.add(5, RemnantMineGoal(this))
        this.goalSelector.add(5, RemnantAttackGoal(this)) 
        this.goalSelector.add(6, RemnantCampfireGoal(this)) 
        this.goalSelector.add(7, WanderAroundFarGoal(this, 1.0))
        this.goalSelector.add(8, LookAroundGoal(this))
        this.goalSelector.add(9, LookAtEntityGoal(this, PlayerEntity::class.java, 8.0f))

        this.targetSelector.add(1, ActiveTargetGoal(this, PlayerEntity::class.java, false))
        this.targetSelector.add(2, RevengeGoal(this))
    }

    override fun initEquipment(random: net.minecraft.util.math.random.Random, difficulty: net.minecraft.world.LocalDifficulty) {
        super.initEquipment(random, difficulty)
        this.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_PICKAXE))
    }

    override fun tick() {
        super.tick()
        if (!world.isClient) {
            updateHandItem()
        }
    }

    private fun updateHandItem() {
        val target = this.target
        val mainHandStack = this.mainHandStack

        if (this.isUsingItem) return
        if (this.isMining) return // Allows holding Axe
        if (this.isBuilding) return
        if (this.isCooking) return
        if (this.isChopping) return // Explicitly allow chopping/burning item

        // Default Logic
        if (mainHandStack.item == Items.STONE_AXE) return

        if (target != null && target.isAlive) {
            // Default Aggro: Hold Sword
            if (mainHandStack.item != Items.STONE_SWORD) {
                 this.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_SWORD))
            }
        } else {
            // Idle: Stone Pickaxe
            if (mainHandStack.item != Items.STONE_PICKAXE) {
                this.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_PICKAXE))
            }
        }
    }
}
