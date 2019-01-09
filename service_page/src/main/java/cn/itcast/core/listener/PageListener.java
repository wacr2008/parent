package cn.itcast.core.listener;

import cn.itcast.core.service.CmsService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

public class PageListener implements MessageListener {

    @Autowired
    private CmsService cmsService;

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm=(ActiveMQTextMessage)message;
        try {
            String text = atm.getText();
            Map<String, Object> dataById = cmsService.findDataById(Long.parseLong(text));
            cmsService.createStaticPage(dataById,Long.parseLong(text));
        } catch ( Exception e) {
            e.printStackTrace();
        }
        /*cmsService.findDataById();
        cmsService.createStaticPage(, );*/
    }
}
