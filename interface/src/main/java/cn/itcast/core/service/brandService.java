package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface brandService {
    List<Brand> brandFindAll();
    PageResult findPage(Integer pageNum, Integer pageSize);
    void add(Brand brand);
    Brand findOne( Long id);
    void update(Brand brand);
    void delete(Long[] ids);
    PageResult findPage(Brand brand, int pageNum,int pageSize);
    void updateStatus(Long id,String status);
    List<Map> selectOptionList();
}
