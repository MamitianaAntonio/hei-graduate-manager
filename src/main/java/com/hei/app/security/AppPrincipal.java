package com.hei.app.security;

import com.hei.app.model.Role;
import java.util.UUID;

public record AppPrincipal(UUID accountId, String email, Role role) {}
