package nxt.http;

import nxt.Db;
import nxt.NxtException;
import nxt.db.TransactionalDb;
import nxt.util.CustomAPIResponse;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * This endpoint returns the number of accounts known to the blockchain. The return is a plain number not a json.
 * An account can be known by the blockchain for 2 reasons:
 *  a). it was part of at least one transaction
 *  b). it was part of the initial pool of accounts being credited during genesys.
 *
 *  This endpoint has one optional parameter: minUnconfirmedBalanceNQT. When set, the endpoint will return the count of
 *  the accounts with minUnconfirmedBalanceNQT or more UNCONFIRMED_BALANCE
 *
 */
public class CountAccounts extends APIServlet.APIRequestHandler {
    static final CountAccounts instance = new CountAccounts();
    protected static final TransactionalDb db = Db.db;


    private CountAccounts() {
        super(new APITag[] {APITag.ACCOUNTS}, "minUnconfirmedBalanceNQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        Long minUnconfirmedBalanceNQT = 0L;
        String param = request.getParameter("minUnconfirmedBalanceNQT");
        if(param!=null) {
            try {
                minUnconfirmedBalanceNQT = Long.parseLong(param);
            } catch (NumberFormatException e) {
                throw new ParameterException(JSONResponses.INCORRECT_MIN_BALANCE);
            }
        }
        int count = -1;
        try (Connection con = db.getConnection(); PreparedStatement pstmt = con.prepareStatement(
                "SELECT COUNT(DISTINCT ID) AS ACCOUNT_COUNT FROM ACCOUNT WHERE UNCONFIRMED_BALANCE > ?")) {
            pstmt.setLong(1, minUnconfirmedBalanceNQT);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                count = rs.getInt(("ACCOUNT_COUNT"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return new CustomAPIResponse(count);
    }
}