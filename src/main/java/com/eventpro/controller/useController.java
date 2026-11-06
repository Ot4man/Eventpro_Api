package com.eventpro.controller;

import com.eventpro.service.UserService;

@Deprecated
public class useController extends UserController {
    public useController(UserService userService) {
        super(userService);
    }
}
