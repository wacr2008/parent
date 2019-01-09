package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class uploadFileCntroller {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) {
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("I:\\pyg\\parent\\web_shop\\src\\main\\resources\\fastDFS\\fdfs_client.conf");
            String s = fastDFSClient.uploadFile(file.getBytes(), file.getOriginalFilename(), file.getSize());

            return new Result(true, FILE_SERVER_URL+s);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败!!!");
        }

    }

}
