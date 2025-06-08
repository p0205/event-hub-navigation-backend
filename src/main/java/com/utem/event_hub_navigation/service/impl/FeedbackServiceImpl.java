package com.utem.event_hub_navigation.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.dto.FeedbackDTO;
import com.utem.event_hub_navigation.mapper.FeedbackMapper;
import com.utem.event_hub_navigation.model.Feedback;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.repo.FeedbackRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.service.FeedbackService;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private FeedbackRepo feedbackRepo;

    private RegistrationRepo registrationRepo;

    private FeedbackMapper feedbackMapper;

    @Autowired
    public FeedbackServiceImpl(FeedbackRepo feedbackRepo, RegistrationRepo registrationRepo,FeedbackMapper feedbackMapper){
        this.feedbackRepo = feedbackRepo;
        this.registrationRepo = registrationRepo;
        this.feedbackMapper = feedbackMapper;
    }

    @Override
    public void saveFeedback(Integer eventId, FeedbackDTO feedbackDTO) {
  
            Feedback feedback = feedbackMapper.fromDto(feedbackDTO);
            Registration registration = registrationRepo.findByEvent_IdAndParticipant_Id(eventId, feedbackDTO.getUserId());
            feedback.setRegistration(registration);
            feedback.setSubmittedAt(LocalDateTime.now());
            feedbackRepo.save(feedback);
   
    }

    @Override
    public Boolean checkIfFeedbackExist(Integer eventId, Integer userId) {
       
Boolean exist = (feedbackRepo.existsByEventIdAndUserId(eventId,userId) == 1) ? true : false;
            return exist;
     
    }
}
