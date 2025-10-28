package vn.edu.iuh.fit.bookshop_be.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Random;

@Service
public class AvatarService {

    private final Cloudinary cloudinary;

    public AvatarService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String createAndUploadAvatar(String username, String email) {
        try {
            // 1. Tạo ảnh 200x200 px
            int size = 200;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 2. Màu nền ngẫu nhiên
            Color bgColor = getRandomColor();
            g.setColor(bgColor);
            g.fillRect(0, 0, size, size);

            // 3. Vẽ chữ cái đầu
            String firstLetter = username != null && !username.isEmpty()
                    ? username.substring(0, 1).toUpperCase()
                    : "U";

            g.setFont(new Font("Arial", Font.BOLD, 100));
            g.setColor(Color.WHITE);

            // Căn giữa chữ
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(firstLetter);
            int textHeight = fm.getAscent();

            int x = (size - textWidth) / 2;
            int y = (size + textHeight) / 2 - 20;

            g.drawString(firstLetter, x, y);
            g.dispose();

            // 4. Chuyển sang byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();

            // 5. Upload Cloudinary
            String folderName =  "avatars/" + email ;
            Map uploadResult = cloudinary.uploader().upload(
                    bytes,
                    ObjectUtils.asMap(
                            "folder", folderName,
                            "public_id", email + "_avatar",
                            "resource_type", "image"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo avatar: " + e.getMessage(), e);
        }
    }

    private Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}
