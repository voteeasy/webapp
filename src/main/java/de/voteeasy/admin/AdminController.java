package de.voteeasy.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j
public class AdminController {
    private final AdminService adminService;
    private final VersionService versionService;

    @Autowired
    public AdminController(AdminService adminService, VersionService versionService) {
        this.adminService = adminService;
        this.versionService = versionService;
    }

    @RequestMapping(method = RequestMethod.GET, value = {
            "/cardinality",
            "/cardinality/{tenantId}"
    })
    public ModelAndView showCardinality(@PathVariable Optional<UUID> tenantId) {
        CardinalityResult result = tenantId.isEmpty() ? adminService.calculateCardinalities() : adminService.calculateCardinalities(tenantId.get());

        ModelAndView modelAndView = new ModelAndView("admin/cardinality");
        modelAndView.addObject("cardinalityMap", result.getTableCardinalities());
        // TODO add extra endpoint to retrieve piechart data asynchronously - https://github.com/ottlinger/hornherzogen/blob/master/admin/index.php#L214
        modelAndView.addObject("cardinalityMapChart", result.getAsChart());
        modelAndView.addObject("total", result.calculateAndGetTotal());

        return modelAndView;
    }

    @RequestMapping("/version")
    public ModelAndView showVersionInformation() {
        ModelAndView modelAndView = new ModelAndView("admin/version");
        modelAndView.addObject("versioninformation", versionService.getBuildProperties());
        modelAndView.addObject("gitinformation", versionService.getGitProperties());
        return modelAndView;
    }

}
