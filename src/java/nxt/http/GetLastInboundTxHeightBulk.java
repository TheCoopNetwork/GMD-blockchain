package nxt.http;

import nxt.Constants;
import nxt.Db;
import nxt.Nxt;
import nxt.crypto.Crypto;
import nxt.db.TransactionalDb;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;


public class GetLastInboundTxHeightBulk extends APIServlet.APIRequestHandler {
    static final String ACCOUNT_PREFIX = Constants.ACCOUNT_PREFIX+ "-";
    static final GetLastInboundTxHeightBulk instance = new GetLastInboundTxHeightBulk();
    protected static final TransactionalDb db = Db.db;


    private GetLastInboundTxHeightBulk() {
        super(new APITag[] {APITag.TRANSACTIONS}, "listOfAccounts");
    }

    @Override
    protected boolean requirePost() {
        return true;
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws ParameterException {

        String listOfAccounts = request.getParameter("listOfAccounts"); //comma separated list of accounts
        if(listOfAccounts == null){
            throw new ParameterException(JSONResponses.INCORRECT_INPUT_LIST_PARAMETER);
        }
        String[] accounts = listOfAccounts.split(",");
        if(accounts.length == 0){
            throw new ParameterException(JSONResponses.INCORRECT_INPUT_LIST_PARAMETER);
        }

        LinkedList<String> accountIds = new LinkedList<>();
        for(String account : accounts){
            String accountId = rsToLongId(account);
            if(accountId!=null){
                accountIds.add(accountId);
            }
        }
        if (accountIds.size() == 0){
            throw new ParameterException(JSONResponses.INCORRECT_INPUT_LIST_PARAMETER);
        }

        String queryList = String.join(",", accountIds);


        String query = "SELECT MAX(TRANSACTION.HEIGHT) AS HEIGHT_LAST_TRANSACTION, ACCOUNT.ID, ACCOUNT.UNCONFIRMED_BALANCE "+
                "FROM TRANSACTION LEFT JOIN ACCOUNT ON TRANSACTION.RECIPIENT_ID=ACCOUNT.ID "+
                "WHERE ACCOUNT.ID IN ("+ queryList +") "+
                "AND ACCOUNT.LATEST=TRUE GROUP BY TRANSACTION.RECIPIENT_ID ORDER BY HEIGHT_LAST_TRANSACTION DESC";
        try (Connection con = db.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
            JSONObject response = new JSONObject();
            response.put("results", executePreparedStatement(pstmt));
            response.put("currentHeight", Nxt.getBlockchain().getHeight());
            return response;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }



    private String rsToLongId(String input){
        try {
            return ""+Crypto.rsDecode(input.toUpperCase().startsWith(ACCOUNT_PREFIX) ? input.substring(4) : input);
        } catch(Exception e) {
            return null;
        }
    }

    private JSONArray executePreparedStatement(PreparedStatement pstmt) throws SQLException{
        JSONArray outputArray = new JSONArray();
        try (ResultSet rs = pstmt.executeQuery();) {
            while (rs.next()) {
                JSONObject o = new JSONObject();
                o.put("account", ACCOUNT_PREFIX +Crypto.rsEncode(rs.getLong("ID")));
                o.put("height", rs.getInt("HEIGHT_LAST_TRANSACTION"));
                o.put("balance", ""+rs.getLong("UNCONFIRMED_BALANCE"));

                outputArray.add(o);
            }
        }
        return outputArray;
    }
}
