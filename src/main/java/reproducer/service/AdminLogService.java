package reproducer.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reproducer.logger.SpringAdminLogger;
import reproducer.model.AdminLog;

@ConditionalOnBean(SpringAdminLogger.class)
@Service
public class AdminLogService {

    private final SpringAdminLogger adminLogger;

    public AdminLogService(SpringAdminLogger adminLogger) {
        this.adminLogger = adminLogger;
    }

    public void log(String code, String context) {
        adminLogger.trace(new AdminLog(code, context));
    }

}

