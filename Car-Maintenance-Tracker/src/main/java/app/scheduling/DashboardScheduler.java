package app.scheduling;

import app.user.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DashboardScheduler {

    private final UserService userService;

    public DashboardScheduler(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void refreshDashboardCacheJob() {
        userService.refreshCache();
        System.out.println("Scheduled (fixedRate) job executed: dashboard cache refreshed.");
    }
}
