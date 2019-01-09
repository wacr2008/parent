package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CmsServiceImpl implements CmsService, ServletContextAware {

    @Autowired
    GoodsDao goodsDao;

    @Autowired
    GoodsDescDao goodsDescDao;

    @Autowired
    ItemDao itemDao;

    @Autowired
    ItemCatDao itemCatDao;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;

    private ServletContext servletContext;

    @Override
    public void createStaticPage(Map<String, Object> map, Long id) throws IOException, TemplateException {
        Configuration configuration = freemarkerConfig.getConfiguration();
        Template template = configuration.getTemplate("item.ftl");
        String path = id+".html";
       String realpath=getPath(path);
        Writer writer = new OutputStreamWriter(new FileOutputStream(new File(realpath)),"utf-8");
        template.process(map, writer);
        writer.close();
    }

    private String getPath(String path) {
        return servletContext.getRealPath(path);

    }

    @Override
    public Map<String, Object> findDataById(Long id) {
        Goods goods = goodsDao.selectByPrimaryKey(id);
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        Map<String, Object> map = new HashMap<>();
        if (goods != null) {
            ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
            ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
            ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());

            map.put("itemCat1", itemCat1);
            map.put("itemCat2", itemCat2);
            map.put("itemCat3", itemCat3);

        }
        map.put("goods", goods);
        map.put("goodsDesc", goodsDesc);
        map.put("itemList", itemList);
        return map;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
