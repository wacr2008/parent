package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.OrderEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Reference
    private OrderService orderService;

    @RequestMapping("/search")
    public PageResult search(@RequestBody OrderEntity orderEntity, Integer page, Integer rows) {
        PageResult page1 = orderService.search(orderEntity, page, rows);
        return page1;
    }


}
