package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.BuyerCart;
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
import cn.itcast.core.util.Constants;
import cn.itcast.core.util.DateUtils;
import cn.itcast.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private PayLogDao payLogDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao goodsDescDao;

    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add(Order order) {
        //获取当前登录用户的用户名
        String userId = order.getUserId();
        //根据用户名到redis中获取当前用户的购物车集合
        List<BuyerCart> cartList = (List<BuyerCart>) redisTemplate.boundHashOps(Constants.CART_LIST_REDIS).get(userId);

        List<String> orderIdList = new ArrayList();//订单ID列表
        double total_money = 0;//总金额 （元）

        if (cartList != null) {
            //1. 遍历购物车集合
            for (BuyerCart cart : cartList) {
                // 2. 根据购物车来形成订单记录
                long orderId = idWorker.nextId();
                System.out.println("sellerId:" + cart.getSellerId());
                Order tborder = new Order();//新创建订单对象
                tborder.setOrderId(orderId);//订单ID
                tborder.setUserId(order.getUserId());//用户名
                tborder.setPaymentType(order.getPaymentType());//支付类型
                tborder.setStatus("1");//状态：未付款
                tborder.setCreateTime(new Date());//订单创建日期
                tborder.setUpdateTime(new Date());//订单更新日期
                tborder.setReceiverAreaName(order.getReceiverAreaName());//地址
                tborder.setReceiverMobile(order.getReceiverMobile());//手机号
                tborder.setReceiver(order.getReceiver());//收货人
                tborder.setSourceType(order.getSourceType());//订单来源
                tborder.setSellerId(cart.getSellerId());//商家ID
                //循环购物车明细
                double money = 0;

                //3. 从购物车中获取订单明细集合
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if (orderItemList != null) {
                    //4. 遍历购物明细集合
                    for (OrderItem orderItem : orderItemList) {
                        //5.根据购物明细对象形成订单详情记录
                        orderItem.setId(idWorker.nextId());
                        orderItem.setOrderId(orderId);//订单ID
                        orderItem.setSellerId(cart.getSellerId());
                        money += orderItem.getTotalFee().doubleValue();//金额累加
                        orderItemDao.insertSelective(orderItem);

                    }
                }
                tborder.setPayment(new BigDecimal(money));
                orderDao.insertSelective(tborder);
                //添加到订单列表
                orderIdList.add(orderId + "");
                //累加到总金额
                total_money += money;

            }

            // 6. 计算所有购物车中的总价钱, 形成支付日志记录
            if ("1".equals(order.getPaymentType())) {//如果是微信支付
                PayLog payLog = new PayLog();
                String outTradeNo = idWorker.nextId() + "";//支付订单号
                payLog.setOutTradeNo(outTradeNo);//支付订单号
                payLog.setCreateTime(new Date());//创建时间
                //订单号列表，逗号分隔
                String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
                payLog.setOrderList(ids);//订单号列表，逗号分隔
                payLog.setPayType("1");//支付类型
                payLog.setTotalFee((long) (total_money * 100));//总金额(分)
                payLog.setTradeState("0");//支付状态
                payLog.setUserId(order.getUserId());//用户ID
                payLogDao.insertSelective(payLog);//插入到支付日志表
                redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存
            }
            redisTemplate.boundHashOps("cartList").delete(order.getUserId());

        }

    }

    @Override
    public PayLog getPayLogByUserName(String userName) {
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(userName);
        return payLog;
    }

    @Override
    public void updatePayStatus(String userName) {
        //1. 根据用户名获取redis中的支付日志对象
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(userName);

        if (payLog != null) {
            //2. 更改支付日志表中的支付状态为已支付
            payLog.setTradeState("1");
            payLogDao.updateByPrimaryKeySelective(payLog);

            //3. 更改订单表中的支付状态
            String orderListStr = payLog.getOrderList();
            String[] split = orderListStr.split(",");
            if (split != null) {
                for (String orderId : split) {
                    Order order = new Order();
                    order.setOrderId(Long.parseLong(orderId));
                    order.setStatus("1");
                    orderDao.updateByPrimaryKeySelective(order);
                }
            }

            //4. 删除redis中的支付日志数据
            redisTemplate.boundHashOps("payLog").delete(userName);
        }
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
