package cn.itcast.core.service.impl;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    SpecificationDao specificationDao;

    @Autowired
    SpecificationOptionDao specificationOptionDao;




    @Override
    public PageResult search(Specification spec, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        SpecificationQuery specificationQuery=new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (spec != null){
            if (spec.getSpecName() !=null && !"".equals(spec.getSpecName())){
                criteria.andSpecNameLike("%"+spec.getSpecName()+"%");
            }
        }
        Page<Specification> specificationPage=( Page<Specification>) specificationDao.selectByExample(specificationQuery);
        return new PageResult(specificationPage.getTotal(),specificationPage.getResult());
    }

    @Override
    public void add(SpecEntity specEntity) {
        /**
         * 添加主表数据
         */
        specificationDao.insertSelective(specEntity.getSpecification());

        /**
         * 添加从表数据
         */
        List<SpecificationOption> list = specEntity.getSpecificationOptionList();
        if (list !=null){
            for (SpecificationOption specificationOption : list) {
                specificationOption.setSpecId(specEntity.getSpecification().getId());
                specificationOptionDao.insertSelective(specificationOption);
                }
            }

    }

    @Override
    public SpecEntity findOne(Long id) {
        Specification specification = specificationDao.selectByPrimaryKey(id);
        SpecificationOptionQuery specificationOptionQuery=new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);

        return new SpecEntity(specification,specificationOptions);
    }

    @Override
    public void update(SpecEntity spec) {
        /**
         * 更新主表数据
         */
        specificationDao.updateByPrimaryKeySelective(spec.getSpecification());

        /**
         * 删除从表数据
         */
        SpecificationOptionQuery specificationOptionQuery=new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(spec.getSpecification().getId());
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        /**
         * 重新添加从表数据
         */
        List<SpecificationOption> list = spec.getSpecificationOptionList();
        if (list !=null){
            for (SpecificationOption specificationOption : list) {
                specificationOption.setSpecId(spec.getSpecification().getId());
                specificationOptionDao.insertSelective(specificationOption);
            }
        }
    }

    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                /**
                 * 删除主表数据
                 */
                specificationDao.deleteByPrimaryKey(id);
                /**
                 *
                 */
                SpecificationOptionQuery specificationOptionQuery=new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
                criteria.andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(specificationOptionQuery);
            }



        }
    }

    @Override
    public List<Map> selectOptionList() {
       return specificationDao.selectOptionList();
    }

}
