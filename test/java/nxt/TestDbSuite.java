/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt;

import nxt.addons.AddonsSuite;
import nxt.http.PaymentAndMessagesSuite;
import nxt.http.accountControl.AccountControlSuite;
import nxt.http.accountproperties.AccountPropertiesSuite;
import nxt.http.alias.CreateAliasTest;
import nxt.http.assetexchange.AssetExchangeSuite;
import nxt.http.monetarysystem.CurrencySuite;
import nxt.http.shuffling.ShufflingSuite;
import nxt.http.twophased.TwoPhasedSuite;
import nxt.http.votingsystem.VotingSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TokenTest.class,
        GeneratorTest.class,
        CurrencySuite.class,
        PaymentAndMessagesSuite.class,
        VotingSuite.class,
        TwoPhasedSuite.class,
        ShufflingSuite.class,
        AccountControlSuite.class,
        AccountPropertiesSuite.class,
        AssetExchangeSuite.class,
        CreateAliasTest.class,
        AddonsSuite.class
})
public class TestDbSuite extends SafeShutdownSuite {
}
