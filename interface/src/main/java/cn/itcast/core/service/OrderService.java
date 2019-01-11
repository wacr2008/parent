package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.OrderEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;

/**
 * @author Administrator
 */
public interface OrderService {
    public void add(Order order);

    PayLog getPayLogByUserName(String userName);

    void updatePayStatus(String userName);

    //分页查询
    public PageResult search(OrderEntity orderEntity, Integer page, Integer rows);
}
