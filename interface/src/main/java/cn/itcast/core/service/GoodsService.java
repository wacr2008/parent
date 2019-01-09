package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;

public interface GoodsService {
    void add(GoodsEntity goodsEntity);

    PageResult search(Goods goods, Integer page, Integer rows);

    GoodsEntity findOne(Long id);

    void update(GoodsEntity goodsEntity);

    void delete(Long id);

    void updateStatus(Long id, String status);

}
