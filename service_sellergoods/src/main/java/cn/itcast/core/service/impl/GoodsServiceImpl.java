package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao descDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private SellerDao sellerDao;

    @Autowired
    private ItemCatDao catDao;

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQTopic topicPageAndSolrDestination;

    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;

    /**
     * 保存商品
     *
     * @param goodsEntity
     */
    @Override
    public void add(GoodsEntity goodsEntity) {
        //1. 保存商品表数据
        //初始化新增的商品状态为0, 未审核状态
        goodsEntity.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsEntity.getGoods());

        //2. 保存商品详情表数据
        //设置商品=表主键, 就是商品详情表主键
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        descDao.insertSelective(goodsEntity.getGoodsDesc());

        //3. 保存库存集合数据
        insertItem(goodsEntity);

        System.out.println(goodsEntity);

    }
    @Override
    public void updateStatus(final Long id, String status) {


                //1. 修改商品状态
                Goods goods = new Goods();
                goods.setId(id);
                goods.setAuditStatus(status);
                goodsDao.updateByPrimaryKeySelective(goods);

                //2. 修改库存状态
                Item item = new Item();
                item.setStatus(status);

                ItemQuery query = new ItemQuery();
                ItemQuery.Criteria criteria = query.createCriteria();
                criteria.andGoodsIdEqualTo(id);
                itemDao.updateByExampleSelective(item, query);

                //消息队列
         if ("1".equals(status)) {
                        jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                            @Override
                            public Message createMessage(Session session) throws JMSException {
                                Message message = session.createTextMessage(String.valueOf(id));
                                return message;
                            }
                        });
                    }


    }

    @Override
    public PageResult search(Goods goods, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if (goods != null) {
            if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName())) {

                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus())) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (goods.getSellerId() != null && !"".equals(goods.getSellerId())
                    && !"admin".equals(goods.getSellerId()) && !"wc".equals(goods.getSellerId())) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
        }

        Page<Goods> page1 = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(page1.getTotal(), page1.getResult());

    }

    @Override
    public GoodsEntity findOne(Long id) {
        /**
         *查询商品信息
         */
        Goods goods = goodsDao.selectByPrimaryKey(id);
        //查询商品描述
        GoodsDesc goodsDesc = descDao.selectByPrimaryKey(id);
        //查询商品库存
        ItemQuery query=new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(query);

        GoodsEntity goodsEntity=new GoodsEntity();
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(items);



        return goodsEntity;
    }

    @Override
    public void update(GoodsEntity goodsEntity) {
        goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());
        descDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());

        Item item=new Item();
        ItemQuery itemQuery=new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(goodsEntity.getGoods().getId());
        itemDao.deleteByExample(itemQuery);
        insertItem(goodsEntity);

    }

    @Override
    public void delete(final Long  id) {

                //根据商品id对商品表做逻辑删除
                Goods goods = new Goods();
                goods.setId(id);
                goods.setIsDelete("1");
                goodsDao.updateByPrimaryKeySelective(goods) ;
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        Message message = session.createTextMessage(String.valueOf(id));
                        return message;
                    }
                });



    }

    /**
     * 设置item库存对象属性值
     *
     * @return
     */
    private Item setItemValue(GoodsEntity goodsEntity, Item item) {

        //库存状态, 默认为0未审核
        item.setStatus("0");
        //设置对应的商品id
        item.setGoodsId(goodsEntity.getGoods().getId());
        //卖家名称
        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        item.setSeller(seller.getName());

        //创建时间
        item.setCreateTime(new Date());
        //修改时间
        item.setUpdateTime(new Date());
        //分类id, 使用商品中的第三级分类作为这里的分类id和分类名称
        item.setCategoryid(goodsEntity.getGoods().getCategory3Id());
        //分类名称
        ItemCat itemCat = catDao.selectByPrimaryKey(goodsEntity.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());

        //商品示例图片
        String itemImages = goodsEntity.getGoodsDesc().getItemImages();
        if (itemImages != null) {
            List<Map> maps = JSON.parseArray(itemImages, Map.class);
            if (maps != null && maps.size() > 0) {
                String url = String.valueOf(maps.get(0).get("url"));
                item.setImage(url);
            }
        }
        return item;
    }

    /**
     * 插入库存数据
     */
    public void insertItem(GoodsEntity goodsEntity) {
        if ("1".equals(goodsEntity.getGoods().getIsEnableSpec())) {
            //3. 遍历库存集合, 保存库存集合数据
            if (goodsEntity.getItemList() != null) {
                for (Item item : goodsEntity.getItemList()) {
                    //库存数据标题, 商品名称 + 具体规格组成商品库存标题, 目的是为了消费者搜索的时候搜索的更精确
                    String title = goodsEntity.getGoods().getGoodsName();
                    //获取库存对象中的规格json字符串
                    //例如: {"机身内存":"16G","网络":"联通3G"}
                    String specJsonStr = item.getSpec();
                    //将json字符串转换成一个实体类对象
                    Map specMap = JSON.parseObject(specJsonStr, Map.class);
                    if (specMap != null) {
                        Collection<String> values = specMap.values();
                        for (String value : values) {
                            title += " " + value;
                        }
                    }
                    //拼接完后里面的数据是  iphone8 16G 联通3G
                    item.setTitle(title);

                    //设置item对象属性值
                    setItemValue(goodsEntity, item);

                    itemDao.insertSelective(item);
                }
            }
        } else {
            //3. 如果页面没有勾选规格, 初始化一条库存数据
            Item item = new Item();
            //设置库存标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());
            //设置规格
            item.setSpec("{}");
            //设置默认库存量
            item.setNum(0);
            //设置默认价格
            item.setPrice(new BigDecimal("999999999"));

            //设置item对象属性值
            setItemValue(goodsEntity, item);
            itemDao.insertSelective(item);

        }
    }
}
