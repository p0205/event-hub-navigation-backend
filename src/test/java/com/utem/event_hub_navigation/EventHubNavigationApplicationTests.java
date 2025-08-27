// package com.utem.event_hub_navigation;

// import java.util.List;
// import java.util.Map;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import com.utem.event_hub_navigation.repo.RegistrationRepo;
// import com.utem.event_hub_navigation.service.impl.EventReportServiceImpl;

// @SpringBootTest
// class EventHubNavigationApplicationTests {

// 	@Test
// 	void contextLoads() {
// 	}

// 	@Autowired
// private RegistrationRepo registrationRepo;
// @Autowired
// private EventReportServiceImpl eventService;;

// @Test
// void testGetDemographicDataGroupByFaculty() {
// 	List<Object[]> data = registrationRepo.getDemographicDataGroupByCourse(39);
//     Map<String, Long> facultyCounts = eventService.convertGraphDataType(data);
//     facultyCounts.forEach((faculty, count) -> 
//         System.out.println("Faculty: " + faculty + ", Count: " + count)
//     );
// }

// }
