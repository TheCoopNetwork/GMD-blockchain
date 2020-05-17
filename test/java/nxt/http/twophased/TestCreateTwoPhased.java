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

package nxt.http.twophased;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.Nxt;
import nxt.VoteWeighting;
import nxt.http.APICall;
import nxt.http.callers.SendMoneyCall;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNull;


public class TestCreateTwoPhased extends BlockchainTest {

    private static JSONObject issueCreateTwoPhasedFailed(APICall apiCall) {
        JSONObject twoPhased = apiCall.invoke();
        Logger.logMessage("two-phased sendMoney: " + twoPhased.toJSONString());

        generateBlock();
        assertNull(twoPhased.get("transaction"));
        return twoPhased;
    }

    static JSONObject issueCreateTwoPhasedSuccess(APICall apiCall) {
        JSONObject twoPhased = apiCall.invokeNoError();
        generateBlock();
        return twoPhased;
    }

    public static class TwoPhasedMoneyTransferBuilder {

        private final SendMoneyCall builder = SendMoneyCall.create();

        public TwoPhasedMoneyTransferBuilder() {

            int height = Nxt.getBlockchain().getHeight();

            builder.secretPhrase(ALICE.getSecretPhrase());
            builder.feeNQT(2*Constants.ONE_NXT);
            builder.recipient(BOB.getId());
            builder.param("amountNQT", 50 * Constants.ONE_NXT);
            builder.param("phased", "true");
            builder.param("phasingVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode());
            builder.param("phasingQuorum", 1);
            builder.param("phasingWhitelisted", CHUCK.getStrId());
            builder.param("phasingFinishHeight", height + 50);
        }

        public TwoPhasedMoneyTransferBuilder fee(long fee) {
            builder.feeNQT(fee);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder votingModel(byte model) {
            builder.param("phasingVotingModel", model);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder finishHeight(int maxHeight) {
            builder.param("phasingFinishHeight", maxHeight);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder minBalance(long minBalance, byte minBalanceModel) {
            builder.param("phasingMinBalance", minBalance);
            builder.param("phasingMinBalanceModel", minBalanceModel);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder quorum(int quorum) {
            builder.param("phasingQuorum", quorum);
            return this;
        }

        public TwoPhasedMoneyTransferBuilder noWhitelist() {
            builder.param("phasingWhitelisted", "");
            return this;
        }

        public TwoPhasedMoneyTransferBuilder whitelisted(long accountId) {
            builder.param("phasingWhitelisted", Long.toUnsignedString(accountId));
            return this;
        }

        public TwoPhasedMoneyTransferBuilder holding(long accountId) {
            builder.param("phasingHolding", Long.toUnsignedString(accountId));
            return this;
        }

        public APICall build() {
            return builder.build();
        }
    }


    @Test
    public void validMoneyTransfer() {
        APICall apiCall = new TwoPhasedMoneyTransferBuilder().build();
        issueCreateTwoPhasedSuccess(apiCall);
    }

    @Test
    public void invalidMoneyTransfer() {
        int height = Nxt.getBlockchain().getHeight();

        APICall apiCall = new TwoPhasedMoneyTransferBuilder().finishHeight(height).build();
        issueCreateTwoPhasedFailed(apiCall);

        apiCall = new TwoPhasedMoneyTransferBuilder().finishHeight(height + 100000).build();
        issueCreateTwoPhasedFailed(apiCall);

        apiCall = new TwoPhasedMoneyTransferBuilder().quorum(0).build();
        issueCreateTwoPhasedFailed(apiCall);

        apiCall = new TwoPhasedMoneyTransferBuilder().noWhitelist().build();
        issueCreateTwoPhasedFailed(apiCall);

        apiCall = new TwoPhasedMoneyTransferBuilder().whitelisted(0).build();
        issueCreateTwoPhasedFailed(apiCall);

        apiCall = new TwoPhasedMoneyTransferBuilder().votingModel(VoteWeighting.VotingModel.ASSET.getCode()).build();
        issueCreateTwoPhasedFailed(apiCall);

        apiCall = new TwoPhasedMoneyTransferBuilder().votingModel(VoteWeighting.VotingModel.ASSET.getCode())
                .minBalance(50, VoteWeighting.MinBalanceModel.ASSET.getCode())
                .build();
        issueCreateTwoPhasedFailed(apiCall);
    }

    @Test
    public void unconfirmed() {
        List<String> transactionIds = new ArrayList<>(10);

        for(int i=0; i < 10; i++){
            APICall apiCall = new TwoPhasedMoneyTransferBuilder().build();
            JSONObject transactionJSON = issueCreateTwoPhasedSuccess(apiCall);
            String idString = (String) transactionJSON.get("transaction");
            transactionIds.add(idString);
        }

        APICall apiCall = new TwoPhasedMoneyTransferBuilder().build();
        apiCall.invoke();

        JSONObject response = TestGetAccountPhasedTransactions.phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");

        for(String idString:transactionIds){
            Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, idString));
        }
    }
}