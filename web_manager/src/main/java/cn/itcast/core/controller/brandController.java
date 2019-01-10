package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.brandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class brandController {

    @Reference
    brandService brandService;
     @RequestMapping("/findAll")
    public List brandFindAll(){

         List<Brand> brands = brandService.brandFindAll();
         return brands;
     }



    @RequestMapping("/findPage")
     public PageResult findPage(Integer page , Integer rows){
        return   brandService.findPage(page, rows);


     }
    @RequestMapping("/save")
    public Result add(@RequestBody Brand brand){

        try {
            brandService.add(brand);
            return new Result(true,"添加成功！！！");
        } catch (Exception e) {

            e.printStackTrace();
            return new Result(false,"添加失败！！！");
        }

    }

    @RequestMapping("/findOne")
    public Brand findOne(Long id){
        return   brandService.findOne(id);

    }

    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){

        try {
            brandService.update(brand);
            return new Result(true,"修改成功！！！");
        } catch (Exception e) {

            e.printStackTrace();
            return new Result(false,"修改失败！！！");
        }

    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功！！！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败！！！");
        }

    }

    @RequestMapping("/search")
    public PageResult search(@RequestBody Brand brand, Integer page , Integer rows){
        System.out.println(brand);
        System.out.println();
        return  brandService.findPage(brand,page,rows);

    }
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();

    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            if (ids != null) {
                for (Long id : ids) {
                    brandService.updateStatus(id, status);
                }
            }
            return new Result(true, "审核成功!!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "审核失败!!");
        }
    }

}
