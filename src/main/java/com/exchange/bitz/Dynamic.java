package com.exchange.bitz;

import com.exchange.test;
import com.exchange.weChatUtils.WeChatMsgSend;
import com.exchange.weChatUtils.WeChatUrlData;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Dynamic {
    private static Logger log = Logger.getLogger(Dynamic.class);
    public static void Logger(String info,String... args) {
        WeChatMsgSend swx = new WeChatMsgSend();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            if (args.length>0 && args[0].equals("@")) {
                String token = swx.getToken("wwd5fe0bb9d75b271b", "iXLdiDs0Vts8xArmyxtOcwgxhWBZmTR6VS0X2cmflgs");
                String postdata = swx.createpostdata("wangyang", "text", 1000002, "content", date+" "+info);
                String resp = swx.post("utf-8", WeChatMsgSend.CONTENT_TYPE, (new WeChatUrlData()).getSendMessage_Url(), postdata, token);
                System.out.println("发送微信的响应数据======>" + resp);
            }
            log.info(info);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
