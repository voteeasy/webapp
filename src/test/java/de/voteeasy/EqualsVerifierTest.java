package de.voteeasy;

import de.voteeasy.admin.CardinalityResult;
import de.voteeasy.gathering.Gathering;
import de.voteeasy.invitation.RegisterByUserTokenForm;
import de.voteeasy.invitation.UserToken;
import de.voteeasy.participation.Participation;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.settings.ChangeNameForm;
import de.voteeasy.settings.ChangePasswordForm;
import de.voteeasy.settings.tenant.*;
import de.voteeasy.tenant.CreateTenantForm;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUser;
import de.voteeasy.user.Role;
import de.voteeasy.user.User;
import de.voteeasy.user.UserTenant;
import de.voteeasy.votes.Option;
import de.voteeasy.votes.Vote;
import de.voteeasy.votes.VoteProposal;
import de.voteeasy.votes.result.RelatedGathering;
import de.voteeasy.votes.result.VoteProposalResult;
import de.voteeasy.votes.result.VoteResultView;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class EqualsVerifierTest {
    @Test
    void classesShouldRespectBeanContract() {
        // POJO
        EqualsVerifier.forClass(VoteProposalResult.class);
        EqualsVerifier.forClass(VoteResultView.class);
        EqualsVerifier.forClass(RelatedGathering.class);
        EqualsVerifier.forClass(UserTenant.class);
        EqualsVerifier.forClass(ApplicationUser.class);
        EqualsVerifier.forClass(CreateTenantForm.class);
        EqualsVerifier.forClass(SettingType.class);
        EqualsVerifier.forClass(SettingScope.class);
        EqualsVerifier.forClass(SettingPropertyForm.class);
        EqualsVerifier.forClass(SettingKeyUsage.class);
        EqualsVerifier.forClass(ChangeNameForm.class);
        EqualsVerifier.forClass(ChangePasswordForm.class);
        EqualsVerifier.forClass(ServiceResult.class);
        EqualsVerifier.forClass(RegisterByUserTokenForm.class);
        EqualsVerifier.forClass(CardinalityResult.class);

        // database entities
        EqualsVerifier.forClass(Vote.class);
        EqualsVerifier.forClass(Tenant.class);
        EqualsVerifier.forClass(Gathering.class);
        EqualsVerifier.forClass(User.class);
        EqualsVerifier.forClass(Role.class);
        EqualsVerifier.forClass(Setting.class);
        EqualsVerifier.forClass(Option.class);
        EqualsVerifier.forClass(VoteProposal.class);
        EqualsVerifier.forClass(Participation.class);
        EqualsVerifier.forClass(UserToken.class);
    }
}
