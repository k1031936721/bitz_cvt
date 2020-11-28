package com.exchange;

import com.exchange.bitz.PublicMethod;
import com.exchange.mxc.Api;
import com.exchange.restapi.ApiRestClient;
import com.exchange.utils.Constant;
import org.json.JSONObject;

import javax.sound.midi.Soundbank;
import java.awt.dnd.DragSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) throws Exception {
        String apiServer    = "https://apiv2.bitz.com";
        String apiKey 		= "bced6590a0307aa3c75500d51a79de91";
        String secretKey 	= "GOTyTQkD3YK43n0v1fuoxnWy6Q0qJit6R8SVBNmZZLxOPWxFuP92POTTsyJWArOG";
        String tradePwd 	= "199266qq"; // 委托单 必须 传递 tradePwd 交易密码
        // write your code here
        ApiRestClient restClient = new ApiRestClient(apiServer,apiKey,secretKey,tradePwd);
        String tmp = "";
        tmp = restClient.depth("cvt_btc");
        System.out.println("获取深度数据:\n"+tmp);
        //顶单
        /*PublicMethod pb = new PublicMethod();
        List<Map<String,String>> depthList = pb.getDepthData(restClient);
        List<Map<String,String>> orderlist = pb.getOrderDetail(restClient,"cvt","btc",Constant.BITZ_CURRENCY_TRANSACTION_BUY);
        System.out.print("");
        Double dmp = 0.0;
        Double sum = 0.0;
        String coinFrom = "cvt";
        String trade = Constant.BITZ_TRADING_PAIR_CVT_BTC;
        //返回当前委托单的最大价格
        for(int i=0;i<=orderlist.size()-1;i++){
            Map<String,String> orderMap = orderlist.get(i);
            Double price = Double.valueOf(orderMap.get("price"));
            if(dmp<price){
                dmp = price;
            }
        }
        for(int j=0;j<=depthList.size()-1;j++){
            Map<String,String> depthMap = depthList.get(j);
            Double price = Double.valueOf(depthMap.get("price"));
            Double number = Double.valueOf(depthMap.get("number"));
            if(price>dmp){
                sum+=number;
            }
        }
        //根据上面的挂单触发顶单
        if(sum>=10000){
            //撤销当前委托单
            for(int i=0;i<=orderlist.size()-1;i++){
                Map<String,String> orderMap = orderlist.get(i);
                String id = orderMap.get("id");
                //撤销订单
                restClient.cancelEntrustSheet(id);
                Thread.sleep(3000);
            }
            //重新挂单
            String position = pb.getOrderPosition(restClient,coinFrom,trade,price_btc);
            position = new BigDecimal(position).setScale(8,BigDecimal.ROUND_UP).toPlainString();
            //重新获取资产数量
            Double btc_num_now = PublicMethod.getUserAssets(restClient,coinTo);
            //获取个人冻结资产
            Double btc_freez_now = pb.getUserNowFreezingAssets(restClient,coinFrom,coinTo,type);
            //可以使用的资产数目
            Double btc_num_ky_now = btc_num_now-btc_freez_now;
            //可以挂单的数量
            String coin_buy_num_now = String.valueOf(Math.floor(btc_num_ky_now/Double.valueOf(position)));
            log.info("落单价格  =================================> "+new BigDecimal(position).setScale(8,BigDecimal.ROUND_UP).toPlainString());
            log.info("落单数量  =================================> "+coin_buy_num_now);
            //挂单
            buyStr = restClient.addEntrustSheet(trade,coin_buy_num_now,position, type);
            buyStr = buyStr.replaceAll("\\{","")
                    .replaceAll("\\}","")
                    .replaceAll("\"","");
            status = buyStr.split(",")[0].split(":")[1].toString();

        }*/
    }






}
