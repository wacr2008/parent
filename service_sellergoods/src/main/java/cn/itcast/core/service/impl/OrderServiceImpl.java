package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.OrderEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.ZheXianTuEntity;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderItemQuery;
import cn.itcast.core.pojo.order.OrderQuery;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import cn.itcast.core.service.OrderService;
import cn.itcast.core.util.DateUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

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
                criteria.andCreateTimeEqualTo(orderEntity.getDate());
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

    @Override
    public Map statistics(ZheXianTuEntity zheXianTuEntity) {
        Map map = new HashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        OrderQuery orderQuery = new OrderQuery();
        OrderQuery.Criteria criteria = orderQuery.createCriteria();
        //判断传入参数是否为空
        if (zheXianTuEntity != null) {
            //根据商家ID查
            if (zheXianTuEntity.getSellerName() != null && !"".equals(zheXianTuEntity.getSellerName())) {
                String sellerId = getSellerId(zheXianTuEntity.getSellerName());
                criteria.andSellerIdEqualTo(sellerId);
            }
            List<Integer> integers = new ArrayList<>();
            List<String> dates = new ArrayList<>();
            //根据传入事假查
            if (zheXianTuEntity.getDates() != null && zheXianTuEntity.getDates().length >= 2) {
                Date startDate = zheXianTuEntity.getDates()[0];
                Date entDate = zheXianTuEntity.getDates()[1];
                Date tmpDate = new Date();
                for (int i = 1; tmpDate == entDate; i++) {
                    Date qian = getQian(startDate, i);
                    tmpDate = qian;
                    criteria.andPaymentTimeEqualTo(qian);
                    int i1 = orderDao.countByExample(orderQuery);
                    dates.add(sdf.format(qian));
                    integers.add(i1);
                }
            }
            map.put("data", dates);
            map.put("count", integers);
            return map;
        }
        List<Integer> integers = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        Date date = new Date();
        //没有时间传入则查取前一周的数据
        for (int i = -6; i <= 0; i++) {
            Date qian = getQian(date, i);
            criteria.andPaymentTimeEqualTo(qian);
            int i1 = orderDao.countByExample(orderQuery);
            dates.add(sdf.format(qian));
            integers.add(i1);
        }
        map.put("data", dates.toArray());
        map.put("count", integers.toArray());
        return map;
    }

    /**
     * 根据商家名求商家ID
     * @param sellerName
     * @return
     */
    private String getSellerId(String sellerName) {
        SellerQuery query = new SellerQuery();
        SellerQuery.Criteria criteria = query.createCriteria();
        criteria.andNameEqualTo(sellerName);
        List<Seller> sellers = sellerDao.selectByExample(query);
        if (sellers.size() > 1) {
            String name = sellers.get(0).getName();
            return name;
        }
        return null;
    }

    /**
     * 根据传来的时间计算前几日和后日
     * @param date
     * @param i 传入正值求后几日
     * @return
     */
    private Date getQian(Date date, int i) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, i);
        date = calendar.getTime();
        return date;
    }

}
