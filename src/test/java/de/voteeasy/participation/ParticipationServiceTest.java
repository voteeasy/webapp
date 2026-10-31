package de.voteeasy.participation;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

// TODO #44 add more tests
@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private ParticipationRepository participationRepository;
    @InjectMocks
    private ParticipationService participationService;

    @Test
    void wiringWorks() {
        assertThat(participationService).isNotNull();
    }

    @Test
    void getParticipantsWithoutUser() {
        assertThat(participationService.getParticipants()).isEmpty();
        verifyNoInteractions(participationRepository);
    }

    @Test
    void getParticipantsWithUser() {
        Tenant tenant = new Tenant("getParticipantsWithUser");
        tenant.setId(UUID.randomUUID());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("getParticipantsWithUser");
        user.setOrganisation(tenant);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));
        Participation participation = new Participation();
        participation.setOwner(user);
        Gathering g = new Gathering();
        g.setName("G-getParticipantsWithUser");
        participation.setEvent(g);

        Participant participant = new Participant("G-getParticipantsWithUser", 4711L, "userName", "fullName");

        when(participationRepository.findByOwnerAndStoppedAtIsNull(eq(user))).thenReturn(Set.of(participation));

        when(participationRepository.findAllByEventAndOwnerAndStoppedAtIsNull(eq(g), eq(tenant))).thenReturn(Set.of(participant));

        assertThat(participationService.getParticipants()).containsOnly(participant);
        verify(participationRepository).findAllByEventAndOwnerAndStoppedAtIsNull(eq(g), eq(tenant));
    }

    @Test
    void getAllParticipationsWithoutUser() {
        assertThat(participationService.getAllParticipations()).isEmpty();
        verifyNoInteractions(participationRepository);
    }

    @Test
    void getAllParticipationsWithUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("getAllParticipationsWithUser");
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));
        Participation participation = new Participation();
        participation.setOwner(user);
        when(participationRepository.findByOwnerOrderByStartedAtDesc(eq(user))).thenReturn(Set.of(participation));

        assertThat(participationService.getAllParticipations()).containsOnly(participation);
    }

    @Test
    void getCurrentParticipationWithoutUser() {
        assertThat(participationService.getCurrentParticipation()).isEmpty();
        verifyNoInteractions(participationRepository);
    }

    @Test
    void getCurrentParticipation() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("getCurrentParticipation");
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        Participation p1 = new Participation();
        p1.setId(UUID.randomUUID());
        p1.setOwner(user);
        Participation p2 = new Participation();
        p2.setId(UUID.randomUUID());
        p2.setOwner(user);
        when(participationRepository.findByOwnerAndStoppedAtIsNull(eq(user))).thenReturn(Set.of(p1, p2));

        final Optional<Participation> currentParticipation = participationService.getCurrentParticipation();
        assertThat(currentParticipation).isNotEmpty();
        // as a set is not stable in its order either of the given participations is returned
        assertThat(currentParticipation.get().equals(p1) || currentParticipation.get().equals(p2)).isTrue();
    }

    @Test
    void participateInWithoutExistingParticipation() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("participateInWithoutExistingParticipation");
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        Gathering gathering = new Gathering();
        gathering.setId(UUID.randomUUID());

        final ServiceResult serviceResult = participationService.participateIn(Optional.of(gathering));
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(OK);
        assertThat(serviceResult.getMessageKey()).isEqualTo("participation.success");
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    void participateInWithoutGathering() {
        final ServiceResult result = participationService.participateIn(Optional.empty());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("participation.notWorking");
        verifyNoInteractions(participationRepository);
    }

    @Test
    void participateInWithGatheringButWithoutUser() {
        final ServiceResult result = participationService.participateIn(Optional.of(new Gathering()));
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("participation.notWorking");
        verifyNoInteractions(participationRepository);
    }

    @Test
    void leaveWithExistingParticipation() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("leaveWithExistingParticipation");
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        Participation p1 = mock(Participation.class);
        Participation p2 = mock(Participation.class);
        when(participationRepository.findByOwnerAndStoppedAtIsNull(eq(user))).thenReturn(Set.of(p1, p2));

        final ServiceResult serviceResult = participationService.leave();
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(OK);
        assertThat(serviceResult.getMessageKey()).isEqualTo("participation.leave.success");

        // make sure all participations are properly saved and get a stoppedAtDate
        verify(p1).setStoppedAt(anyLong());
        verify(p2).setStoppedAt(anyLong());
        verify(participationRepository, times(2)).save(any(Participation.class));
        verify(participationRepository).save(eq(p1));
        verify(participationRepository).save(eq(p2));
    }

    @Test
    void leaveWithoutBeingLoggedIn() {
        final ServiceResult serviceResult = participationService.leave();
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(serviceResult.getMessageKey()).isEqualTo("participation.leave.notWorking");
    }

    @Test
    void leaveForAGivenUnknownUser() {
        final ServiceResult result = participationService.leave(new User());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OK);
        assertThat(result.getMessageKey()).isEqualTo("participation.leave.success");
    }

    @Test
    void deleteFor() {
        Gathering gathering = new Gathering();
        gathering.setId(UUID.randomUUID());

        assertDoesNotThrow(() -> participationService.deleteFor(gathering));
        verify(participationRepository).deleteAllByEvent(eq(gathering));
    }
}