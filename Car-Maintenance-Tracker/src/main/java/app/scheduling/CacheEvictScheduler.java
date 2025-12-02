package app.scheduling;

import app.car.service.CarService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheEvictScheduler {

    private final CarService carService;

    public CacheEvictScheduler(CarService carService) {
        this.carService = carService;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void refreshCarCache() {
        carService.clearAllCarCache();
    }
}
