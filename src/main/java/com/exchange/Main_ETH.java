package com.exchange;

import com.exchange.bitz.PublicMethod;
import com.exchange.mxc.Api;
import com.exchange.restapi.ApiRestClient;
import com.exchange.utils.Constant;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main_ETH {
    private static Logger log = Logger.getLogger(Main_ETH.class);
    public static void main(String[] args) throws IOException, HttpException, JSONException, InterruptedException {
        /**
         * ------------------------------------------------------------------
         * @param apiServer     必须
         * @param apiKey        必须
         * @param secretKey     必须
         * @param tradePwd      需要下单交易时必须传（addEntrustSheet）
         * ------------------------------------------------------------------
         */
        String apiServer    = "https://apiv2.bitz.com";
        String apiKey 		= "bced6590a0307aa3c75500d51a79de91";
        String secretKey 	= "GOTyTQkD3YK43n0v1fuoxnWy6Q0qJit6R8SVBNmZZLxOPWxFuP92POTTsyJWArOG";
        String tradePwd 	= "199266qq"; // 委托单 必须 传递 tradePwd 交易密码
        // write your code here
        ApiRestClient restClient = new ApiRestClient(apiServer,apiKey,secretKey,tradePwd);
        String tmp = "";

        //开始编写
        /*
         * 1.秒单：
         * （1）从MXC获取cvt的基准价格，计算利润价；
         * （2）从BITZ根据个人资产计算可以买入的cvt数量，进行秒单，如果有挂单，可以先撤销挂单；
         * （3）秒完单后，如果有剩余的资金，还可以根据挂单情况，找个合适的位置进行挂单。
         * 2.顶单：
         * （1）从MXC获取cvt的基准价格，计算利润价；
         * （2）从BITZ根据个人资产计算可以买入的cvt数量；
         * （3）去除自身订单，在自身之上的若大于5000，需要撤销当前订单，将价格规制到二分位置，直至小于5000为止，不过必须小于利润价；
         * （4）若其他订单价格回落，也应该将价格规制到相应的二分位置，抑或是有相当充足利润时，可以与第二位保持稍小的距离，扩大利润。
         * 3.找个出口，可以随时停掉脚本
         */
        PublicMethod pb = new PublicMethod();
        String data = null;
        String buyStr = null;
        String status = null;
        try {
            //轮询
            while (true){
                //从MXC获取CVT的实时行情信息
                Api mxc_api = new Api();
                List<Map<String,String>> bidsList = pb.getBidsList();
                if(bidsList.size() == 0) continue;
                Double buy_cvt = pb.getAvg(bidsList);
                log.info("MXC中CVT_USDT的基准价格 =================================>"+new BigDecimal(buy_cvt).setScale(8,BigDecimal.ROUND_UP).toPlainString());
                String ticket_eth = mxc_api.getTicket("ETH_USDT");
                if(ticket_eth == null) continue;
                JSONObject json_eth = new JSONObject(ticket_eth);
                if(json_eth == null) continue;
                JSONObject dataObject_eth = json_eth.getJSONObject("data");
                if(dataObject_eth == null) continue;
                Double buy_eth = Double.valueOf(dataObject_eth.getString("buy"));
                Double price_eth = buy_cvt*0.92/buy_eth; //使用ETH计价，利润价格（低于此价）
                log.info("使用ETH计价，基准价格 =================================> "+new BigDecimal(buy_cvt/buy_eth).setScale(8,BigDecimal.ROUND_UP).toPlainString());
                if(price_eth == 0.0) continue;
                log.info("使用ETH计价，利润价格 =================================> "+new BigDecimal(price_eth).setScale(8,BigDecimal.ROUND_UP).toPlainString());
                //被动落单--开始
                status = pb.getBeLongStatus(restClient,"cvt","eth",Constant.BITZ_CURRENCY_TRANSACTION_BUY,price_eth,Constant.BITZ_TRADING_PAIR_CVT_ETH);
                if(status != null && Integer.valueOf(status) == 200){
                    log.info("自动落单成功！");
                }else if(status == null){
                    log.info("不需要落单！");
                }
                //主动落单--开始

                //顶单--开始
                status = pb.getTopOrderStatus(restClient,"cvt","eth",Constant.BITZ_CURRENCY_TRANSACTION_BUY,price_eth,Constant.BITZ_TRADING_PAIR_CVT_ETH);
                if(status != null && Integer.valueOf(status) == 200){
                    log.info("自动顶单成功！");
                }else if(status == null){
                    log.info("不需要顶单！");
                }
                //秒单--开始
                data = pb.getOrderData(restClient,Constant.BITZ_TRADING_PAIR_CVT_ETH);
                if(data == null) continue;
                JSONObject dataJSON = new JSONObject(data);
                if(dataJSON == null) continue;
                String asks = dataJSON.get("asks").toString();
                if(asks == null) continue;
                String[] askArray = asks.substring(1,asks.length()-1).split("],");
                //获取第一个卖单数据
                String[] askArr = askArray[askArray.length-1]
                        .replaceAll("\\[","")
                        .replaceAll("\\]","")
                        .replaceAll("\"","")
                        .split(",");
                log.info("卖单 =================================> "+askArr[0]+"_"+askArr[1]+"_"+askArr[2]);
                if(Double.valueOf(askArr[0])<=price_eth){
                    //秒单 1买进 2卖出
                    String nowPrice = new BigDecimal(askArr[0]).setScale(8,BigDecimal.ROUND_UP).toPlainString();
                    //获取ETH数量，计算可以买入的币数
                    Double eth_num = PublicMethod.getUserAssets(restClient,"eth");
                    //最多能够买的数量
                    String coin_buy_num = String.valueOf(Math.floor(eth_num/Double.valueOf(nowPrice)));
                    log.info("秒单价格  =================================>  "+new BigDecimal(askArr[0]).setScale(8,BigDecimal.ROUND_UP).toPlainString());
                    if(Double.valueOf(askArr[1])>Double.valueOf(coin_buy_num)){
                        askArr[1]=coin_buy_num;
                    }
                    buyStr = restClient.addEntrustSheet(Constant.BITZ_TRADING_PAIR_CVT_ETH,askArr[1],nowPrice, Constant.BITZ_CURRENCY_TRANSACTION_BUY);
                    buyStr = buyStr.replaceAll("\\{","")
                            .replaceAll("\\}","")
                            .replaceAll("\"","");
                    status = buyStr.split(",")[0].split(":")[1].toString();
                    if(Integer.valueOf(status) == -200031){ // 资产不足
                        List<Map<String,String>> list = pb.getOrderDetail(restClient,"cvt","eth",Constant.BITZ_CURRENCY_TRANSACTION_BUY);
                        if(list != null && list.size()-1>=0){
                            for(int m=0;m<=list.size()-1;m++){
                                String id = list.get(m).get("id");
                                //撤销限价委托单
                                String revoke_order = restClient.cancelEntrustSheet(id);
                                Thread.sleep(3000);
                            }
                            //重新秒单
                            buyStr = restClient.addEntrustSheet(Constant.BITZ_TRADING_PAIR_CVT_ETH,askArr[1],nowPrice, Constant.BITZ_CURRENCY_TRANSACTION_BUY);
                            buyStr = buyStr.replaceAll("\\{","")
                                    .replaceAll("\\}","")
                                    .replaceAll("\"","");
                            status = buyStr.split(",")[0].split(":")[1].toString();
                            //秒单成功，重新挂单
                            if(Integer.valueOf(status) == 200){
                                //重新挂单
                                String position = pb.getOrderPosition(restClient,"cvt",Constant.BITZ_TRADING_PAIR_CVT_ETH,price_eth);
                                position = new BigDecimal(position).setScale(8,BigDecimal.ROUND_UP).toPlainString();
                                //重新获取资产数量
                                Double eth_num_now = PublicMethod.getUserAssets(restClient,"eth");
                                if(eth_num_now == 0.0) continue; //没有资产就不挂单
                                //可以挂单的数量
                                String coin_buy_num_now = String.valueOf(Math.floor(eth_num_now/Double.valueOf(position)));
                                log.info("挂单价格 =================================> "+new BigDecimal(position).setScale(8,BigDecimal.ROUND_UP).toPlainString());
                                log.info("挂单数量 =================================> "+coin_buy_num_now);
                                //挂单
                                buyStr = restClient.addEntrustSheet(Constant.BITZ_TRADING_PAIR_CVT_ETH,coin_buy_num_now,position, Constant.BITZ_CURRENCY_TRANSACTION_BUY);
                                buyStr = buyStr.replaceAll("\\{","")
                                        .replaceAll("\\}","")
                                        .replaceAll("\"","");
                                status = buyStr.split(",")[0].split(":")[1];
                                if(Integer.valueOf(status) == 200){
                                    log.info("自动挂单成功！");
                                }

                            }

                        }

                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

}

