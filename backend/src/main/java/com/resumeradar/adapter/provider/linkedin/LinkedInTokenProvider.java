package com.resumeradar.adapter.provider.linkedin;

import com.resumeradar.entity.UserEntity;
import com.resumeradar.port.outbound.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class LinkedInTokenProvider {

    private final UserRepository userRepository;

    public LinkedInTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof OAuth2User oAuth2User)) {
            throw new IllegalStateException("No authenticated user found");
        }

        String linkedInId = oAuth2User.getAttribute("sub");
        UserEntity user = userRepository.findByLinkedInId(linkedInId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + linkedInId));

        return user.getAccessToken();
    }
}
