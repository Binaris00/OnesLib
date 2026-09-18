package com.binaris.oneslib.test;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.test.ability.DragonBeam;
import com.binaris.oneslib.test.ability.DragonFireball;
import com.binaris.oneslib.test.ability.EvilHulkGolpe;
import com.binaris.oneslib.test.ability.EvilHulkGrito;
import com.binaris.oneslib.test.ability.ForeignPunch;
import com.binaris.oneslib.test.ability.GiantOgreGrab;
import com.binaris.oneslib.test.ability.ImpAttack;
import com.binaris.oneslib.test.ability.SirenHeadLaser;
import com.binaris.oneslib.test.ability.TailsFly;
import com.binaris.oneslib.test.ability.TailsPassive;
import com.binaris.oneslib.test.entity.DragonOne;
import com.binaris.oneslib.test.entity.EvilHulkOne;
import com.binaris.oneslib.test.entity.ForeignOne;
import com.binaris.oneslib.test.entity.GiantOgreOne;
import com.binaris.oneslib.test.entity.ImpOne;
import com.binaris.oneslib.test.entity.SirenHeadOne;
import com.binaris.oneslib.test.entity.TailsOne;

public final class TestOnes {

    private TestOnes() {
    }

    public static void register() {
        Ones.register("tails", one -> one
                .entity(TailsOne::new)
                .hitbox(0.6F, 1.8F)
                .assets(TestMod.id("tails"))
                .attributes(attributes -> attributes
                        .health(40.0F)
                        .speed(0.25F)
                        .damage(6.0F)
                        .flySeconds(10))
                .ability(new TailsPassive())
                .ability(new TailsFly())
                .build());

        Ones.register("evil_hulk", one -> one
                .entity(EvilHulkOne::new)
                .hitbox(0.8F, 2.2F)
                .assets(TestMod.id("evil_hulk"))
                .attributes(attributes -> attributes
                        .health(120.0F)
                        .speed(0.24F)
                        .damage(12.0F)
                        .knockback(1.5F))
                .ability(new EvilHulkGolpe())
                .ability(new EvilHulkGrito())
                .build());

        Ones.register("siren_head", one -> one
                .entity(SirenHeadOne::new)
                .hitbox(0.7F, 2.6F)
                .assets(TestMod.id("siren_head"))
                .attributes(attributes -> attributes
                        .health(100.0F)
                        .speed(0.22F)
                        .damage(10.0F)
                        .ignoreFallDamage(true))
                .ability(new SirenHeadLaser())
                .build());

        Ones.register("dragon", one -> one
                .entity(DragonOne::new)
                .hitbox(1.2F, 1.8F)
                .assets(TestMod.id("dragon"))
                .attributes(attributes -> attributes
                        .health(80.0F)
                        .speed(0.28F)
                        .damage(8.0F)
                        .flySeconds(-1))
                .ability(new DragonBeam())
                .ability(new DragonFireball())
                .build());

        Ones.register("giant_ogre", one -> one
                .entity(GiantOgreOne::new)
                .hitbox(1.4F, 3.0F)
                .assets(TestMod.id("giant_ogre"))
                .attributes(attributes -> attributes
                        .health(150.0F)
                        .speed(0.2F)
                        .damage(14.0F)
                        .knockback(2.0F))
                .ability(new GiantOgreGrab())
                .build());

        Ones.register("imp", one -> one
                .entity(ImpOne::new)
                .hitbox(0.6F, 1.2F)
                .assets(TestMod.id("imp"))
                .attributes(attributes -> attributes
                        .health(30.0F)
                        .speed(0.3F)
                        .damage(4.0F))
                .ability(new ImpAttack())
                .build());

        Ones.register("foreign", one -> one
                .entity(ForeignOne::new)
                .hitbox(0.6F, 1.8F)
                .assets(TestMod.id("foreign"))
                .attributes(attributes -> attributes
                        .health(30.0F)
                        .speed(0.2F))
                .ability(new ForeignPunch())
                .build());
    }
}
