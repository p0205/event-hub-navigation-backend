package com.utem.event_hub_navigation;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.utem.event_hub_navigation.dto.DemographicDataRow;
import com.utem.event_hub_navigation.repo.RegistrationRepo;

@SpringBootTest
class EventHubNavigationApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
private RegistrationRepo registrationRepo;

@Test
void testGetDemographicDataGroupByFaculty() {
    List<DemographicDataRow> rows = registrationRepo.getDemographicDataGroupByGender(39);
    rows.forEach(System.out::println);
}

}
