package com.hei.app.security;

import com.hei.app.model.UserAccount;
import com.hei.app.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserAccountRepository userAccountRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UserAccount account =
        userAccountRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));
    AppPrincipal principal =
        new AppPrincipal(account.getId(), account.getEmail(), account.getRole());
    return new AppUserDetails(principal, account.getPasswordHash(), account.isActive());
  }
}
