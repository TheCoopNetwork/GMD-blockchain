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

package nxt.http.votingsystem;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.Nxt;
import nxt.VoteWeighting;
import nxt.http.APICall;
import nxt.http.callers.CreatePollCall;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class TestCreatePoll extends BlockchainTest {

    static String issueCreatePoll(APICall apiCall, boolean shouldFail) {
        JSONObject createPollResponse = apiCall.invoke();
        Logger.logMessage("createPollResponse: " + createPollResponse.toJSONString());

        if (!shouldFail) {
            Assert.assertNull(createPollResponse.get("errorCode"));
        }

        generateBlock();

        try {
            String pollId = (String) createPollResponse.get("transaction");

            assertFalse(!shouldFail && pollId == null);

            apiCall = new APICall.Builder("getPoll").param("poll", pollId).build();

            JSONObject getPollResponse = apiCall.invoke();
            Logger.logMessage("getPollResponse:" + getPollResponse.toJSONString());
            Assert.assertEquals(pollId, getPollResponse.get("poll"));
            return pollId;
        } catch (Throwable t) {
            if (!shouldFail) Assert.fail(t.getMessage());
            return null;
        }
    }

    @Test
    public void createValidPoll() {
        APICall apiCall = new CreatePollBuilder().build();
        issueCreatePoll(apiCall, false);
        generateBlock();

        apiCall = new CreatePollBuilder().votingModel(VoteWeighting.VotingModel.NQT.getCode()).build();
        issueCreatePoll(apiCall, false);
        generateBlock();
    }

    @Test
    public void createInvalidPoll() {
        APICall apiCall = new CreatePollBuilder().minBalance(-Constants.ONE_NXT).build();
        issueCreatePoll(apiCall, true);
        generateBlock();

        apiCall = new CreatePollBuilder().minBalance(0).build();
        issueCreatePoll(apiCall, true);
        generateBlock();
    }

    public static class CreatePollBuilder {
        private final CreatePollCall builder = CreatePollCall.create();

        public CreatePollBuilder() {
            builder.secretPhrase(ALICE.getSecretPhrase());
            builder.feeNQT(10 * Constants.ONE_NXT);
            builder.name("Test1");
            builder.description("The most cool Beatles guy?");
            builder.finishHeight(Nxt.getBlockchain().getHeight() + 100);
            builder.votingModel(VoteWeighting.VotingModel.ACCOUNT.getCode());
            builder.minNumberOfOptions(1);
            builder.maxNumberOfOptions(2);
            builder.minRangeValue(0);
            builder.maxRangeValue(1);
            builder.minBalance(10 * Constants.ONE_NXT);
            builder.minBalanceModel(VoteWeighting.MinBalanceModel.NQT.getCode());
            builder.param("option00", "Ringo");
            builder.param("option01", "Paul");
            builder.param("option02", "John");
        }

        public CreatePollBuilder votingModel(byte votingModel) {
            builder.votingModel(votingModel);
            return this;
        }

        public CreatePollBuilder minBalance(long minBalance) {
            builder.minBalance(minBalance);
            return this;
        }

        public CreatePollBuilder minBalance(long minBalance, byte minBalanceModel) {
            builder.minBalance(minBalance);
            builder.minBalanceModel(minBalanceModel);
            return this;
        }

        public CreatePollBuilder secretPhrase(String s) {
            builder.secretPhrase(s);
            return this;
        }

        public CreatePollBuilder minBalance(long minBalance, byte minBalanceModel, long holdingId) {
            builder.minBalance(minBalance);
            builder.minBalanceModel(minBalanceModel);
            builder.param("holdingId", holdingId);
            return this;
        }

        public APICall build() {
            return builder.build();
        }
    }
}
