package de.voteeasy.seeding;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringRepository;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import de.voteeasy.votes.Option;
import de.voteeasy.votes.OptionRepository;
import de.voteeasy.votes.VoteProposal;
import de.voteeasy.votes.VoteProposalRepository;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GatheringSeedDataCreator {

    public void bootstrapGatheringData(@NotNull GatheringRepository gatheringRepository, @NotNull GatheringService gatheringService, @NotNull UserRepository userRepository, @NotNull VoteProposalRepository voteProposalRepository, @NotNull OptionRepository optionRepository, @NotNull TenantRepository tenantRepository) {

        for (String tenant : UserSeedDataCreator.TENANTS) {
            log.info("Bootstrapping GATHERING data for {} ....", tenant);

            Optional<Tenant> dbTenant = tenantRepository.findByName(tenant);
            if (dbTenant.isEmpty()) {
                log.error("Unable to find tenant {} - will exit now.", tenant);
                System.exit(-1);
            }

            Tenant t = dbTenant.get();
            Optional<User> gongbing = userRepository.getByUserInTenant("gongbing", t);
            if (gongbing.isEmpty()) {
                log.error("Unable to find user to create gathering - will exit now.");
                System.exit(-1);
            }

            // make sure we are not trying to create seed data if it already exists
            if (gatheringRepository.findByOrganisation(t).isEmpty()) {
                Gathering g = new Gathering("Meine Vorstandsversammlung", t);
                g.setStartedBy(gongbing.get());
                g.setOrganisation(t);

                Gathering gathering = gatheringService.saveWithTenantAndUser(g);

                VoteProposal votingProposal = new VoteProposal();
                votingProposal.setCreatedAt(System.currentTimeMillis());
                votingProposal.setGathering(gathering);
                votingProposal.setItemOfAgenda("Top 1");
                votingProposal.setName("Entschluss für Verwendung der Spendengelder");
                votingProposal.setAdminAllowedToVote(true);
                votingProposal.setAnonymized(true);
                voteProposalRepository.save(votingProposal);
                createOptionsForVoteProposal(optionRepository, Arrays.asList("Ja", "Nein", "Enthaltung"), votingProposal);

                VoteProposal personProposal = new VoteProposal();
                personProposal.setCreatedAt(System.currentTimeMillis() + 1);
                personProposal.setGathering(gathering);
                personProposal.setName("Wahl des Vorsitzenden GF");
                personProposal.setItemOfAgenda("Top 2");
                personProposal.setAdminAllowedToVote(false);
                voteProposalRepository.save(personProposal);
                createOptionsForVoteProposal(optionRepository, Arrays.asList("Phil Collins", "Wayne Gretzky", "Charlotte Mahlsdorf", "Géraldine Nakache"), personProposal);

                Gathering local = new Gathering("Mitgliederversammlung", t);
                local.setStartedBy(gongbing.get());
                gatheringService.saveWithTenantAndUser(local);

                VoteProposal protokoll = new VoteProposal();
                protokoll.setCreatedAt(System.currentTimeMillis() + 1);
                protokoll.setGathering(local);
                protokoll.setName("Wahl des geheimen Protokollanten");
                protokoll.setItemOfAgenda("Top 1");
                protokoll.setAdminAllowedToVote(false);
                voteProposalRepository.save(protokoll);
                createOptionsForVoteProposal(optionRepository, Arrays.asList("A. Helper", "T. Ippse", "B. Brot"), protokoll);

                VoteProposal entlastung = new VoteProposal();
                entlastung.setCreatedAt(System.currentTimeMillis());
                entlastung.setGathering(local);
                entlastung.setItemOfAgenda("Top 2");
                entlastung.setName("Entlastung des Vorstands");
                entlastung.setAdminAllowedToVote(true);
                entlastung.setAnonymized(true);
                voteProposalRepository.save(entlastung);
                createOptionsForVoteProposal(optionRepository, Arrays.asList("Ja", "Nein", "Enthaltung"), entlastung);

                log.info("GATHERING - Bootstrapping finished for {}", tenant);
            } else {
                log.info("Seeding already took place for tenant {}", tenant);
            }
        }
    }

    private void createOptionsForVoteProposal(OptionRepository optionRepository, List<String> options, VoteProposal proposal) {
        for (String name : options) {
            optionRepository.save(new Option(name, proposal));
        }
    }

}
