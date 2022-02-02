// Auto generated code, do not modify
package nxt.http.callers;

public class MintNFTCall extends CreateTransactionCallBuilder<MintNFTCall> {
    private MintNFTCall() {
        super(ApiSpec.mintNFT);
    }

    public static MintNFTCall create() {
        return new MintNFTCall();
    }

    public MintNFTCall name(String name) {
        return param("name", name);
    }

    public MintNFTCall description(String description) {
        return param("description", description);
    }

    public MintNFTCall data(String data) {
        return param("data", data);
    }
}
