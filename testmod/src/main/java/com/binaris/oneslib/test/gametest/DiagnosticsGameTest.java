package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.common.OnesVerification;
import com.binaris.oneslib.test.TestMod;
import com.binaris.oneslib.test.util.TestPlayers;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DiagnosticsGameTest {

    private DiagnosticsGameTest() {
    }

    @GameTest(template = "empty")
    public static void verify_isClean(GameTestHelper helper) {
        OnesVerification.Report report = OnesVerification.verify();
        helper.assertTrue(report.clean(), "verify should be clean but found: " + report.problems());
        helper.assertTrue(report.ones() >= 5, "all test Ones should be registered but found " + report.ones());
        helper.succeed();
    }
}
