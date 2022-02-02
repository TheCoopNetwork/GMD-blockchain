package nxt.http;

import nxt.Account;
import nxt.Attachment;
import nxt.NxtException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class MintNFT extends CreateTransaction{
    static final MintNFT instance = new MintNFT();

    private  MintNFT() {
        super("file", new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "name", "description", "filename", "data");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        Account account = ParameterParser.getSenderAccount(request);
        Attachment.NFTAttachment nftAttachment = ParameterParser.getNFTData(request);
        return createTransaction(request, account, nftAttachment);
    }
}
