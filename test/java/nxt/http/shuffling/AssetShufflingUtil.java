/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http.shuffling;

import nxt.Constants;
import nxt.HoldingType;
import nxt.Tester;
import nxt.http.APICall;
import nxt.http.callers.ShufflingCreateCall;
import nxt.util.Logger;
import org.json.simple.JSONObject;

import static nxt.BlockchainTest.generateBlock;

class AssetShufflingUtil {
    static final long defaultHoldingShufflingAmount = 40000;

    private final long shufflingAsset;
    private final Tester assetCreator;

    JSONObject createAssetShuffling(Tester creator) {
        APICall apiCall = ShufflingCreateCall.create().
                secretPhrase(creator.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                amount(String.valueOf(defaultHoldingShufflingAmount)).
                participantCount("4").
                registrationPeriod("10").
                holding(Long.toUnsignedString(shufflingAsset)).
                holdingType(HoldingType.ASSET.getCode()).
                build();
        JSONObject response = apiCall.invokeNoError();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    private AssetShufflingUtil(long shufflingAsset, Tester assetCreator) {
        this.shufflingAsset = shufflingAsset;
        this.assetCreator = assetCreator;
    }

    static AssetShufflingUtil createShufflingUtil(Tester tester) {
        return new AssetShufflingUtil(issueAsset(tester), tester);
    }

    private static long issueAsset(Tester tester) {
        JSONObject issueAssetResult = new APICall.Builder("issueAsset").
                param("secretPhrase", tester.getSecretPhrase()).
                param("name", "AliceAsset").
                param("description", "AliceAssetDescription").
                param("quantityQNT", defaultHoldingShufflingAmount * 10).
                param("decimals", 0).
                param("feeNQT", 0).
                build().invokeNoError();
        generateBlock();
        return Long.parseUnsignedLong((String) issueAssetResult.get("transaction"));
    }

    long getShufflingAsset() {
        return shufflingAsset;
    }

    void sendAssetsTo(Tester... testers) {
        for (Tester tester : testers) {
            new APICall.Builder("transferAsset").
                    secretPhrase(assetCreator.getSecretPhrase()).
                    param("recipient", tester.getStrId()).
                    param("asset", Long.toUnsignedString(shufflingAsset)).
                    param("quantityQNT", defaultHoldingShufflingAmount).
                    param("feeNQT", 0).
                    build().invokeNoError();
        }
        generateBlock();
    }
}
