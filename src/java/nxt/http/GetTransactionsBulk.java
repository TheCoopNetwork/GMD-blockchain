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
import java.util.function.Predicate;


public class GetTransactionsBulk extends APIServlet.APIRequestHandler {
    static final GetTransactionsBulk instance = new GetTransactionsBulk();
    protected static final TransactionalDb db = Db.db;
    public static final String[] type = {"payment", "messaging", "colored_coins", "digital_goods", "account_control", "monetary_system", "data", "shuffling"};
    public static final String[][] subtype = {
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
        super(new APITag[] {APITag.TRANSACTIONS},  "pageSize", "page", "filterBySender", "filterByReceiver", "filterByType", "filterBySubtype");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws ParameterException {
        Integer pageSize = getIntParamFromRequest(request, "pageSize", i->i<1||i>100 , JSONResponses.INCORRECT_PAGE_SIZE , false);
        Integer page = getIntParamFromRequest(request, "page", i->i<0 , JSONResponses.INCORRECT_PAGE, false);
        long senderRSId=decodeAddrToLongId(request.getParameter("filterBySender"));
        long recvRSId=decodeAddrToLongId(request.getParameter("filterByReceiver"));
        Integer typeId;
        Integer subTypeId;
        typeId = getIntParamFromRequest(request, "filterByType", i->false, JSONResponses.INCORRECT_TYPE, true);
        subTypeId = getIntParamFromRequest(request, "filterBySubtype", i->false, JSONResponses.INCORRECT_SUBTYPE , true);

        try (Connection con = db.getConnection(); PreparedStatement pstmt = con.prepareStatement(
                "SELECT * "+
                        "FROM TRANSACTION " +
                        "WHERE TRUE "+
                        (senderRSId!=0 ? "AND ? = SENDER_ID ":"") +
                        (recvRSId!=0 ? "AND ? = RECIPIENT_ID ":"") +
                        (typeId!=null ? "AND ? = TYPE ":"") +
                        (subTypeId!=null ? "AND ? = SUBTYPE ":"") +
                        "ORDER BY TIMESTAMP DESC " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")) {
            int offset = 0;
            if (senderRSId!=0){
                pstmt.setLong(++offset,senderRSId);
            }
            if (recvRSId!=0){
                pstmt.setLong(++offset,recvRSId);
            }
            if(typeId!=null){
                pstmt.setInt(++offset,typeId);
            }
            if(subTypeId!=null){
                pstmt.setInt(++offset,subTypeId);
            }
            pstmt.setInt(++offset, page*pageSize);
            pstmt.setInt(++offset, pageSize);

            JSONObject response = new JSONObject();
            response.put("Transactions", executePreparedStatement(pstmt));
            return response;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    /**
     *
     * @param request
     * @param parameterName
     * @param predicateCheckFail condition to be applied on the output integer. If true will throw ParameterException.
     * @param errorResponse details for the ParameterException
     * @param isOptional Optional parameters do not throw ParameterException in case parameter was not supplied. Mandatory parameters will throw exception if missing.
     * @return null if parameter is not present and is optional. An integer if the string param contains an integer. An exception is thrown otherwise.
     * @throws ParameterException
     */
    private Integer getIntParamFromRequest(HttpServletRequest request, String parameterName, Predicate<Integer> predicateCheckFail, JSONStreamAware errorResponse, boolean isOptional) throws ParameterException {
        String param = request.getParameter(parameterName);
        if(param==null){
            if(!isOptional) {
                throw new ParameterException(errorResponse);
            }
            return null;
        }
        Integer output;
        try {
            output = Integer.parseInt(param);
            if(predicateCheckFail.test(output)) {
                throw new ParameterException(errorResponse);
            }
        } catch (NumberFormatException e) {
            throw new ParameterException(errorResponse);
        }
        return output;
    }

    /**
     *
     * @param input String cotaining either long number either RS addr (format GMD-...)
     * @return Long id decoded
     */
    private long decodeAddrToLongId(String input){
        long output = 0L;
        if(input!=null){
            if(input.toUpperCase().startsWith("GMD-")){
                String rsIdStr = input.substring(4);
                try {
                    output = Crypto.rsDecode(rsIdStr);
                } catch(Exception e) {}
            } else {
                try {
                    output = Long.parseLong(input);
                } catch (NumberFormatException e) {}
            }
        }
        return output;
    }

    private JSONArray executePreparedStatement(PreparedStatement pstmt) throws SQLException{
        JSONArray outputArray = new JSONArray();
        try (ResultSet rs = pstmt.executeQuery();) {
            while (rs.next()) {
                JSONObject o = new JSONObject();
                o.put("ID", rs.getLong("ID"));
                Long recId = rs.getLong("RECIPIENT_ID");
                if (recId != 0) {
                    o.put("RECIPIENT_ID", "GMD-" + Crypto.rsEncode(recId));
                }
                o.put("TRANSACTION_INDEX", rs.getInt("TRANSACTION_INDEX"));
                o.put("AMOUNT", rs.getLong("AMOUNT"));
                o.put("FEE", rs.getLong("FEE"));
                o.put("FULL_HASH", rs.getString("FULL_HASH"));
                o.put("HEIGHT", rs.getLong("HEIGHT"));
                o.put("BLOCK_ID", rs.getLong("BLOCK_ID"));
                o.put("SIGNATURE", rs.getString("SIGNATURE"));
                o.put("TIMESTAMP", rs.getLong("TIMESTAMP"));
                o.put("SENDER_ID", "GMD-" + Crypto.rsEncode(rs.getLong("SENDER_ID")));
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
                } catch (Exception e) {
                    type_json.put("type_str", "unknown");
                }
                try {
                    type_json.put("subtype_str", subtype[t][st]);
                } catch (Exception e) {
                    type_json.put("subtype_str", "unknown");
                }
                o.put("type_obj", type_json);

                outputArray.add(o);
            }
        }
        return outputArray;
    }
}
