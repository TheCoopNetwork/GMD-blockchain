package nxt.http;

import nxt.Db;
import nxt.crypto.Crypto;
import nxt.db.TransactionalDb;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;


public class GetAccountsBulk extends APIServlet.APIRequestHandler {
    static final GetAccountsBulk instance = new GetAccountsBulk();
    protected static final TransactionalDb db = Db.db;

    private GetAccountsBulk() {
        super(new APITag[] {APITag.ACCOUNTS}, "minBalanceNQT", "pageSize", "page", "includeDescription", "includeEffectiveBalance");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws ParameterException {
        int pageSize;
        int page;
        long minBalanceNQT;
        boolean includeDescription;
        boolean includeEffectiveBalance;
        try {
            pageSize = Integer.parseInt(request.getParameter("pageSize"));
            if(pageSize < 1 || pageSize > 100) {
                throw new ParameterException(JSONResponses.INCORRECT_PAGE_SIZE);
            }
        } catch (NumberFormatException e) {
            throw new ParameterException(JSONResponses.INCORRECT_PAGE_SIZE);
        }
        try {
            page = Integer.parseInt(request.getParameter("page"));
            if(page < 0) {
                throw new ParameterException(JSONResponses.INCORRECT_PAGE_SIZE);
            }
        } catch (NumberFormatException e) {
            throw new ParameterException(JSONResponses.INCORRECT_PAGE);
        }
        try {
            minBalanceNQT = Long.parseLong(request.getParameter("minBalanceNQT"));
        } catch (NumberFormatException e) {
            minBalanceNQT = 0;
        }
        try {
            includeDescription = Boolean.parseBoolean(request.getParameter("includeDescription"));
        } catch (NumberFormatException e) {
            includeDescription = false;
        }
        try {
            includeEffectiveBalance = Boolean.parseBoolean(request.getParameter("includeEffectiveBalance"));
        } catch (NumberFormatException e) {
            includeEffectiveBalance = false;
        }



        JSONObject response = new JSONObject();
        try (Connection con = db.getConnection(); PreparedStatement pstmt = con.prepareStatement(
                "SELECT ID,UNCONFIRMED_BALANCE,ACCOUNT.LATEST,FORGED_BALANCE,ACTIVE_LESSEE_ID,ACCOUNT.HEIGHT" +
                        (includeDescription? ",NAME,DESCRIPTION ":" ")+
                        (includeEffectiveBalance? ",BALANCE ":" ")+
                        "FROM ACCOUNT " +
                        (includeDescription ? "LEFT JOIN ACCOUNT_INFO ON ACCOUNT.ID = ACCOUNT_INFO.ACCOUNT_ID ":"")+
                        "WHERE ACCOUNT.LATEST=TRUE AND UNCONFIRMED_BALANCE >= ? " +
                        "ORDER BY UNCONFIRMED_BALANCE DESC " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")) {
            pstmt.setLong(1, minBalanceNQT);
            pstmt.setInt(2, page*pageSize);
            pstmt.setInt(3, pageSize);
            try (ResultSet rs = pstmt.executeQuery();) {

                JSONArray arr = new JSONArray();
                while (rs.next()) {
                    JSONObject o = new JSONObject();
                    o.put("ID", "GMD-"+Crypto.rsEncode(rs.getLong("ID")));
                    o.put("UNCONFIRMED_BALANCE", rs.getLong("UNCONFIRMED_BALANCE"));
                    o.put("FORGED_BALANCE", rs.getLong("FORGED_BALANCE"));
                    Long lesee = rs.getLong("ACTIVE_LESSEE_ID");
                    if (lesee != 0) {
                        o.put("ACTIVE_LESSEE_ID", "GMD-"+Crypto.rsEncode(lesee));
                    }

                    o.put("HEIGHT", rs.getLong("ACCOUNT.HEIGHT"));

                    if (includeDescription){
                        String name = rs.getString("NAME");
                        String description = rs.getString("DESCRIPTION");
                        if (name!=null){
                            o.put("NAME",name);
                        }
                        if (description!=null){
                            o.put("DESCRIPTION", name);
                        }
                    }

                    if(includeEffectiveBalance){
                        o.put("EFFECTIVE_BALANCE", rs.getLong(("BALANCE")));
                    }

                    arr.add(o);

                }
                response.put("Accounts",arr);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return response;
    }
}
