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
import nxt.Tester;
import nxt.http.APICall;
import nxt.http.callers.ShufflingCancelCall;
import nxt.http.callers.ShufflingCreateCall;
import nxt.http.callers.ShufflingProcessCall;
import nxt.http.callers.ShufflingRegisterCall;
import nxt.http.callers.ShufflingVerifyCall;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONObject;

class ShufflingUtil {

    static final Tester ALICE_RECIPIENT = new Tester("oiketrdgfxyjqhwds");
    static final Tester BOB_RECIPIENT = new Tester("5ehtrd9oijnkter");
    static final Tester CHUCK_RECIPIENT = new Tester("sdfxbejytdgfqrwefsrd");
    static final Tester DAVE_RECIPIENT = new Tester("gh-=e49rsiufzn4^");

    static final long defaultShufflingAmount = 1500000000;
    static final long defaultHoldingShufflingAmount = 40000;

    static JSONObject create(Tester creator) {
        return create(creator, 4);
    }

    static JSONObject create(Tester creator, int participantCount) {
        APICall apiCall = ShufflingCreateCall.create().
                secretPhrase(creator.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                param("amount", String.valueOf(defaultShufflingAmount)).
                param("participantCount", String.valueOf(participantCount)).
                param("registrationPeriod", 10).
                build();
        JSONObject response = apiCall.invokeNoError();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject register(String shufflingFullHash, Tester tester) {
        APICall apiCall = ShufflingRegisterCall.create().
                secretPhrase(tester.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject response = apiCall.invokeNoError();
        Logger.logMessage("shufflingRegisterResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject getShuffling(String shufflingId) {
        APICall apiCall = new APICall.Builder("getShuffling").
                param("shuffling", shufflingId).
                build();
        JSONObject getShufflingResponse = apiCall.invokeNoError();
        Logger.logMessage("getShufflingResponse: " + getShufflingResponse.toJSONString());
        return getShufflingResponse;
    }

    static JSONObject getShufflingParticipants(String shufflingId) {
        APICall apiCall = new APICall.Builder("getShufflingParticipants").
                param("shuffling", shufflingId).
                build();
        JSONObject getParticipantsResponse = apiCall.invokeNoError();
        Logger.logMessage("getShufflingParticipantsResponse: " + getParticipantsResponse.toJSONString());
        return getParticipantsResponse;
    }

    static JSONObject process(String shufflingId, Tester tester, Tester recipient) {
        return process(shufflingId, tester, recipient, true);
    }

    static JSONObject process(String shufflingId, Tester tester, Tester recipient, boolean broadcast) {
        APICall.Builder builder = ShufflingProcessCall.create().
                shuffling(shufflingId).
                secretPhrase(tester.getSecretPhrase()).
                recipientSecretPhrase(recipient.getSecretPhrase()).
                feeNQT(0);
        if (!broadcast) {
            builder.param("broadcast", "false");
        }
        APICall apiCall = builder.build();
        JSONObject shufflingProcessResponse = apiCall.invokeNoError();
        Logger.logMessage("shufflingProcessResponse: " + shufflingProcessResponse.toJSONString());
        return shufflingProcessResponse;
    }

    static JSONObject verify(String shufflingId, Tester tester, String shufflingStateHash) {
        APICall apiCall = ShufflingVerifyCall.create().
                shuffling(shufflingId).
                secretPhrase(tester.getSecretPhrase()).
                shufflingStateHash(shufflingStateHash).
                feeNQT(Constants.ONE_NXT).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logDebugMessage("shufflingVerifyResponse:" + response);
        return response;
    }

    static JSONObject cancel(String shufflingId, Tester tester, String shufflingStateHash, long cancellingAccountId) {
        return cancel(shufflingId, tester, shufflingStateHash, cancellingAccountId, true);
    }

    static JSONObject cancel(String shufflingId, Tester tester, String shufflingStateHash, long cancellingAccountId, boolean broadcast) {
        APICall.Builder builder = ShufflingCancelCall.create().
                shuffling(shufflingId).
                secretPhrase(tester.getSecretPhrase()).
                shufflingStateHash(shufflingStateHash).
                feeNQT(10 * Constants.ONE_NXT);
        if (cancellingAccountId != 0) {
            builder.param("cancellingAccount", Long.toUnsignedString(cancellingAccountId));
        }
        if (!broadcast) {
            builder.param("broadcast", "false");
        }
        APICall apiCall = builder.build();
        JSONObject response = apiCall.invoke();
        Logger.logDebugMessage("shufflingCancelResponse:" + response);
        return response;
    }

    static JSONObject broadcast(JSONObject transaction, Tester tester) {
        transaction.remove("signature");
        APICall apiCall = new APICall.Builder("signTransaction")
                .param("unsignedTransactionJSON", transaction.toJSONString())
                .param("validate", "false")
                .param("secretPhrase", tester.getSecretPhrase())
                .build();
        JSONObject response = apiCall.invoke();
        if (response.get("transactionJSON") == null) {
            return response;
        }
        apiCall = new APICall.Builder("broadcastTransaction").
                param("transactionJSON", ((JSONObject)response.get("transactionJSON")).toJSONString()).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("broadcastTransactionResponse:" + response);
        return response;
    }

    static JSONObject startShuffler(Tester tester, Tester recipient, String shufflingFullHash) {
        APICall apiCall = startShufflerBuilder(tester, recipient, shufflingFullHash);
        JSONObject response = apiCall.invokeNoError();
        Logger.logMessage("startShufflerResponse: " + response.toJSONString());
        return response;
    }

    static APICall startShufflerBuilder(Tester tester, Tester recipient, String shufflingFullHash) {
        return new APICall.Builder("startShuffler").
                    secretPhrase(tester.getSecretPhrase()).
                    param("recipientPublicKey", Convert.toHexString(recipient.getPublicKey())).
                    param("shufflingFullHash", shufflingFullHash).
                    build();
    }

    static JSONObject stopShuffler(Tester tester, String shufflingFullHash) {
        APICall apiCall = new APICall.Builder("stopShuffler").
                secretPhrase(tester.getSecretPhrase()).
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("stopShufflerResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject sendMoney(Tester sender, Tester recipient, long amountNXT) {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", sender.getSecretPhrase()).
                param("recipient", recipient.getStrId()).
                param("amountNQT", amountNXT * Constants.ONE_NXT).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logMessage("sendMoneyReponse: " + response.toJSONString());
        return response;
    }
}
