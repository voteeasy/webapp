package de.voteeasy.votes;

import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Validated
public final class VoteProposalFormMapper {

    static VoteProposal toEntity(@NonNull @Valid VoteProposalForm fromForm) {
        VoteProposal.VoteProposalBuilder voteProposalBuilder = VoteProposal.builder();

        if (fromForm.getOptions() != null && !fromForm.getOptions().isEmpty()) {
            Set<Option> options = new TreeSet<>();
            fromForm.getOptions().forEach(name -> {
                String trimmed = name.trim();
                if (!trimmed.isEmpty()) {
                    // workaround to prevent filter error as name is used as reference in views
                    options.add(new Option(removeSpecialChars(trimmed)));
                }
            });
            voteProposalBuilder.options(options);
        }

        voteProposalBuilder.name(fromForm.getName());
        voteProposalBuilder.itemOfAgenda(fromForm.getItemOfAgenda());
        voteProposalBuilder.isAnonymized(fromForm.isAnonymized());
        voteProposalBuilder.isAdminAllowedToVote(fromForm.isAdminAllowedToVote());

        return voteProposalBuilder.build();
    }

    private static String removeSpecialChars(String trimmed) {
        if (StringUtils.isNotEmpty(trimmed)) {
            return trimmed.replaceAll(",", "");
        }
        return trimmed;
    }

    static VoteProposalForm toForm(@NonNull VoteProposal fromDatabase) {
        VoteProposalForm form = new VoteProposalForm();

        if (fromDatabase.getOptions() != null) {
            form.setOptions(new TreeList<>(fromDatabase.getOptions().stream().map(Option::getName).collect(Collectors.toSet())));
        }

        form.setName(fromDatabase.getName());
        form.setItemOfAgenda(fromDatabase.getItemOfAgenda());
        form.setAnonymized(fromDatabase.isAnonymized());
        form.setAdminAllowedToVote(fromDatabase.isAdminAllowedToVote());

        return form;
    }
}
