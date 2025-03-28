package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.dashboards.AdminDashboardDto;
import uk.bovykina.matching_guru.security.CustomUserDetails;
import uk.bovykina.matching_guru.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            Long organisationId = userDetails.getUser().getOrganisation().getId();
            log.info("✅ Authenticated user: {}", userDetails.getUser().getEmail());
            return ResponseEntity.ok(dashboardService.getAdminDashboard(organisationId));
        }

        log.error("❌ Unable to extract CustomUserDetails from SecurityContext");
        return ResponseEntity.status(403).build();
    }
}
