package com.binaris.oneslib.common;

import java.util.ArrayList;
import java.util.List;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.common.state.StateChannel;

import net.minecraft.resources.ResourceLocation;

public final class OnesVerification {

    public record Report(List<String> problems, int ones, int abilities) {

        public boolean clean() {
            return this.problems.isEmpty();
        }
    }

    private OnesVerification() {
    }

    public static Report verify() {
        List<String> problems = new ArrayList<>();
        int abilities = 0;

        for (One one : Ones.registry().all()) {
            if (one.entityType() == null) {
                problems.add("One '" + one.id() + "' has no registered entity type");
            }
            if (one.stateChannel() == StateChannel.PACKET) {
                problems.add("One '" + one.id() + "' uses the PACKET state channel, which is not implemented yet");
            }
            checkAssets(one, problems);
            for (OneAbility ability : one.abilities()) {
                abilities++;
                if (ability.id().isBlank()) {
                    problems.add("One '" + one.id() + "' has an ability with a blank id");
                }
            }
        }

        return new Report(List.copyOf(problems), Ones.registry().all().size(), abilities);
    }

    private static void checkAssets(One one, List<String> problems) {
        ResourceLocation asset = one.asset();
        String base = "assets/" + asset.getNamespace() + "/";
        String path = asset.getPath();
        String[] required = {
                base + "geo/entity/" + path + ".geo.json",
                base + "textures/entity/" + path + ".png",
                base + "animations/entity/" + path + ".animation.json"
        };

        ClassLoader loader = OnesVerification.class.getClassLoader();
        for (String resource : required) {
            if (loader.getResource(resource) == null) {
                problems.add("One '" + one.id() + "' is missing asset '" + resource + "'");
            }
        }
    }
}
