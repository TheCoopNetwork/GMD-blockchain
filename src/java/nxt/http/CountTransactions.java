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

public class CountTransactions extends APIServlet.APIRequestHandler {
    static final CountTransactions instance = new CountTransactions();
    protected static final TransactionalDb db = Db.db;


    private CountTransactions() {
        super(new APITag[] {APITag.TRANSACTIONS});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        int count = -1;
        try (Connection con = db.getConnection(); PreparedStatement pstmt = con.prepareStatement(
                "SELECT COUNT(ID) AS TRANSACTION_COUNT FROM TRANSACTION")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                count = rs.getInt(("TRANSACTION_COUNT"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return new CustomAPIResponse(count);
    }
}