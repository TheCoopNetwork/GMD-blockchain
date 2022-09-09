package nxt.util;

import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.Writer;

public class CustomAPIResponse implements JSONStreamAware {
    private String msg;
    public CustomAPIResponse(String msg){
        this.msg = msg;
    }
    public CustomAPIResponse(int i){
        this.msg = ""+i;
    }
    @Override
    public void writeJSONString(Writer writer) throws IOException {
        if(msg==null){
            writer.write("null");
        } else {
            writer.write(msg);
        }
    }
}