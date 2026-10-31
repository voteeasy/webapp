package de.voteeasy.user;

import de.voteeasy.settings.ChangeNameForm;
import de.voteeasy.settings.ChangePasswordForm;
import de.voteeasy.serviceresult.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@Service
@Slf4j
@PreAuthorize("isAuthenticated()")
@Transactional
@Validated
public class UserService {
    private final ApplicationUserService applicationUserService;
    private final UserRepository userRepository;

    @Autowired
    public UserService(ApplicationUserService applicationUserService, UserRepository userRepository) {
        this.applicationUserService = applicationUserService;
        this.userRepository = userRepository;
    }

    public ServiceResult updateUserWithSecurityCheck(@Valid @NotNull ChangeNameForm form) {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty() || form.getUserId() == null || !form.getUserId().equals(user.get().getId())) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }
        User u = user.get();
        u.setFullName(form.getFullName().trim());
        userRepository.save(u);

        return ServiceResult.builder().messageKey("changeuser.success").status(HttpStatus.OK).build();
    }

    public ServiceResult updateUserWithSecurityCheck(@Valid @NotNull ChangePasswordForm form) {
        Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (form.getUserId() == null || user.isEmpty() || !form.getUserId().equals(user.get().getId())) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }
        User u = user.get();
        u.setPasswordHash(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(form.getPassword().trim()));
        userRepository.save(u);

        return ServiceResult.builder().messageKey("changepassword.success").status(HttpStatus.OK).build();
    }
}
