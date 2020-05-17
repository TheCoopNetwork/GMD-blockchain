/*
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

package nxt.addons;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.http.APICall;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Ignore
public class JPLSnapshotTest extends BlockchainTest {
    /**
     * This value came from genesis block.
     */
    private static final String aliceId = "9230759115816986914";
    /**
     * This value came from genesis block.
     */
    private static final long aliceCurrentBalance = 5033128 * Constants.ONE_NXT;

    private static final String INPUT_JSON_STR =
            "{\n" +
            "    \"balances\": {\n" +
            "        \"NXT-NZKH-MZRE-2CTT-98NPZ\": 30000000000000000,\n" +
            "        \"NXT-X5JH-TJKJ-DVGC-5T2V8\": 30000000000000000,\n" +
            "        \"NXT-LTR8-GMHB-YG56-4NWSE\": 30000000000000000\n" +
            "    },\n" +
            "    \"publicKeys\": [\n" +
            "        \"bf0ced0472d8ba3df9e21808e98e61b34404aad737e2bae1778cebc698b40f37\",\n" +
            "        \"39dc2e813bb45ff063a376e316b10cd0addd7306555ca0dd2890194d37960152\",\n" +
            "        \"011889a0988ccbed7f488878c62c020587de23ebbbae9ba56dd67fd9f432f808\"\n" +
            "    ]\n" +
            "}\n";

    @Before
    public void setUpTest() {
        generateBlock();
    }

    @Test
    public void testSnapshotWithoutInput() {
        JSONObject response = new APICall.Builder("downloadJPLSnapshot").
                param("height", getHeight()).
                build().invokeNoError();
        JSONObject balances = (JSONObject)response.get("balances");
        long total = 0;
        long aliceSnapshotBalance = 0;
        for (Map.Entry<String, Long> entry : ((Map<String, Long>)balances).entrySet()) {
            total += entry.getValue();
            if (entry.getKey().equals(aliceId)) {
                aliceSnapshotBalance = entry.getValue();
            }
        }
        Assert.assertEquals(aliceCurrentBalance, aliceSnapshotBalance); // 5033128 comes from genesis block
        Assert.assertTrue(total > Constants.MAX_BALANCE_NQT - 100000 * Constants.ONE_NXT); // some funds were sent to genesis or are locked in claimable currency
    }

    @Test
    public void testSnapshotWithInput() {
        JSONObject response = new APICall.Builder("downloadJPLSnapshot").
            param("height", getHeight()).
            parts("newGenesisAccounts", INPUT_JSON_STR.getBytes()).
            build().invokeNoError();
        JSONObject balances = (JSONObject)response.get("balances");
        long total = 0;
        long aliceSnapshotBalance = 0;
        for (Map.Entry<String, Long> entry : ((Map<String, Long>)balances).entrySet()) {
            total += entry.getValue();
            if (entry.getKey().equals(aliceId)) {
                aliceSnapshotBalance = entry.getValue();
            }
        }
        Assert.assertTrue(Constants.MAX_BALANCE_NQT - total < 10000);
        Assert.assertEquals(aliceCurrentBalance / 10, aliceSnapshotBalance);
        JSONObject inputGenesis = (JSONObject)JSONValue.parse(INPUT_JSON_STR);
        JSONObject inputBalances = (JSONObject) inputGenesis.get("balances");
        for (Map.Entry<String, Long> entry : ((Map<String, Long>)inputBalances).entrySet()) {
            long newBalance = (long)(Long)balances.get(Long.toUnsignedString(Convert.parseAccountId(entry.getKey())));
            if (entry.getValue() != newBalance) {
                Assert.fail("Balances differ for key " + entry.getKey());
            }
        }
        JSONArray publicKeys = (JSONArray)response.get("publicKeys");
        Set<String> publicKeysSet = new HashSet<>();
        for (Object publicKey : publicKeys) {
            publicKeysSet.add((String) publicKey);
        }
        JSONArray inputPublicKeys = (JSONArray)inputGenesis.get("publicKeys");
        Set<String> inputPublicKeysSet = new HashSet<>();
        for (Object inputPublicKey : inputPublicKeys) {
            inputPublicKeysSet.add((String) inputPublicKey);
        }
        publicKeysSet.retainAll(inputPublicKeysSet);
        Assert.assertEquals(inputPublicKeysSet.size(), publicKeysSet.size());
    }
}
