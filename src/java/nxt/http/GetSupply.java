package nxt.http;
import nxt.*;

import nxt.util.CustomAPIResponse;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 *
 * This APIRequestHandler is used by the getMaxSupply and getTotalSupply endpoints.
 * As minting is not possible for GMD, MaxSupply will always be equal to TotalSupply and will be equal to initial supply (1,000,000,000 GMD) minus the burned amount.
 * Genesis account is the only negative balance account and is equal to -1,000,000,000 GMD + burned amount. Genesys account cannot transfer out any GMD, only transfer in is possible (burning).
 * As a result, genesis account balance * -1 is the total/max supply.
 */
public class GetSupply extends APIServlet.APIRequestHandler{
    static final GetSupply instance = new GetSupply();
    private static final BigDecimal MINUS_ONE_GMD = new BigDecimal(-1 * Constants.ONE_GMD);

    private GetSupply() {
        super(new APITag[] {APITag.ACCOUNTS});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        Account genesisAccount = Account.getAccount(Genesis.CREATOR_ID);
        long genesisAccountBalanceNQT = genesisAccount.getBalanceNQT();
        BigDecimal supply = new BigDecimal(genesisAccountBalanceNQT).divide(MINUS_ONE_GMD);
        return new CustomAPIResponse(""+supply); // For this particular endpoint a number is required to be returned directly, not a json.
    }
}
