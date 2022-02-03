package nxt.http;

import nxt.Db;
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


public class GetTransactionsBulk extends APIServlet.APIRequestHandler {
    static final GetTransactionsBulk instance = new GetTransactionsBulk();
    protected static final TransactionalDb db = Db.db;
    String[] type = {"payment", "messaging", "colored_coins", "digital_goods", "account_control", "monetary_system", "data", "shuffling"};
    String[][] subtype = {
            {"ordinary_payment"},
            {"arbitrary_message", "alias_assignment", "poll_creation", "vote_casting", "hub_announcement", "account_info", "alias_sell", "alias_buy", "alias_delete", "phasing_vote_casting", "account_property", "account_property_delete"},
            {"asset_issuance", "asset_transfer", "ask_order_placement", "bid_order_placement", "ask_order_cancellation", "bid_order_cancellation", "dividend_payment", "asset_delete", "asset_increase", "property_set", "property_delete"},
            {"listing", "delisting", "price_change", "quantity_change", "purchase", "delivery", "feedback", "refund"},
            {"effective_balance_leasing", "phasing_only"},
            {"currency_issuance", "reserve_increase", "reserve_claim", "currency_transfer", "publish_exchange_offer", "exchange_buy", "exchange_sell", "currency_minting", "currency_deletion"},
            {"upload", "extend"},
            {"creation", "registration", "processing", "recipients", "verification", "cancellation"}
    };

    private GetTransactionsBulk() {
        super(new APITag[] {APITag.TRANSACTIONS},  "pageSize", "page");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws ParameterException {
        int pageSize;
        int page;
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
                throw new ParameterException(JSONResponses.INCORRECT_PAGE);
            }
        } catch (NumberFormatException e) {
            throw new ParameterException(JSONResponses.INCORRECT_PAGE);
        }

        JSONObject response = new JSONObject();
        try (Connection con = db.getConnection(); PreparedStatement pstmt = con.prepareStatement(
                "SELECT * "+
                        "FROM TRANSACTION " +
                        "ORDER BY TIMESTAMP DESC " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")) {
            pstmt.setInt(1, page*pageSize);
            pstmt.setInt(2, pageSize);
            try (ResultSet rs = pstmt.executeQuery();) {

                JSONArray arr = new JSONArray();
                while (rs.next()) {
                    JSONObject o = new JSONObject();
                    o.put("ID", rs.getLong("ID"));
                    Long recId = rs.getLong("RECIPIENT_ID");
                    if (recId != 0) {
                        o.put("RECIPIENT_ID", "GMD-"+Crypto.rsEncode(recId));
                    }
                    o.put("TRANSACTION_INDEX", rs.getInt("TRANSACTION_INDEX"));
                    o.put("AMOUNT", rs.getLong("AMOUNT"));
                    o.put("FEE", rs.getLong("FEE"));
                    o.put("FULL_HASH", rs.getString("FULL_HASH"));
                    o.put("HEIGHT", rs.getLong("HEIGHT"));
                    o.put("BLOCK_ID", rs.getLong("BLOCK_ID"));
                    o.put("SIGNATURE", rs.getString("SIGNATURE"));
                    o.put("TIMESTAMP", rs.getLong("TIMESTAMP"));
                    o.put("SENDER_ID", "GMD-"+Crypto.rsEncode(rs.getLong("SENDER_ID")));
                    o.put("BLOCK_TIMESTAMP", rs.getLong("BLOCK_TIMESTAMP"));
                    o.put("PHASED", rs.getBoolean("PHASED"));
                    o.put("ATTACHMENT_BYTES", rs.getString("ATTACHMENT_BYTES"));
                    JSONObject type_json = new JSONObject();
                    int t = rs.getInt("TYPE");
                    int st = rs.getInt("SUBTYPE");
                    type_json.put("TYPE", t);
                    type_json.put("SUBTYPE", st);
                    try {
                        type_json.put("type_str", type[t]);
                    } catch (Exception e){
                        type_json.put("type_str","unknown");
                    }
                    try {
                        type_json.put("subtype_str", subtype[t][st]);
                    } catch (Exception e){
                        type_json.put("subtype_str","unknown");
                    }
                    o.put("type_obj", type_json);

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
