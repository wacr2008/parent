package cn.itcast.core.common;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoninController {
    @RequestMapping("/showName")
    public Map Login(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map   HashMap = new HashMap<>();
        HashMap.put("username", name);
        return HashMap;
    }
}
