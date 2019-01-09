package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.BuyerCart;

import java.util.List;

public interface CartService {

    List<BuyerCart> addGoodsToCartList(List<BuyerCart> cartList, Long itemId, Integer num);

    void setCartListToRedis(String userName, List<BuyerCart> cartList);

    List<BuyerCart> getCartListFromRedis(String userName);

    List<BuyerCart> mergeCookieCartListToRedisCartList(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList);

}
