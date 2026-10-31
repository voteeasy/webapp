package de.voteeasy.votes.validation;

import de.voteeasy.votes.VoteProposalForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

@Slf4j
public class ValidVoteProposalOptionsValidator implements ConstraintValidator<ValidVoteProposalOptions, VoteProposalForm> {
    @Override
    public void initialize(ValidVoteProposalOptions constraintAnnotation) {
        log.debug("{} validator started.", ValidVoteProposalOptions.class.getSimpleName());
    }

    @Override
    public boolean isValid(VoteProposalForm value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        List<String> selectedOptions = value.getOptions();
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return false;
        }

        return hasEmptyElementsOnly(selectedOptions);
    }

    private boolean hasEmptyElementsOnly(List<String> selectedOptions) {
        int emptyElements = 0;
        for (String selectedOption : selectedOptions) {
            if (selectedOption.isEmpty() || StringUtils.isEmpty(selectedOption) || StringUtils.isEmpty(selectedOption.trim())) {
                emptyElements++;
            }
        }
        return emptyElements != selectedOptions.size();
    }
}
