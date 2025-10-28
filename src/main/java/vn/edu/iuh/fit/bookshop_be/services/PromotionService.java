package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.models.Promotion;
import vn.edu.iuh.fit.bookshop_be.repositories.PromotionRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final NotificationService notificationService;

    public PromotionService(PromotionRepository promotionRepository, NotificationService notificationService) {
        this.promotionRepository = promotionRepository;
        this.notificationService = notificationService;
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Promotion getPromotionById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
    }

    private String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }


    public Promotion createPromotion(Promotion promotion) {
        notificationService.createNotification(
                promotion.getName(),
                "Hãy nhập code '"+ promotion.getCode() + "' để giảm " + promotion.getDiscountPercent() + "% hóa đơn từ ngày "
                        + formatDate(promotion.getStartDate())
                        + " đến ngày "
                        + formatDate(promotion.getEndDate())
        );
        return promotionRepository.save(promotion);
    }

    public Promotion updatePromotion(Integer id, Promotion promotion) {
        if (promotionRepository.existsById(id)) {
            promotion.setId(id);
            return promotionRepository.save(promotion);
        }
        return null;
    }

    public void deletePromotion(Integer id) {
        if (promotionRepository.existsById(id)) {
            promotionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Promotion not found with id: " + id);
        }
    }

    public Promotion findByCode(String code) {
        return promotionRepository.findByCode(code);
    }






}
