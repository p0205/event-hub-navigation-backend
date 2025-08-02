package com.utem.event_hub_navigation.service;

import com.utem.event_hub_navigation.model.EmailVerificationCode;

public interface VerificationCodeService {

    EmailVerificationCode generateAndSaveVerificationCode(String email);

    boolean verifyCode(String email, String code);

    boolean isVerified(String email);

}