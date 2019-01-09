package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.seller.Seller;

import java.util.List;

public interface SellerService {
    void add(Seller seller);

    Seller findOne(String id);

    void updateStatus(String sellerId, String status);

    PageResult findPage(Seller seller, Integer page, Integer rows);


}
