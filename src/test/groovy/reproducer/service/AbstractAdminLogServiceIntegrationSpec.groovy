package reproducer.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import reproducer.logger.AdminLogger
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ActiveProfiles('integration-test')
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractAdminLogServiceIntegrationSpec extends Specification {

    @Autowired
    AdminLogService adminLogService

    void 'admin log - ok'() {
        given:
            String context = "ctx-${UUID.randomUUID()}"

        when:
            adminLogService.log('tc1', context)

        then:
            new PollingConditions().eventually {
                adminLogFile.readLines().findAll { it == "CUSTOM_ADMIN_LOG: {\"code\":\"tc1\",\"context\":\"${context}\"}" }.size() == 1
            }
    }

    File getAdminLogFile() {
        return new File(AdminLogger.ADMIN_LOG.privateConfig.loggerConfig.appenders.adminFile.manager.fileName)
    }

}
