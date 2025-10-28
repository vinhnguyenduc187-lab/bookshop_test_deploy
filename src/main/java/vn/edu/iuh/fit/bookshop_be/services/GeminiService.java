package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.bookshop_be.models.Product;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String API_KEY;
    private final ProductService productService;

    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    public GeminiService(ProductService productService) {
        this.productService = productService;
    }

    public String chatboxAI(Map<String, String> requestBody) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        String userMessage = requestBody.get("message");
        String context = requestBody.get("context"); // Lấy ngữ cảnh từ FE

        List<Product> products = productService.getAllProducts();
        String textResponse = generateProductText(products, userMessage);

        String systemPrompt = "Bạn là một nhân viên bán sách. Hãy trả lời khách hàng một cách lịch sự, rõ ràng và hữu ích dựa trên ngữ cảnh cuộc trò chuyện trước đó:\n" +
                (context != null ? context + "\n" : "") +
                textResponse + "\nKhách hàng hỏi: " + userMessage;

        Map<String, Object> request = new HashMap<>();
        request.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text", systemPrompt)
                })
        });

        Map response = restTemplate.postForObject(GEMINI_API_URL + API_KEY, request, Map.class);
        if (response != null && response.containsKey("candidates")) {
            Map firstCandidate = (Map) ((java.util.List) response.get("candidates")).get(0);
            Map content = (Map) firstCandidate.get("content");
            java.util.List parts = (java.util.List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            String reply = (String) firstPart.get("text");
            return reply != null ? reply.replace("*", "") : "Xin lỗi, hiện tại tôi chưa thể trả lời.";
        }
        return "Xin lỗi, hiện tại tôi chưa thể trả lời.";
    }

    private String generateProductText(List<Product> products, String userMessage) {
        StringBuilder text = new StringBuilder();
        text.append("Chào mừng bạn đến với HIEUVINHbook!\n")
                .append("Hiên tại, HIEUVINHbook có những sản phẩm sau đây:\n");

        for (Product product : products) {
            text.append("- ").append(product.getProductName())
                    .append(" - Giá gốc: ").append(product.getPrice()).append(" VND")
                    .append(") - Giảm giá: ").append(product.getDiscountPercentage()).append("%")
                    .append(" - Giá bán: ").append(product.getPriceAfterDiscount()).append(" VND\n");

//                    .append(" - Tồn kho: ").append(product.getStockQuantity()).append(" cuốn\n");
        }

        text.append("Chúng tôi rất hân hạnh được phục vụ bạn. Nếu bạn cần thêm thông tin, hãy liên hệ với HIEUVINHbook nhé!\n")
                .append("Câu hỏi của bạn: ").append(userMessage);
        return text.toString();
    }

    public String askGemini(String userMessage, byte[] imageBytes) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        String systemPrompt = "Bạn là một nhân viên bán sách. Hãy trả lời khách hàng một cách lịch sự, rõ ràng và hữu ích.";
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> request = new HashMap<>();
        request.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text", systemPrompt + "\nKhách hàng hỏi: " + userMessage),
                        Map.of("inlineData", Map.of(
                                "mimeType", "image/png",
                                "data", base64Image
                        ))
                })
        });

        Map response = restTemplate.postForObject(GEMINI_API_URL + API_KEY, request, Map.class);
        if (response != null && response.containsKey("candidates")) {
            Map firstCandidate = (Map) ((java.util.List) response.get("candidates")).get(0);
            Map content = (Map) firstCandidate.get("content");
            java.util.List parts = (java.util.List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            String reply = (String) firstPart.get("text");
            // Loại bỏ tất cả các dấu * từ reply
            return reply != null ? reply.replace("*", "") : "Xin lỗi, hiện tại tôi chưa thể trả lời.";
        }
        return "Xin lỗi, hiện tại tôi chưa thể trả lời.";
    }

    public boolean isImageRelatedToBook(byte[] imageBytes) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = "Hãy kiểm tra bức ảnh này có liên quan đến sách, bìa sách, thư viện hay hoạt động đọc sách không. "
                + "Nếu có thì trả lời duy nhất 'YES', nếu không thì trả lời duy nhất 'NO'.";

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> request = new HashMap<>();
        request.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text", prompt),
                        Map.of("inlineData", Map.of(
                                "mimeType", "image/png",
                                "data", base64Image
                        ))
                })
        });

        Map response = restTemplate.postForObject(GEMINI_API_URL + API_KEY, request, Map.class);

        if (response != null && response.containsKey("candidates")) {
            Map firstCandidate = (Map) ((java.util.List) response.get("candidates")).get(0);
            Map content = (Map) firstCandidate.get("content");
            java.util.List parts = (java.util.List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            String answer = ((String) firstPart.get("text")).trim().toUpperCase();
            return answer.contains("YES");
        }
        return false;
    }
}