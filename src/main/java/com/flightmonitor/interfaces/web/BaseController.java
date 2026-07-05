package com.flightmonitor.interfaces.web;

import com.flightmonitor.infrastructure.security.UserAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;

abstract class BaseController {

    protected Long currentUserId() {
        return ((UserAuthentication) SecurityContextHolder.getContext().getAuthentication()).getUserId();
    }
}
