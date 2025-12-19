package reproducer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import reproducer.service.AdminLogService
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ActiveProfiles('applog-only-test')
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationLogOnlyFunctionalSpec extends Specification {

    private static final Logger APPLICATION_LOGGER = LogManager.getLogger('reproducer')

    @Autowired
    ApplicationContext applicationContext

    void 'application log - ok'() {
        given:
            String message = "app-msg-${UUID.randomUUID()}"

        when:
            APPLICATION_LOGGER.info(message)

        then:
            new PollingConditions().eventually {
                applicationLogFile.readLines().findAll { it.contains(message) }.size() == 1
            }
    }

    void 'configuration sensitive bean is not present in Spring context - ok'() {
        when:
            applicationContext.getBean(AdminLogService)

        then:
            thrown NoSuchBeanDefinitionException
    }

    private File getApplicationLogFile() {
        return new File(APPLICATION_LOGGER.privateConfig.loggerConfig.appenders.customFile.manager.fileName)
    }
}
