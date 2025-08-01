package com.utem.event_hub_navigation.event;

import org.springframework.context.ApplicationEvent;

public class onVerifyEmailEvent extends ApplicationEvent {

    private final String email;


    public onVerifyEmailEvent(Object source, String email) {
        super(source);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }


}
