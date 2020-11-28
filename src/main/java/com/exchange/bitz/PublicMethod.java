package com.exchange.bitz;

import com.exchange.Main;
import com.exchange.mxc.Api;
import com.exchange.restapi.ApiRestClient;
import com.exchange.utils.Constant;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicMethod {
    private static Logger log = Logger.getLogger(PublicMethod.class);
    /**
     * 获取订单详情
     * @param restClient
     * @param coinFrom
     * @param coinTo
     * @param type
     * @return
     */
    public List<Map<String,String>> getOrderDetail(ApiRestClient restClient,String coinFrom, String coinTo, String type){
        //当前委托单
        String orderStr = restClient.getUserNowEntrustSheet(coinFrom,coinTo,type);
        String d1 = null;
        String d2 = null;
        if(orderStr == null) return null;
        JSONObject orderJSON = null;
        Map<String,String> map = new HashMap<String,String>();
        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        try {
            orderJSON = new JSONObject(orderStr);
            if(orderJSON.get("data") != null){
                d1 = orderJSON.get("data").toString();
            }
            if(d1 == null) return null;
            JSONObject orderJSON2 = new JSONObject(d1);
            if(orderJSON2.get("data") != null){
                d2 = orderJSON2.get("data").toString();
            }
            if(d2 == null || "null".equals(d2)) return null;
            String[] arr = d2.substring(1,d2.length()-1).split("},");
            for(String str : arr){
                str = str.replaceAll("\\{","").replaceAll("\\}","").replaceAll("\"","");
                arr = str.split(",");
                for(String st : arr){
                    String[] a = st.split(":");
                    if(a.length>=2){
                        map.put(a[0],a[1]);
                    }else{
                        map.put(a[0],"");
                    }

                }
                list.add(map);
                map = new HashMap<String,String>();
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("报错日志==============>"+e.getMessage());
        }

        return list;
    }

    /**
     * 获取币种个人持有资产
     * @param restClient
     * @param coin
     * @return
     */
    public static Double getUserAssets(ApiRestClient restClient,String coin) throws InterruptedException {
        String userAssets = restClient.getUserAssets();
        if(userAssets == null) return null;
        JSONObject json_bitz = new JSONObject(userAssets);
        JSONArray jsonArray = json_bitz.getJSONObject("data").getJSONArray("info");
        Double coin_num = 0.0;
        for(int i=0;i<jsonArray.length()-1;i++){
            String str = jsonArray.getJSONObject(i).get("name").toString().replaceAll("\"","");
            if(coin.equals(str)){
                coin_num =Double.valueOf((String) jsonArray.getJSONObject(i).get("num"));
                log.info("获取币种个人持有资产_"+ coin+"================>"+coin_num);
                break;
            }

        }
        Thread.sleep(1000);
        return coin_num;
    }

    /**
     *  获取深度数据
     * @param restClient
     * @param coinTo
     * @return
     */
    public String getOrderData(ApiRestClient restClient,String coinTo){
        String tmp = "";
        String data = "";
        try {
            /*if(coinTo.equals(Constant.BITZ_TRADING_PAIR_CVT_BTC)){
                tmp = restClient.depth(Constant.BITZ_TRADING_PAIR_CVT_BTC);
                if(tmp == null || "".equals(tmp)){
                    Thread.sleep(3000);
                    tmp = restClient.depth(Constant.BITZ_TRADING_PAIR_CVT_BTC);
                }
            }else if(coinTo.equals(Constant.BITZ_TRADING_PAIR_CVT_ETH)){
                tmp = restClient.depth(Constant.BITZ_TRADING_PAIR_CVT_ETH);
                if(tmp == null || "".equals(tmp)){
                    Thread.sleep(3000);
                    tmp = restClient.depth(Constant.BITZ_TRADING_PAIR_CVT_ETH);
                }
            }*/
            tmp = restClient.depth(coinTo);
            if(tmp == null || "".equals(tmp)){
                Thread.sleep(3000);
                tmp = restClient.depth(coinTo);
            }
            if(tmp == null) return null;
            JSONObject jsonObj = new JSONObject(tmp);
            data = jsonObj.get("data").toString();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("报错日志=============>"+e.getMessage());
        }

        return data;

    }

    /**
     * 获取挂单位置
     * @param restClient
     * @return
     */
    public String getOrderPosition(ApiRestClient restClient,String coinFrom,String coinTo,Double price_coin){
        //coinTo btc/eth
        //coinFrom cvt
        JSONObject dataJSON = null;
        String position = null;
        String data = null;
        try {
            data = getOrderData(restClient,coinTo);
            if(data == null) return null;
            dataJSON = new JSONObject(data);
//            String asks = dataJSON.get("asks").toString();
            String bids = dataJSON.get("bids").toString();
            if(bids == null) return null;
//            String[] askArray = asks.substring(1,asks.length()-1).split("],");
            String[] bidArray = bids.substring(1,bids.length()-1).split("],");
            if(bidArray == null) return null;
            Double tmp_0 = 0.0;
            Double tmp_1 = 0.0;
            Double tmp_2 = 0.0;
            for(int i=0;i<=bidArray.length-1;i++) {
                String[] bidArr = bidArray[i]
                        .replaceAll("\\[", "")
                        .replaceAll("\\]", "")
                        .replaceAll("\"", "")
                        .split(",");
                tmp_0 += Double.valueOf(bidArr[0]);//价格
                tmp_1 += Double.valueOf(bidArr[1]);//数量
                tmp_2 += Double.valueOf(bidArr[0])*Double.valueOf(bidArr[1]);//总值
                if(tmp_1>=0 && tmp_1<20000){
                    //小于利润价并满足以上条件跳出
                    if(Double.valueOf(bidArr[0])+0.00000001<=price_coin*0.96){
                        position = String.valueOf(Double.valueOf(bidArr[0])+0.00000001);
                        System.out.print(position);
                        break;
                    }
                }else if(tmp_1>=20000 && tmp_1<50000){
                    if(Double.valueOf(tmp_2/tmp_1)+0.00000001<=price_coin*0.96){
                        position = String.valueOf(new BigDecimal(Double.valueOf(tmp_2/tmp_1)+0.00000001).setScale(8, java.math.BigDecimal.ROUND_UP).toPlainString());
                        System.out.print(position);
                        break;
                    }
                }else if(tmp_1>=50000){
                    if(Double.valueOf(tmp_2/tmp_1)+0.00000001<=price_coin && Double.valueOf(tmp_2/tmp_1)+0.00000001>price_coin*0.96){
                        position = String.valueOf(Double.valueOf(tmp_2/tmp_1)+0.00000001);
                        System.out.print(position);
                    }else{
                        position = String.valueOf(price_coin*0.75);
                        System.out.print(position);
                    }
                    break;
                }
            }
            log.info("挂单基准极限价格:"+coinTo+" ============================> "+new BigDecimal(price_coin*0.96-0.00000001).setScale(8,BigDecimal.ROUND_UP).toPlainString());
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("报错日志=============>"+e.getMessage());

        }

        return position;

    }

    /**
     * 获取当前冻结资产
     * @param coinFrom
     * @param coinTo
     * @param type
     * @return
     */
    public Double getUserNowFreezingAssets(ApiRestClient restClient,String coinFrom, String coinTo, String type) throws InterruptedException {
        PublicMethod pb = new PublicMethod();
        List<Map<String,String>> list = pb.getOrderDetail(restClient,coinFrom,coinTo,type);
        Double sum = 0.0;
        Double price = 0.0;
        Double number = 0.0;
        if(list != null && list.size()-1>=0) {
            for (int m = 0; m <= list.size() - 1; m++) {
                if(list.get(m).get("price") != null){
                    price = Double.valueOf(list.get(m).get("price"));
                }
                if(list.get(m).get("number") != null){
                    number = Double.valueOf(list.get(m).get("number"));
                }
                sum += price*number;

            }
        }
        Thread.sleep(1000);
        return sum;
    }

    /**
     * 获取落单状态
     * @param restClient
     * @param coinFrom cvt
     * @param coinTo cvt_btc
     * @param type
     * @param price_btc
     * @return
     */
    public String getBeLongStatus(ApiRestClient restClient,String coinFrom, String coinTo, String type,Double price_btc,String trade){
        String buyStr = null;
        String status = null;
        String bj = null;
        try {
            PublicMethod pb = new PublicMethod();
            List<Map<String,String>> ldList = pb.getOrderDetail(restClient,coinFrom,coinTo,type);
            if(ldList != null && ldList.size()-1>=0) {
                for (int m = 0; m <= ldList.size() - 1; m++) {
                    if(ldList.get(m).get("price") == null) continue;
                    Double price = Double.valueOf(ldList.get(m).get("price"));
                    String id = ldList.get(m).get("id");
                    if(price != null && price>price_btc){
                        //撤销订单
                        restClient.cancelEntrustSheet(id);
                        Thread.sleep(3000);
                        bj = "cx";
                    }
                }
                if(bj != "cx") return null;
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
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("报错日志=============>"+e.getMessage());
        }
        return status;
    }

    /**
     * 获取MXC交易深度
     * @return
     */
    public List<Map<String,String>> getBidsList(){
        Api mxc_api = new Api();
        Map<String,String> bidMap = new HashMap<String,String>();
        List<Map<String,String>> bidsList = new ArrayList<Map<String,String>>();
        try {
            String marketDepth = null;
            marketDepth = mxc_api.getMarketDepth();
            if(marketDepth == null) return null;
            JSONObject json_cvt = new JSONObject(marketDepth);
            JSONObject dataObject_cvt = json_cvt.getJSONObject("data");
            String bids = dataObject_cvt.getString("bids");
            String[] bidsArr = bids.replaceAll("\\[","").replaceAll("\\]","").split("},");
            for(int i=0;i<=bidsArr.length-1;i++){
                String[] bidArr = bidsArr[i].replaceAll("\\}","").replaceAll("\\{","").split(",");
                bidMap = new HashMap<String,String>();
                for(int j=0;j<=bidArr.length-1;j++){
                    String[] arr = bidArr[j].replaceAll("\"","").split(":");
                    if(arr.length>1){
                        bidMap.put(arr[0],arr[1]);
                    }

                }
                bidsList.add(bidMap);
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bidsList;
    }

    /**
     * 获取MXC的CVT均价
     * @param bidsList
     * @return
     */
    public Double getAvg(List<Map<String,String>> bidsList){
        Double price = 0.0;
        Double quantity = 0.0;
        Double sum = 0.0;
        Double tmp_quantity = 0.0;
        Double avg = 0.0;
        for(int i=0;i<=bidsList.size()-1;i++){
            if(bidsList.get(i).get("price") != null){
                price = Double.valueOf(bidsList.get(i).get("price"));
            }
            if(bidsList.get(i).get("quantity") != null){
                quantity = Double.valueOf(bidsList.get(i).get("quantity"));
            }
            sum+=price*quantity;
            tmp_quantity+=quantity;
            if(tmp_quantity>=50000){
                break;
            }

        }
        avg = sum/tmp_quantity;
        return avg;
    }

    /**
     * 获取买单数据
     * @param restClient
     * @return
     */
    public List<Map<String,String>> getDepthData(ApiRestClient restClient,String trade){
        PublicMethod pb = new PublicMethod();
        JSONObject dataJSON = null;
        String[] bidArray = null;
        String bids = null;
        String data = null;
        Map<String,String> depthMap = new HashMap<String,String>();
        List<Map<String,String>> depthList = new ArrayList<Map<String,String>>();
        try {
            data = pb.getOrderData(restClient,trade);
            if(data != null){
                dataJSON = new JSONObject(data);
                if(dataJSON != null){
                    bids = dataJSON.get("bids").toString();
                    if(bids != null){
                        bidArray = bids.substring(1,bids.length()-1).split("],");
                        for(int i=0;i<=bidArray.length-1;i++){
                            String[] bidsArr = bidArray[i]
                                    .replaceAll("\\[","")
                                    .replaceAll("\\]","")
                                    .replaceAll("\"","")
                                    .split(",");
                            depthMap.put("price",bidsArr[0]);
                            depthMap.put("quantity",bidsArr[1]);
                            depthMap.put("sum",bidsArr[2]);
                            depthList.add(depthMap);
                            depthMap = new HashMap<String,String>();
                        }
                    }

                }


            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return depthList;

    }

    /**
     * 获取顶单状态
     * @param restClient
     * @param coinFrom
     * @param coinTo
     * @param type
     * @param price_btc
     * @param trade
     * @return
     */
    public String getTopOrderStatus(ApiRestClient restClient,String coinFrom, String coinTo, String type,Double price_btc,String trade){
        String buyStr = null;
        String status = null;
        try {
            PublicMethod pb = new PublicMethod();
            List<Map<String,String>> depthList = pb.getDepthData(restClient,trade);
            List<Map<String,String>> orderlist = pb.getOrderDetail(restClient,coinFrom,coinTo,type);
            Double dmp = 0.0;
            Double sum = 0.0;
            if(orderlist == null) return null;
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
                Double quantity = Double.valueOf(depthMap.get("quantity"));
                if(price>dmp){
                    sum+=quantity;
                }
            }

            String position = new BigDecimal(pb.getOrderPosition(restClient,coinFrom,trade,price_btc)).setScale(8,BigDecimal.ROUND_UP).toPlainString();
            String bj = null;
            String coin_buy_num_now = null;
            //根据上面的挂单触发顶单
            if(sum>=1000){
                //撤销当前委托单
                for(int i=0;i<=orderlist.size()-1;i++){
                    Map<String,String> orderMap = orderlist.get(i);
                    String id = orderMap.get("id");
                    String price = orderMap.get("price");
                    //撤销订单 price_coin*0.95-0.00000001
                    if(Double.valueOf(position)>Double.valueOf(price)){
                        restClient.cancelEntrustSheet(id);
                        Thread.sleep(3000);
                        bj="cx";
                    }
                    
                }
                if(bj != null){
                    //重新获取资产数量
                    Double btc_num_now = PublicMethod.getUserAssets(restClient,coinTo);
                    //获取个人冻结资产
                    Double btc_freez_now = pb.getUserNowFreezingAssets(restClient,coinFrom,coinTo,type);
                    //可以使用的资产数目
                    Double btc_num_ky_now = btc_num_now-btc_freez_now;
                    //可以挂单的数量
                    coin_buy_num_now = String.valueOf(Math.floor(btc_num_ky_now/Double.valueOf(position)));
                }

                if(coin_buy_num_now == null){
                    log.info("可以顶单数量  =================================> 0.0");
                }else{
                    log.info("顶单价格  =================================> "+position);
                    log.info("顶单数量  =================================> "+coin_buy_num_now);
                }
                //挂单
                if(coin_buy_num_now != null && Double.valueOf(coin_buy_num_now)>0.0){
                    buyStr = restClient.addEntrustSheet(trade,coin_buy_num_now,position, type);
                    buyStr = buyStr.replaceAll("\\{","")
                            .replaceAll("\\}","")
                            .replaceAll("\"","");
                    status = buyStr.split(",")[0].split(":")[1].toString();
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("报错日志================>"+e.getMessage());
        }
        return status;

    }




}
