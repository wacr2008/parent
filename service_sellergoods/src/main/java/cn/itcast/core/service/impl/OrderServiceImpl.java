package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.OrderEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderItemQuery;
import cn.itcast.core.pojo.order.OrderQuery;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.OrderService;
import cn.itcast.core.util.DateUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao goodsDescDao;

    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add(Order order) {

    }

    @Override
    public PayLog getPayLogByUserName(String userName) {
        return null;
    }

    @Override
    public void updatePayStatus(String userName) {

    }

    @Override
    public PageResult search(OrderEntity orderEntity, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        OrderQuery orderQuery = new OrderQuery();
        OrderQuery.Criteria criteria = orderQuery.createCriteria();
        if (orderEntity != null) {
            //支付状态
            if (orderEntity.getStatus() != null && !"".equals(orderEntity.getStatus())) {
                criteria.andPostFeeEqualTo(orderEntity.getStatus());
            }
            //根据订单时间查询
            if (orderEntity.getFinishedDate() != null && !"".equals(orderEntity.getFinishedDate())) {
                Date date = new Date();
                if (orderEntity.getFinishedDate().equals("1")) {
                    criteria.andCreateTimeEqualTo(date);
                }
                if (orderEntity.getFinishedDate().equals("2")) {
                    Date[] weekStartAndEndDate = DateUtils.getWeekStartAndEndDate(date);
                    criteria.andCreateTimeBetween(weekStartAndEndDate[0], weekStartAndEndDate[1]);
                }
                if (orderEntity.getFinishedDate().equals("3")) {
                    Date[] monthStartAndEndDate = DateUtils.getMonthStartAndEndDate(date);
                    criteria.andCreateTimeBetween(monthStartAndEndDate[0], monthStartAndEndDate[1]);
                }
            }
            if (orderEntity.getDate() != null && !"".equals(orderEntity.getDate())) {

            }
        }
        Page<Order> page1 = (Page<Order>) orderDao.selectByExample(orderQuery);

        List<Order> result = page1.getResult();
        List<Order> list = new ArrayList<>();
        for (Order order : result) {
            //根据订单号查询对应的订单详情
            OrderItemQuery orderItemQuery = new OrderItemQuery();
            OrderItemQuery.Criteria orderItemQueryCriteria = orderItemQuery.createCriteria();
            orderItemQueryCriteria.andOrderIdEqualTo(order.getOrderId());
            List<OrderItem> orderItems = orderItemDao.selectByExample(orderItemQuery);
            //遍历订单详情集合  根据商品id查询商品和商品详情对象
            for (OrderItem orderItem : orderItems) {
                GoodsQuery query = new GoodsQuery();
                GoodsQuery.Criteria queryCriteria = query.createCriteria();
                queryCriteria.andIdEqualTo(orderItem.getGoodsId());
                Goods goods = goodsDao.selectByPrimaryKey(orderItem.getGoodsId());
                GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(orderItem.getGoodsId());
                //根据商户id查询商户对象
                Seller seller = sellerDao.selectByPrimaryKey(orderItem.getSellerId());
                goods.setGoodsDesc(goodsDesc);
                orderItem.setSellerName(seller.getName());
                orderItem.setGoods(goods);
            }
            order.setOrderItem(orderItems);
            list.add(order);
        }
        return new PageResult(page1.getTotal(), list);
    }

}
