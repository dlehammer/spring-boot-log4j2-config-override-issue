package reproducer.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reproducer.model.AdminLog;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty(name = "custom-admin-log.enabled", havingValue = "true")
public class SpringAdminLogger {

    private Logger log = LoggerFactory.getLogger(SpringAdminLogger.class);

    private final JsonMapper jsonMapper;

    public SpringAdminLogger(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void trace(AdminLog adminLog) {
        log.info("Tracing: {}", adminLog);

        AdminLogger.trace(jsonMapper.writeValueAsString(adminLog));
    }

}
