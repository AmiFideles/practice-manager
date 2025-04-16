//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import ru.itmo.practicemanager.entity.PracticeType;
//import ru.itmo.practicemanager.entity.CheckStatus;
//import ru.itmo.practicemanager.repository.ActivityRepository;
//import ru.itmo.practicemanager.service.CompanyChecker;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class CompanyCheckerTest {
//    private final CompanyChecker companyChecker = new CompanyChecker();
//
//    @Test
//    void testInn_1207800157454_ReturnsOK() {
//        CheckStatus result = companyChecker.checkCompany(
//                "1207800157454", PracticeType.OFFLINE, "ООО \"ТЮН-ИТ РАЗРАБОТКА\"");
//        assertEquals(CheckStatus.OK, result, "Company with INN 1207800157454 should return OK");
//    }
//
//    @Test
//    void testInn_7813645632_ReturnsOK() {
//        CheckStatus result = companyChecker.checkCompany(
//                "7813645632", PracticeType.OFFLINE, "ООО \"КОДИНГ ТИМ\"");
//        assertEquals(CheckStatus.OK, result,
//                "Company with INN 7813645632 should return OK");
//    }
//
//    @Test
//    void testInn_7803009412_ReturnsOK() {
//        CheckStatus result = companyChecker.checkCompany(
//                "7803009412", PracticeType.OFFLINE, "ЗАО \"ДИДЖИТАЛ ДИЗАЙН\"");
//        assertEquals(CheckStatus.OK, result, "Company with INN 7803009412 should return OK");
//    }
//
//    // Тест с московской компанией, но PracticeType.ONLINE
//    @Test
//    void testInn_7707049388_ReturnsOK() {
//        CheckStatus result = companyChecker.checkCompany(
//                "7707049388", PracticeType.ONLINE, "ПАО \"РОСТЕЛЕКОМ\"");
//        assertEquals(CheckStatus.OK, result, "Company with INN 7707049388 should return OK");
//    }
//
//    @Test
//    void testInn_7715809321_ReturnsLocationNotSuitable() {
//        CheckStatus result = companyChecker.checkCompany(
//                "7715809321", PracticeType.OFFLINE, "ООО \"ПИКСЕЛЬ ПЛЮС\"");
//        assertEquals(CheckStatus.LOCATION_NOT_SUITABLE, result,
//                "Company with INN 7715809321 should return LOCATION_NOT_SUITABLE");
//    }
//
//    @Test
//    void testInn_7815018468_ReturnsActivityNotSuitable() {
//        CheckStatus result = companyChecker.checkCompany(
//                "7815018468", PracticeType.OFFLINE, "ООО \"СТОЛОВАЯ № 1\"");
//        assertEquals(CheckStatus.ACTIVITY_NOT_SUITABLE, result,
//                "Company with INN 7815018468 should return ACTIVITY_NOT_SUITABLE");
//    }
//
//    @Test
//    void testInn_7714481197_ReturnsInvalidCompanyName() {
//        CheckStatus result = companyChecker.checkCompany(
//                "7714481197", PracticeType.ONLINE, "ВК");
//        assertEquals(CheckStatus.INVALID_COMPANY_NAME, result, "Company with INN 7714481197 should return INVALID_COMPANY_NAME");
//    }
//
//    @Test
//    void testInn_666_ReturnsCompanyNotFound() {
//        CheckStatus result = companyChecker.checkCompany("666", PracticeType.OFFLINE, "ITMO");
//        assertEquals(CheckStatus.COMPANY_NOT_FOUND, result,
//                "Company with INN 666 should return COMPANY_NOT_FOUND");
//    }
//}