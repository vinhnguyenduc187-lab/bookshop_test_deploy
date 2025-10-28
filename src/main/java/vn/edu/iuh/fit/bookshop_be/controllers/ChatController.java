package vn.edu.iuh.fit.bookshop_be.controllers;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.bookshop_be.services.GeminiService;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final GeminiService geminiService;

    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chatboxAI")
    public Map<String, String> chatboxAI(@RequestBody Map<String, String> payload) throws IOException {
        String message = payload.get("message");
        String context = payload.get("context");
        String reply = geminiService.chatboxAI(Map.of("message", message, "context", context));
        return Map.of("reply", reply);
    }

    @PostMapping(value = "/ask", consumes = {"multipart/form-data"})
    public Map<String, String> chat(
            @RequestPart("message") String message,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        byte[] imageBytes = image.getBytes();

        // Kiểm tra ảnh liên quan đến sách
        boolean isRelated = geminiService.isImageRelatedToBook(imageBytes);
        if (!isRelated) {
            return Map.of("reply", "Ảnh bạn gửi không liên quan đến sách. Vui lòng gửi ảnh có liên quan.");
        }

        // Nếu liên quan thì gọi Gemini trả lời
        String reply = geminiService.askGemini(message, imageBytes);
        return Map.of("reply", reply);
    }
}
