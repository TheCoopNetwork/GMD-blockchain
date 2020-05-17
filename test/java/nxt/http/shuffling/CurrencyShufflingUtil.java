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

package nxt.http.shuffling;

import nxt.Constants;
import nxt.HoldingType;
import nxt.Tester;
import nxt.http.APICall;
import nxt.http.callers.ShufflingCreateCall;
import nxt.http.callers.TransferCurrencyCall;
import nxt.http.monetarysystem.TestCurrencyIssuance;
import nxt.util.Logger;
import org.json.simple.JSONObject;

import java.util.stream.Stream;

import static nxt.http.monetarysystem.TestCurrencyIssuance.issueCurrencyApi;
import static nxt.http.shuffling.ShufflingUtil.defaultHoldingShufflingAmount;
import static nxt.http.shuffling.ShufflingUtil.defaultShufflingAmount;

class CurrencyShufflingUtil {
    private final long shufflingCurrency;
    private final Tester currencyIssuer;

    private CurrencyShufflingUtil(long shufflingCurrency, Tester currencyIssuer) {
        this.shufflingCurrency = shufflingCurrency;
        this.currencyIssuer = currencyIssuer;
    }

    static CurrencyShufflingUtil createShufflingUtil(Tester tester) {
        return new CurrencyShufflingUtil(issueCurrency(tester), tester);
    }

    private static long issueCurrency(Tester tester) {
        APICall apiCall = new TestCurrencyIssuance.Builder()
                .secretPhrase(tester.getSecretPhrase())
                .param("maxSupply", defaultShufflingAmount * 10)
                .param("initialSupply", defaultShufflingAmount * 10)
                .build();
        String result = issueCurrencyApi(apiCall);

        return Long.parseUnsignedLong(result);
    }

    JSONObject createCurrencyShuffling(Tester creator) {
        APICall apiCall = ShufflingCreateCall.create().
                secretPhrase(creator.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                param("amount", String.valueOf(defaultHoldingShufflingAmount)).
                param("participantCount", "4").
                param("registrationPeriod", 10).
                param("holding", Long.toUnsignedString(shufflingCurrency)).
                param("holdingType", String.valueOf(HoldingType.CURRENCY.getCode())).
                build();
        JSONObject response = apiCall.invokeNoError();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    long getShufflingCurrency() {
        return shufflingCurrency;
    }

    void sendCurrencyTo(Tester... testers) {
        Stream.of(testers).forEach(tester -> TransferCurrencyCall.create()
                .secretPhrase(currencyIssuer.getSecretPhrase())
                .feeNQT(0)
                .param("currency", Long.toUnsignedString(shufflingCurrency))
                .param("recipient", tester.getStrId())
                .param("units", "" + defaultShufflingAmount)
                .build().invokeNoError());
    }
}
