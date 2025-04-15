import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.practicemanager.entity.PracticeType;
import ru.itmo.practicemanager.entity.CheckResult;
import ru.itmo.practicemanager.service.CompanyChecker;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CompanyCheckerTest {

    private final CompanyChecker companyChecker = new CompanyChecker();

    @Test
    void testInn_1207800157454_ReturnsOK() {
        CheckResult result = companyChecker.checkCompany("1207800157454", PracticeType.OFFLINE);
        assertEquals(CheckResult.OK, result, "Company with INN 1207800157454 should return OK");
    }

    @Test
    void testInn_7813645632_ReturnsOK() {
        CheckResult result = companyChecker.checkCompany("7813645632", PracticeType.OFFLINE);
        assertEquals(CheckResult.OK, result,
                "Company with INN 7813645632 should return OK");
    }

    @Test
    void testInn_7803009412_ReturnsOK() {
        CheckResult result = companyChecker.checkCompany("7803009412", PracticeType.OFFLINE);
        assertEquals(CheckResult.OK, result, "Company with INN 7803009412 should return OK");
    }

    // Тест с московской компанией, но PracticeType.ONLINE
    @Test
    void testInn_7707049388_ReturnsOK() {
        CheckResult result = companyChecker.checkCompany("7707049388", PracticeType.ONLINE);
        assertEquals(CheckResult.OK, result, "Company with INN 7707049388 should return OK");
    }

    @Test
    void testInn_7715809321_ReturnsLocationNotSuitable() {
        CheckResult result = companyChecker.checkCompany("7715809321", PracticeType.OFFLINE);
        assertEquals(CheckResult.LOCATION_NOT_SUITABLE, result,
                "Company with INN 7715809321 should return LOCATION_NOT_SUITABLE");
    }

    @Test
    void testInn_7815018468_ReturnsActivityNotSuitable() {
        CheckResult result = companyChecker.checkCompany("7815018468", PracticeType.OFFLINE);
        assertEquals(CheckResult.ACTIVITY_NOT_SUITABLE, result,
                "Company with INN 7815018468 should return ACTIVITY_NOT_SUITABLE");
    }

    @Test
    void testInn_666_ReturnsCompanyNotFound() {
        CheckResult result = companyChecker.checkCompany("666", PracticeType.OFFLINE);
        assertEquals(CheckResult.COMPANY_NOT_FOUND, result,
                "Company with INN 666 should return COMPANY_NOT_FOUND");
    }
}