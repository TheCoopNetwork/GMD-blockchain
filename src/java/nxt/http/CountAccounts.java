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