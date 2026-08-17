package com.hei.app.dto.error;

public record ErrorResponse(int status, String error, String message) {}
