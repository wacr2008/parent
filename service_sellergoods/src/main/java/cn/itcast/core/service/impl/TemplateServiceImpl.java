package cn.itcast.core.service.impl;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.service.TemplateService;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    TypeTemplateDao typeTemplateDao;

    @Autowired
    SpecificationOptionDao specificationOptionDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public PageResult findPage(TypeTemplate template, Integer page, Integer rows) {
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        for (TypeTemplate typeTemplate : typeTemplates) {
            //2. 模板id作为key, 对应的品牌集合作为value, 缓存入redis中
            String brandIdsJsonStr = typeTemplate.getBrandIds();
            List<Map> brandList = JSON.parseArray(brandIdsJsonStr, Map.class);
            //缓存品牌集合数据
            redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).put(typeTemplate.getId(), brandList);

            //3. 模板id作为key, 对应的规格集合作为value, 缓存入redis中
            List<Map> specList = findBySpecList(typeTemplate.getId());
            redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).put(typeTemplate.getId(), specList);
        }

        PageHelper.startPage(page, rows);
        TypeTemplateQuery templateQuery = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = templateQuery.createCriteria();
        if (template != null) {
            if (template.getName() != null && !"".equals(template.getName())) {
                criteria.andNameLike("%" + template.getName() + "%");
            }
        }
        Page<TypeTemplate> page1 = (Page<TypeTemplate>) typeTemplateDao.selectByExample(templateQuery);
        return new PageResult(page1.getTotal(), page1.getResult());
    }

    @Override
    public void add(TypeTemplate template) {
        typeTemplateDao.insertSelective(template);
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);

    }

    @Override
    public void update(TypeTemplate template) {
        typeTemplateDao.updateByPrimaryKeySelective(template);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map> findBySpecList(Long id) {

        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds();
        List<Map> mapList = JSON.parseArray(specIds, Map.class);
        if (mapList != null) {
            for (Map map : mapList) {
                long specId = Long.parseLong(String.valueOf(map.get("id")));
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
                criteria.andSpecIdEqualTo(specId);
                List<SpecificationOption> optionList = specificationOptionDao.selectByExample(specificationOptionQuery);
                map.put("options", optionList);

            }
        }

        return mapList;
    }

}
