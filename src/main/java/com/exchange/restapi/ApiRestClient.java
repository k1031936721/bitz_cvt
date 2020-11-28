package com.exchange.restapi;


import com.exchange.utils.*;

import java.util.HashMap;
import java.util.Map;


public class ApiRestClient {

    private String apiKey;
    private String secretKey;
    private String tradePwd;

    private String WEB_BASE = ""; //

    /**
     * ------------------------------------------------------------------
     * @param api_server        必须
     * @param api_key        必须
     * @param secret_key     必须
     * ------------------------------------------------------------------
     */
    public ApiRestClient(String api_server, String api_key,String secret_key){
        this.WEB_BASE = api_server;
        this.apiKey = api_key;
        this.secretKey = secret_key;
        this.tradePwd = "";
    }
    /**
     * ------------------------------------------------------------------
     * @param api_server        必须
     * @param api_key        必须
     * @param secret_key     必须
     * @param tradePwd      需要下单交易时必须传（addEntrustSheet）
     * ------------------------------------------------------------------
     */
    public ApiRestClient(String api_server, String api_key,String secret_key, String tradePwd){
        this.WEB_BASE = api_server;
        this.apiKey = api_key;
        this.secretKey = secret_key;
        this.tradePwd = tradePwd;
    }

    /**
     *
     * @param path
     * @param params
     * @return
     */
    private String market_api(String path, Map<String,String> params){
        String result = "";
        String strParams = SignUtil.createLinkString(params);
        try{
            result = HttpUtilManager.getInstance().requestHttpGet(WEB_BASE, path, strParams);
        }catch(Exception e){
            result = "";
        }
        return result;
    }
    private String sign_api(String path, Map<String,String> params){
        String result = "";
        //
        long timeStamp = System.currentTimeMillis();
        timeStamp = timeStamp / 1000;
        params.put("apiKey", this.apiKey);
        params.put("timeStamp", String.valueOf(timeStamp));
        params.put("nonce", String.valueOf(timeStamp).substring(0,6));
        String sign = SignUtil.buildSign(params,this.secretKey);
        params.put("sign",sign);
        //
        try{
            result = HttpUtilManager.getInstance().requestHttpPost(WEB_BASE, path, params);
        }catch(Exception e){

        }
        return result;
    }
    //-------------------交易接口-----------------------------------------
    /**
     * ------------------------------------------------------------------
     * 个人资产 (Get user open orders)
     * ------------------------------------------------------------------
     */
    public String getUserAssets(){
        Map<String, String> params = new HashMap<String, String>();
        return this.sign_api("/Assets/getUserAssets",params);
    }
    /**
     * ------------------------------------------------------------------
     * 获取个人当前委托单列表 (Get user open orders)
     * @parame coinFrom	eth
     * @parame coinTo	btc
     * @parame type		integer	1:买,2:卖
     * ------------------------------------------------------------------
     */
    public String getUserNowEntrustSheet(String coinFrom,String coinTo,String type){
        Map<String, String> params = new HashMap<String, String>();
        params.put("coinFrom", coinFrom);
        params.put("coinTo", coinTo);
        params.put("type", type);
        return this.sign_api("/Trade/getUserNowEntrustSheet",params);
    }
    public String getUserNowEntrustSheet(String coinFrom,String coinTo,String type,String page,String pageSize){
        Map<String, String> params = new HashMap<String, String>();
        params.put("coinFrom", coinFrom);
        params.put("coinTo", coinTo);
        params.put("type", type);
        params.put("page", page);
        params.put("pageSize", pageSize);
        return this.sign_api("/Trade/getUserNowEntrustSheet",params);
    }
    /**
     * ------------------------------------------------------------------
     * 获取个人历史委托单列表 (Get user history entrust)
     * ------------------------------------------------------------------
     */
    public String getUserHistoryEntrustSheet(String coinFrom,String coinTo,String type){
        Map<String, String> params = new HashMap<String, String>();
        params.put("coinFrom", coinFrom);
        params.put("coinTo", coinTo);
        params.put("type", type);
        return this.sign_api("/Trade/getUserHistoryEntrustSheet",params);
    }
    public String getUserHistoryEntrustSheet(String coinFrom,String coinTo,String type,String page,String pageSize){
        Map<String, String> params = new HashMap<String, String>();
        params.put("coinFrom", coinFrom);
        params.put("coinTo", coinTo);
        params.put("type", type);
        params.put("page", page);
        params.put("pageSize", pageSize);
        return this.sign_api("/Trade/getUserHistoryEntrustSheet",params);
    }
    /**
     * ------------------------------------------------------------------
     * 提交委托单 (Place an order)
     * @param symbol        string "eth_btc"
     * @param number        float
     * @param price         float
     * @param type          string  "1":"buy"   "2":"sale"
     * ------------------------------------------------------------------
     */
    public String addEntrustSheet(String symbol,String number,String price , String type){
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbol);
        params.put("number", number);
        params.put("price", price);
        params.put("type", type);
        params.put("tradePwd", this.tradePwd); // 委托单 必须 传递 tradePwd 交易密码
        return this.sign_api("/Trade/addEntrustSheet",params);
    }

    /**
     * 提交市价委托单
     * @param symbol
     * @param total
     * @param type
     * @return
     */
    public String marketTrade(String symbol,String total , String type){
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbol);
        params.put("total", total);
        params.put("type", type);
        return this.sign_api("/Trade/MarketTrade",params);
    }

    /**
     * 批量提交限价委托单
     * @param tradeData
     * @return
     */
    public String addEntrustSheetBatch(String tradeData){
        Map<String, String> params = new HashMap<String, String>();
        params.put("tradeData", tradeData);
        return this.sign_api("/Trade/addEntrustSheetBatch",params);
    }
    /**
     * ------------------------------------------------------------------
     * 提交委托单详情 (Get the detail of an order)
     * @param entrustSheetId    string
     * ------------------------------------------------------------------
     */
    public String getEntrustSheetInfo(String entrustSheetId){
        Map<String, String> params = new HashMap<String, String>();
        params.put("entrustSheetId", entrustSheetId);
        return this.sign_api("/Trade/getEntrustSheetInfo",params);
    }
    /**
     * ------------------------------------------------------------------
     * 撤销委托单 (Cancel the order)
     * @param entrustSheetId    string
     * ------------------------------------------------------------------
     */
    public String cancelEntrustSheet(String entrustSheetId){
        Map<String, String> params = new HashMap<String, String>();
        params.put("entrustSheetId", entrustSheetId);
        return this.sign_api("/Trade/cancelEntrustSheet",params);
    }
    /**
     * ------------------------------------------------------------------
     * 批量撤销委托单 (cancel the all entrust)
     * @param entrustSheetIds    string     "id1,id2,id3"
     * ------------------------------------------------------------------
     */
    public String cancelAllEntrustSheet(String entrustSheetIds){
        Map<String, String> params = new HashMap<String, String>();
        params.put("ids", entrustSheetIds);
        return this.sign_api("/Trade/cancelAllEntrustSheet",params);
    }

    /**
     * 获取当前仓位
     * @param contractId
     * @return
     */
    public String getContractActivePositions(String contractId){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId", contractId);
        return this.sign_api("/Contract/getContractActivePositions",params);
    }
    //-------------------行情接口-----------------------------------------
    /**
     * ------------------------------------------------------------------
     * 获取牌价数据 (Get the price data)
     * @param symbol    eth_btc
     * ------------------------------------------------------------------
     */
    public String ticker(String symbol){
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbol);
        return this.market_api("/Market/ticker",params);
    }
    /**
     * ------------------------------------------------------------------
     * 获取所有牌价数据 (Get the price of all symbol)
     * ------------------------------------------------------------------
     */
    public String tickerall(){
        Map<String, String> params = new HashMap<String, String>();
        return this.market_api("/Market/tickerall",params);
    }
    /**
     * ------------------------------------------------------------------
     * 获取最新交易记录 (Get the last orders)
     * @param symbol    eth_btc
     * ------------------------------------------------------------------
     */
    public String orders(String symbol){
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbol);
        return this.market_api("/Market/order",params);
    }
    /**
     * ------------------------------------------------------------------
     * 获取深度数据 (Get depth data)
     * @param symbol    eth_btc
     * ------------------------------------------------------------------
     */
    public String depth(String symbol){
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbol);
        return this.market_api("/Market/depth",params);
    }
    /**
     * ------------------------------------------------------------------
     * 获取深度数据 (Get depth data)
     * @param symbol        string eth_btc
     * @param resolution    string [1min 、5min 、15min 、30min 、60min、 4hour 、 1day 、5day 、1week、 1mon]
     * ------------------------------------------------------------------
     */
    public String kline(String symbol ,String resolution ){
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbol);
        params.put("resolution", resolution);
        return this.market_api("/Market/kline",params);
    }
    public String kline(String symbol ,String resolution ,int size){
        // 构造参数签名
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbol);
        params.put("resolution", resolution);
        params.put("size", String.valueOf(size));
        return this.market_api("/Market/kline",params);
    }
    /**
     * ------------------------------------------------------------------
     * 获取所有交易对的详细信息 (Get the detail of erery symbol)
     * ------------------------------------------------------------------
     */
    public String symbolList(){
        Map<String, String> params = new HashMap<String, String>();
        return this.market_api("/Market/symbolList",params);
    }

    /**
     * 获取合约交易市场列表
     * @param contractId
     * @return
     */
    public String getContractCoin(String contractId){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        return this.market_api("/Market/getContractCoin",params);
    }

    /**
     * 获取合约K线数据
     * @param contractId
     * @param type
     * @param size
     * @return
     */
    public String getContractKline(String contractId,String type,String size){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        params.put("type",type);
        params.put("size",size);
        return this.market_api("/Market/getContractKline",params);
    }

    /**
     * 获取合约交易的市场深度
     * @param contractId
     * @param depth
     * @return
     */
    public String getContractOrderBook(String contractId,String depth){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        params.put("depth",depth);
        return this.market_api("/Market/getContractOrderBook",params);
    }

    /**
     * 获取合约交易的成交历史
     * @param contractId
     * @param pageSize
     * @return
     */
    public String getContractTradesHistory(String contractId,String pageSize){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        params.put("pageSize",pageSize);
        return this.market_api("/Market/getContractTradesHistory",params);
    }

    /**
     * 获取合约交易最新行情
     * @param contractId
     * @return
     */
    public String getContractTickers(String contractId){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        return this.market_api("/Market/getContractTickers",params);
    }

    /**
     * 获取当前法币汇率信息
     * @param symbols
     * @return
     */
    public String currencyRate(String symbols){
        Map<String, String> params = new HashMap<String, String>();
        params.put("symbols",symbols);
        return this.market_api("/Market/currencyRate",params);
    }

    /**
     * 获取虚拟货币法币汇率信息
     * @param coins
     * @return
     */
    public String currencyCoinRate(String coins){
        Map<String, String> params = new HashMap<String, String>();
        params.put("coins",coins);
        return this.market_api("/Market/currencyCoinRate",params);
    }

    /**
     * 获取币种对应汇率信息
     * @param coins
     * @return
     */
    public String coinRate(String coins){
        Map<String, String> params = new HashMap<String, String>();
        params.put("coins",coins);
        return this.market_api("/Market/coinRate",params);
    }

    /**
     * 获取服务器当前时间
     * @return
     */
    public String getServerTime(){
        Map<String, String> params = new HashMap<String, String>();
        return this.market_api("/Market/getServerTime",params);
    }

    /**
     * 获取合约账户权益(资产)
     * @return
     */
    public String getContractAccountInfo(){
        Map<String, String> params = new HashMap<String, String>();
        return this.sign_api("/Contract/getContractAccountInfo",params);
    }

    /**
     * 获取已平仓位列表
     * @param contractId
     * @param page
     * @param pageSize
     * @return
     */
    public String getContractMyPositions(String contractId,String page,String pageSize){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        params.put("page",page);
        params.put("pageSize",pageSize);
        return this.sign_api("/Contract/getContractMyPositions",params);
    }

    /**
     * 获取单个或多个委托单明细
     * @param entrustSheetIds
     * @return
     */
    public String getContractOrderResult(String entrustSheetIds){
        Map<String, String> params = new HashMap<String, String>();
        params.put("entrustSheetIds",entrustSheetIds);
        return this.sign_api("/Contract/getContractOrderResult",params);
    }

    /**
     * 获取我的活动委托
     * @param contractId
     * @return
     */
    public String getContractOrder(String contractId){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        return this.sign_api("/Contract/getContractOrder",params);
    }

    /**
     * 获取某个委托的成交明细
     * @param entrustSheetId
     * @param page
     * @param pageSize
     * @return
     */
    public String getContractTradeResult(String entrustSheetId,String page,String pageSize){
        Map<String, String> params = new HashMap<String, String>();
        params.put("entrustSheetId",entrustSheetId);
        params.put("page",page);
        params.put("pageSize",pageSize);
        return this.sign_api("/Contract/getContractTradeResult",params);
    }

    /**
     * 获取我的委托历史
     * @param contractId
     * @param page
     * @param pageSize
     * @return
     */
    public String getContractMyHistoryTrade(String contractId,String page,String pageSize){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        params.put("page",page);
        params.put("pageSize",pageSize);
        return this.sign_api("/Contract/getContractMyHistoryTrade",params);
    }

    /**
     * 获取我的成交历史
     * @param contractId
     * @param page
     * @param pageSize
     * @param createDate
     * @return
     */
    public String getContractMyTrades(String contractId,String page,String pageSize,String createDate){
        Map<String, String> params = new HashMap<String, String>();
        params.put("contractId",contractId);
        params.put("page",page);
        params.put("pageSize",pageSize);
        params.put("createDate",createDate);
        return this.sign_api("/Contract/getContractMyTrades",params);
    }





}
