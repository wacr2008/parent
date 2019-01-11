package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import cn.itcast.core.service.brandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    SpecificationService specificationService;

    /**
     * 增强搜索
     * @param spec
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Specification spec, Integer page , Integer rows ){
       return specificationService.search(spec,page,rows);
    }
    @RequestMapping("/add")
    public Result add(@RequestBody SpecEntity spec){
        try {
            specificationService.add(spec);
            return new Result(true,"添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败!");
        }
    }

  @RequestMapping("/findOne")
    public  SpecEntity findOne(Long id){
       return specificationService.findOne(id);
  }

    @RequestMapping("/update")
    public  Result update(@RequestBody SpecEntity spec){
        try {
            specificationService.update(spec);
            return new Result(true,"修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败!");
        }
    }
    @RequestMapping("/delete")
    public  Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败!");
        }
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return specificationService.selectOptionList();

    }


    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            if (ids != null) {
                for (Long id : ids) {
                    specificationService.updateStatus(id, status);
                }
            }
            return new Result(true, "审核成功!!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "审核失败!!");
        }
    }



}
