package de.voteeasy.participation;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class ParticipationService {
    private final ApplicationUserService applicationUserService;
    private final ParticipationRepository participationRepository;

    @Autowired
    public ParticipationService(ApplicationUserService applicationUserService, ParticipationRepository participationRepository) {
        this.applicationUserService = applicationUserService;
        this.participationRepository = participationRepository;
    }

    @Transactional(readOnly = true)
    Set<Participation> getAllParticipations() {
        Optional<User> userFromDatabase = applicationUserService.currentlyLoggedInUser();
        if (userFromDatabase.isPresent()) {
            return participationRepository.findByOwnerOrderByStartedAtDesc(userFromDatabase.get());
        }

        return Collections.emptySet();
    }

    @Transactional(readOnly = true)
    public Set<Participant> getParticipants() {
        Optional<Participation> currentParticipation = getCurrentParticipation();
        if (currentParticipation.isEmpty()) {
            return Collections.emptySet();
        }
        return participationRepository.findAllByEventAndOwnerAndStoppedAtIsNull(currentParticipation.get().getEvent(), currentParticipation.get().getOwner().getOrganisation());
    }

    @Transactional(readOnly = true)
    public Optional<Participation> getCurrentParticipation() {
        Optional<User> userFromDatabase = applicationUserService.currentlyLoggedInUser();
        if (userFromDatabase.isPresent()) {
            final Set<Participation> participations = participationRepository.findByOwnerAndStoppedAtIsNull(userFromDatabase.get());
            if (participations.size() > 1) {
                log.warn("User {} has more than one active participation, will be fixed automatically upon logout.", userFromDatabase.get().getId());
            }
            return participations.stream().findFirst();
        }

        return Optional.empty();
    }

    ServiceResult participateIn(Optional<Gathering> gathering) {
        if (gathering.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("participation.notWorking").build();
        }

        Optional<User> userFromDatabase = applicationUserService.currentlyLoggedInUser();
        if (userFromDatabase.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("participation.notWorking").build();
        }

        finishActiveParticipation(userFromDatabase.get());
        return createNewParticipation(gathering.get(), userFromDatabase.get());
    }

    ServiceResult leave() {
        Optional<User> userFromDatabase = applicationUserService.currentlyLoggedInUser();
        if (userFromDatabase.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("participation.leave.notWorking").build();
        }
        return leave(userFromDatabase.get());
    }

    public ServiceResult leave(User user) {
        finishActiveParticipation(user);
        log.debug("User {} left a meeting.", user);
        return ServiceResult.builder().status(HttpStatus.OK).messageKey("participation.leave.success").build();
    }

    private ServiceResult createNewParticipation(Gathering gathering, User user) {
        participationRepository.save(new Participation(user, gathering));
        return ServiceResult.builder().status(HttpStatus.OK).messageKey("participation.success").build();
    }

    private void finishActiveParticipation(User user) {
        final Set<Participation> openParticipations = participationRepository.findByOwnerAndStoppedAtIsNull(user);
        if (openParticipations.isEmpty()) {
            log.debug("User does not take part in any meetings yet.");
            return;
        }

        for (Participation participation : openParticipations) {
            participation.setStoppedAt(System.currentTimeMillis());
            participationRepository.save(participation);
        }
    }

    @Transactional
    public void deleteFor(@NotNull Gathering g) {
        log.info("Removing {} participation entries for gathering with id {}", participationRepository.deleteAllByEvent(g), g.getId());
    }

}
