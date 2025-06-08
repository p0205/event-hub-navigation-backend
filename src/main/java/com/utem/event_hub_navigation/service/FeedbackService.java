package com.utem.event_hub_navigation.service;

import com.utem.event_hub_navigation.dto.FeedbackDTO;

public interface FeedbackService {

    void saveFeedback(Integer eventId,FeedbackDTO feedbackDTO);

    Boolean checkIfFeedbackExist(Integer eventId, Integer userId);

}