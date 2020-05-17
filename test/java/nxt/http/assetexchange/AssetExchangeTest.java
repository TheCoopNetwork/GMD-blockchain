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

package nxt.http.assetexchange;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.HoldingType;
import nxt.Nxt;
import nxt.Tester;
import nxt.http.APICall;
import nxt.http.client.IssueAssetBuilder;
import nxt.http.client.IssueAssetBuilder.IssueAssetResult;
import nxt.http.client.TransferAssetBuilder;
import nxt.http.monetarysystem.TestCurrencyIssuance;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class AssetExchangeTest extends BlockchainTest {

    @Test
    public void exampleAsset() {
        IssueAssetResult issueAssetResult = issueAsset(ALICE, "ACME");

        JSONObject response = new APICall.Builder("getAsset")
                .param("asset", issueAssetResult.getAssetIdString())
                .build()
                .invokeNoError();

        Assert.assertEquals("ACME", response.get("name"));
        Assert.assertEquals("10000000", response.get("quantityQNT"));
        Assert.assertEquals(4L, response.get("decimals"));
        Assert.assertEquals(1000_0000, ALICE.getAssetQuantityDiff(issueAssetResult.getAssetId()));
    }

    @Test
    public void assetTransfer() {
        IssueAssetResult issueAssetResult = issueAsset(ALICE, "ACME");
        long assetId = issueAssetResult.getAssetId();
        String assetIdString = issueAssetResult.getAssetIdString();
        transfer(assetIdString, ALICE, BOB, 300 * 10000);

        generateBlock();

        Assert.assertEquals(700_0000, ALICE.getAssetQuantityDiff(assetId));
        Assert.assertEquals(300_0000, BOB.getAssetQuantityDiff(assetId));
    }

    @Test
    public void nxtDividend() {
        String assetId = issueAsset(ALICE, "ACME").getAssetIdString();
        transfer(assetId, ALICE, BOB, 300 * 10000);
        transfer(assetId, ALICE, CHUCK, 200 * 10000);
        transfer(assetId, ALICE, DAVE, 100 * 10000);
        generateBlock();
        payDividend(ALICE, assetId, HoldingType.NXT, "0", 10000);
        generateBlock();

        Assert.assertEquals(300 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(200 * Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        assertGetAssetDividends(assetId, "0", 3, HoldingType.NXT, 10000,
                (300 + 200 + 100) * Constants.ONE_NXT);
    }

    @Test
    public void assetDividend() {
        IssueAssetResult issueAssetResult = issueAsset(ALICE, "ACME");
        long assetId = issueAssetResult.getAssetId();
        String assetIdString = issueAssetResult.getAssetIdString();
        transfer(assetIdString, ALICE, BOB, 30 * 10000);
        transfer(assetIdString, ALICE, CHUCK, 20 * 10000);
        transfer(assetIdString, ALICE, DAVE, 10 * 10000);
        generateBlock();
        payDividend(ALICE, assetIdString, HoldingType.ASSET, assetIdString, 2);
        generateBlock();

        Assert.assertEquals((1000 - (30 + 20 + 10) * 3) * 10000, ALICE.getAssetQuantityDiff(assetId));
        Assert.assertEquals(30 * 3 * 10000, BOB.getAssetQuantityDiff(assetId));
        Assert.assertEquals(20 * 3 * 10000, CHUCK.getAssetQuantityDiff(assetId));
        Assert.assertEquals(10 * 3 * 10000, DAVE.getAssetQuantityDiff(assetId));
        assertGetAssetDividends(assetIdString, assetIdString, 3, HoldingType.ASSET, 2,
                (30 + 20 + 10) * 2 * 10000);
    }

    @Test
    public void anotherAssetDividend() {
        IssueAssetResult issueAssetResult = issueAsset(ALICE, "ACME");
        long assetId = issueAssetResult.getAssetId();
        String assetIdString = issueAssetResult.getAssetIdString();
        IssueAssetResult payAssetResult = issueAsset(ALICE, "PayAsset");
        String payAssetIdString = payAssetResult.getAssetIdString();
        long payAssetId = payAssetResult.getAssetId();

        transfer(assetIdString, ALICE, BOB, 30 * 10000);
        transfer(assetIdString, ALICE, CHUCK, 20 * 10000);
        transfer(assetIdString, ALICE, DAVE, 10 * 10000);
        generateBlock();
        payDividend(ALICE, assetIdString, HoldingType.ASSET, payAssetIdString, 2);
        generateBlock();

        Assert.assertEquals((1000 - (30 + 20 + 10)) * 10000, ALICE.getAssetQuantityDiff(assetId));
        Assert.assertEquals(30 * 10000, BOB.getAssetQuantityDiff(assetId));
        Assert.assertEquals(20 * 10000, CHUCK.getAssetQuantityDiff(assetId));
        Assert.assertEquals(10 * 10000, DAVE.getAssetQuantityDiff(assetId));

        Assert.assertEquals((1000 - (30 + 20 + 10) * 2) * 10000, ALICE.getAssetQuantityDiff(payAssetId));
        Assert.assertEquals(30 * 2 * 10000, BOB.getAssetQuantityDiff(payAssetId));
        Assert.assertEquals(20 * 2 * 10000, CHUCK.getAssetQuantityDiff(payAssetId));
        Assert.assertEquals(10 * 2 * 10000, DAVE.getAssetQuantityDiff(payAssetId));

        assertGetAssetDividends(assetIdString, payAssetIdString, 3, HoldingType.ASSET, 2,
                (30 + 20 + 10) * 2 * 10000);
    }

    @Test
    public void currencyDividend() {
        String assetId = issueAsset(ALICE, "ACME").getAssetIdString();
        transfer(assetId, ALICE, BOB, 300 * 10000);
        transfer(assetId, ALICE, CHUCK, 200 * 10000);
        transfer(assetId, ALICE, DAVE, 100 * 10000);
        generateBlock();

        TestCurrencyIssuance.Builder currencyBuilder = new TestCurrencyIssuance.Builder()
                .initialSupply(3000_0000)
                .maxSupply(3000_0000);
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(currencyBuilder.build());

        payDividend(ALICE, assetId, HoldingType.CURRENCY, currencyId, 3);
        generateBlock();

        long id = Long.parseUnsignedLong(currencyId);
        Assert.assertEquals(3000_0000 - (3 * (300 + 200 + 100) * 10000), ALICE.getCurrencyUnits(id));
        Assert.assertEquals(3 * 300 * 10000, BOB.getCurrencyUnits(id));
        Assert.assertEquals(3 * 200 * 10000, CHUCK.getCurrencyUnits(id));
        Assert.assertEquals(3 * 100 * 10000, DAVE.getCurrencyUnits(id));
        assertGetAssetDividends(assetId, currencyId, 3, HoldingType.CURRENCY, 3,
                3 * (300 + 200 + 100) * 10000);
    }

    @Test
    public void incorrectAsset() {
        JSONObject response = new APICall.Builder("dividendPayment")
                .param("secretPhrase", ALICE.getSecretPhrase())
                .param("asset", "0")
                .param("holdingType", HoldingType.NXT.getCode())
                .param("holding", "0")
                .param("amountNQTPerQNT", (long) 10000)
                .param("feeNQT", Constants.ONE_NXT)
                .param("height", Nxt.getBlockchain().getHeight())
                .build()
                .invoke();
        Assert.assertNotNull(response.get("errorCode"));
    }

    @Test
    public void zeroAmount() {
        IssueAssetResult issueAssetResult = issueAsset(ALICE, "ACME");
        long assetId = issueAssetResult.getAssetId();
        JSONObject response = new APICall.Builder("dividendPayment")
                .param("secretPhrase", ALICE.getSecretPhrase())
                .param("asset", Long.toUnsignedString(assetId))
                .param("holdingType", HoldingType.NXT.getCode())
                .param("holding", "0")
                .param("amountNQTPerQNT", 0L)
                .param("feeNQT", Constants.ONE_NXT)
                .param("height", Nxt.getBlockchain().getHeight())
                .build()
                .invoke();
        Assert.assertNotNull(response.get("errorCode"));
    }

    public static IssueAssetResult issueAsset(Tester creator, String name) {
        IssueAssetResult result = new IssueAssetBuilder(creator, name).issueAsset();

        generateBlock();

        JSONObject response = new APICall.Builder("getAsset")
                .param("asset", result.getAssetIdString())
                .build()
                .invokeNoError();
        Assert.assertEquals("error issuing test asset", name, response.get("name"));

        return result;
    }

    private static void transfer(String assetId, Tester from, Tester to, long quantityQNT) {
        new TransferAssetBuilder(assetId, from, to)
                .setQuantityQNT(quantityQNT)
                .transfer();
    }

    private static void payDividend(Tester payer, String assetId, HoldingType holdingType, String holdingId, long amountNQTPerQNT) {
        new APICall.Builder("dividendPayment")
                .param("secretPhrase", payer.getSecretPhrase())
                .param("asset", assetId)
                .param("holdingType", holdingType.getCode())
                .param("holding", holdingId)
                .param("amountNQTPerQNT", amountNQTPerQNT)
                .param("feeNQT", Constants.ONE_NXT)
                .param("height", Nxt.getBlockchain().getHeight())
                .build()
                .invokeNoError();
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertGetAssetDividends(String assetId, String holdingId, long numberOfAccounts,
                                                HoldingType holdingType, long amountNQTPerQNT, long totalDividend) {
        JSONObject response = new APICall.Builder("getAssetDividends")
                .param("asset", assetId)
                .build()
                .invokeNoError();
        Assert.assertTrue(response.get("dividends") instanceof JSONArray);
        JSONArray dividends = (JSONArray) response.get("dividends");
        Assert.assertEquals(1, dividends.size());
        JSONObject dividend = (JSONObject) dividends.get(0);
        Assert.assertEquals(holdingId, dividend.get("holding"));
        Assert.assertEquals(numberOfAccounts, dividend.get("numberOfAccounts"));
        Assert.assertEquals((long) holdingType.getCode(), dividend.get("holdingType"));
        Assert.assertEquals(String.valueOf(amountNQTPerQNT), dividend.get("amountNQTPerQNT"));
        Assert.assertEquals(String.valueOf(totalDividend), dividend.get("totalDividend"));
        Assert.assertEquals(assetId, dividend.get("asset"));
    }
}
