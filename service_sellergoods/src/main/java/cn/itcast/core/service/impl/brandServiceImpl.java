package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.service.brandService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class brandServiceImpl implements brandService {

    @Autowired
    BrandDao brandDao;

    @Override
    public List<Brand> brandFindAll() {
        List<Brand> brands = brandDao.selectByExample(null);
        return brands;
    }

    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
       PageHelper.startPage(pageNum, pageSize);
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        return   new PageResult(page.getTotal(),page.getResult());


    }

    @Override
    public void add(Brand brand) {
        brandDao.insert(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long aLong : ids) {

            brandDao.deleteByPrimaryKey(aLong);
        }
    }

    @Override
    public PageResult findPage(Brand brand, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        BrandQuery brandQuery=new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();

        if (brand!=null){
            if (brand.getName()!=null && brand.getName().length()>0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (brand.getFirstChar()!=null  && brand.getFirstChar().length()>0){
                criteria.andFirstCharEqualTo( brand.getFirstChar());
            }

        }

        Page<Brand> brands = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(brands.getTotal(),brands.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return brandDao.selectOptionList();

    }
}
