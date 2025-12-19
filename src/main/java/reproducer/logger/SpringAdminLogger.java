package reproducer.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reproducer.model.AdminLog;

@Component
@ConditionalOnProperty(name = "custom-admin-log.enabled", havingValue = "true")
public class SpringAdminLogger {

    private Logger log = LoggerFactory.getLogger(SpringAdminLogger.class);

    private final ObjectMapper objectMapper;

    public SpringAdminLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void trace(AdminLog adminLog) {
        log.info("Tracing: {}", adminLog);

        try {
            AdminLogger.trace(objectMapper.writeValueAsString(adminLog));
        } catch (JsonProcessingException e) {
            log.error("Error writing: {}", adminLog, e);
        }
    }

}
