package net.axiomainteractive.tranquility.entity.client


import net.minecraft.client.render.entity.animation.*


object CrimsonSlimeAnimations {
    val SQUISH: AnimationDefinition = AnimationDefinition.Builder.create(1f).looping()
        .addBoneAnimation(
            "main_body",
            Transformation(
                Transformation.Targets.SCALE,
                Keyframe(
                    0f, AnimationHelper.createScalingVector(1.0, 1.0, 1.0),
                    Transformation.Interpolations.LINEAR
                ),
                Keyframe(
                    0.5f, AnimationHelper.createScalingVector(1.1, 0.8, 1.1),
                    Transformation.Interpolations.LINEAR
                ),
                Keyframe(
                    1f, AnimationHelper.createScalingVector(1.0, 1.0, 1.0),
                    Transformation.Interpolations.LINEAR
                )
            )
        ).build()
}