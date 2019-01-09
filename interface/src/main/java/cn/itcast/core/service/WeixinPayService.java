package cn.itcast.core.service;

import java.util.Map;

public interface WeixinPayService {
    Map createNative(String outTradeNo, String totalFee);

    Map<String, String> queryPayStatus(String out_trade_no);
}
