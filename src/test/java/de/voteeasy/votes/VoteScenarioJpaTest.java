package de.voteeasy.votes;

import de.voteeasy.AbstractIT;
import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringRepository;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.participation.Participation;
import de.voteeasy.participation.ParticipationRepository;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VoteScenarioJpaTest extends AbstractIT {

    private String uniqueSuffixTestRun = String.valueOf(System.currentTimeMillis());

    @Autowired
    private UserRepository userRepository;
    private User u1;

    @Autowired
    private GatheringRepository gatheringRepository;
    private Gathering g1;

    @Autowired
    private VoteProposalRepository voteProposalRepository;
    private VoteProposal v1;

    @Autowired
    private OptionRepository optionRepository;
    private Option o1;
    private Option o2;
    private Option o3;

    @Autowired
    private TenantRepository tenantRepository;
    private Tenant t1;

    @Autowired
    private ParticipationRepository participationRepository;
    private Participation p1;

    @Autowired
    private GatheringService gatheringService;

    @BeforeEach
    void prepareData() {
        uniqueSuffixTestRun = String.valueOf(System.currentTimeMillis());
        Tenant t = new Tenant("Test1" + uniqueSuffixTestRun);
        t1 = tenantRepository.save(t);

        User u = new User("Test", "Test FullTest", "hashMakesNoSense", t);
        u1 = userRepository.save(u);

        Gathering g = new Gathering("Test", t1);
        g1 = gatheringService.saveWithTenantAndUser(g);

        VoteProposal v = new VoteProposal();
        Option o = new Option("Test-01", v);
        Option op = new Option("Test-02", v);
        Option opt = new Option("Test-03", v);

        v.setGathering(g);
        v.setName("TestVP");
        v.setOptions(Set.of(o, op, opt));
        v1 = voteProposalRepository.save(v);
        o1 = optionRepository.save(o);
        o2 = optionRepository.save(op);
        o3 = optionRepository.save(opt);

        p1 = participationRepository.save(new Participation(u1, g1));
    }

    @Test
    void wiringWorkedCorrectly() {
        assertThat(userRepository).isNotNull();
        assertThat(u1).isNotNull();

        assertThat(gatheringRepository).isNotNull();
        assertThat(g1).isNotNull();

        assertThat(voteProposalRepository).isNotNull();
        assertThat(v1).isNotNull();

        assertThat(optionRepository).isNotNull();
        assertThat(o1).isNotNull();
        assertThat(o2).isNotNull();
        assertThat(o3).isNotNull();

        assertThat(tenantRepository).isNotNull();
        assertThat(t1).isNotNull();

        assertThat(gatheringService).isNotNull();

        assertThat(participationRepository).isNotNull();
        assertThat(p1).isNotNull();
    }

    @Test
    void allObjectsAreInTheDatabase() {
        assertThat(gatheringRepository.findByOrganisation(t1)).isNotNull().hasSize(1).contains(g1);
        assertThat(voteProposalRepository.findByName("TestVP")).isNotEmpty();

        assertThat(optionRepository.findAll()).hasSizeGreaterThan(3);

        assertThat(optionRepository.findByNameInAndVoteProposalEquals(Set.of("Test-01", "Test-02", "Test-03"), v1, Sort.by("name"))).hasSize(3);

        assertThat(tenantRepository.findByName("Test1" + uniqueSuffixTestRun)).isPresent();
    }

    @Test
    void unknownUserWithoutTenantDoesNotSeeOtherTenantsGathering() {
        Tenant differentTenant = new Tenant("ADifferentOne" + uniqueSuffixTestRun);
        differentTenant = tenantRepository.save(differentTenant);
        assertThat(gatheringRepository.findByOrganisation(differentTenant)).isNotNull().isEmpty();

        // since nobody is logged in
        assertThat(gatheringService.getAllWithTenantCheck()).isEmpty();
    }

    @Test
    void removeCascadinglyRemoves() {
        gatheringService.deleteCascadinglyWithoutTenantCheck(g1.getId());
    }

}
