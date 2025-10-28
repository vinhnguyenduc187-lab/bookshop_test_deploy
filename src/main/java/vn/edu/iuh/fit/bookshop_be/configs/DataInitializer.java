package vn.edu.iuh.fit.bookshop_be.configs;

import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.iuh.fit.bookshop_be.models.Category;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.bookshop_be.repositories.EmployeeRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            // ==== DANH MỤC CHA ====
            Category vanHoc = new Category("VĂN HỌC", "Danh mục văn học");
            Category kinhTe = new Category("KINH TẾ", "Danh mục kinh tế");
            Category tamLyKyNangSong = new Category("TÂM LÝ - KỸ NĂNG SỐNG", "Danh mục tâm lý - kỹ năng sống");
            Category nuoiDayCon = new Category("NUÔI DẠY CON", "Danh mục nuôi dạy con");
            Category sachThieuNhi = new Category("SÁCH THIẾU NHI", "Danh mục sách thiếu nhi");
            Category tieuSuHoiKy = new Category("TIỂU SỬ - HỒI KÝ", "Danh mục tiểu sử - hồi ký");
            Category sachNgoaiNgu = new Category("SÁCH HỌC NGOẠI NGỮ", "Danh mục sách học ngoại ngữ");
            Category dungCuHocSinh = new Category("DỤng cụ học sinh", "Dụng cụ học sinh");
            Category butViet = new Category("BÚT - VIẾT", "Danh mục bút và viết");
            Category dungCuVanPhong = new Category("DỤNG CỤ VĂN PHÒNG", "Danh mục dụng cụ văn phòng");
            Category sachGiaoKhoa = new Category("SÁCH GIÁO KHOA", "Danh mục sách giáo khoa");
            Category doNgheDenTruong = new Category("ĐỒ NGHỀ ĐẾN TRƯỜNG", "Dụng cụ học tập đến trường");

            // ==== DANH MỤC CON ====
            // Văn Học
            Category tieuThuyet = new Category("Tiểu Thuyết", "Sách tiểu thuyết");
            Category truyenNgan = new Category("Truyện Ngắn - Tản Văn", "Sách truyện ngắn - tản văn");
            Category lightNovel = new Category("Light Novel", "Sách light novel");
            Category ngonTinh = new Category("Ngôn Tình", "Sách ngôn tình");

            // Kinh Tế
            Category nhanVat = new Category("Nhân Vật - Bài Học Kinh Doanh", "Sách kinh doanh và nhân vật");
            Category quanTri = new Category("Quản Trị - Lãnh Đạo", "Sách quản trị lãnh đạo");
            Category marketing = new Category("Marketing - Bán Hàng", "Sách marketing - bán hàng");
            Category phanTich = new Category("Phân Tích Kinh Tế", "Sách phân tích kinh tế");

            // Tâm Lý - Kỹ Năng Sống
            Category kyNangSong = new Category("Kỹ Năng Sống", "Sách kỹ năng sống");
            Category renLuyenNhanCach = new Category("Rèn Luyện Nhân Cách", "Sách rèn luyện nhân cách");
            Category tamLy = new Category("Tâm Lý", "Sách tâm lý");
            Category sachTuoiMoiLon = new Category("Sách Cho Tuổi Mới Lớn", "Sách dành cho tuổi mới lớn");

            // Nuôi Dạy Con
            Category camNangChaMe = new Category("Cẩm Nang Làm Cha Mẹ", "Sách cẩm nang cho cha mẹ");
            Category phuongPhapGd = new Category("Phương Pháp Giáo Dục Trẻ", "Phương pháp giáo dục trẻ");
            Category phatTrienTriTue = new Category("Phát Triển Trí Tuệ Cho Trẻ", "Sách phát triển trí tuệ cho trẻ");
            Category kyNangTre = new Category("Phát Triển Kỹ Năng Cho Trẻ", "Sách phát triển kỹ năng cho trẻ");

            // Sách Thiếu Nhi
            Category manga = new Category("Manga - Comic", "Sách truyện tranh, comic");
            Category kienThucBachKhoa = new Category("Kiến Thức Bách Khoa", "Sách kiến thức bách khoa");
            Category sachTranh = new Category("Sách Tranh Kỹ Năng Sống", "Sách tranh kỹ năng sống");
            Category vuaHocVuaChoi = new Category("Vừa Học - Vừa Chơi", "Sách thiếu nhi vừa học vừa chơi");

            // Tiểu Sử - Hồi Ký
            Category cauChuyen = new Category("Câu Chuyện Cuộc Đời", "Sách kể chuyện cuộc đời");
            Category chinhTri = new Category("Chính Trị", "Sách chính trị");
            // 🔥 Đổi tên để không trùng slug với "KINH TẾ"
            Category kinhTeHoiKy = new Category("Kinh Tế Hồi Ký", "Sách kinh tế hồi ký");
            Category ngheThuat = new Category("Nghệ Thuật - Giải Trí", "Sách nghệ thuật - giải trí");

            // Sách Học Ngoại Ngữ
            Category tiengAnh = new Category("Tiếng Anh", "Sách học tiếng Anh");
            Category tiengNhat = new Category("Tiếng Nhật", "Sách học tiếng Nhật");
            Category tiengHoa = new Category("Tiếng Hoa", "Sách học tiếng Hoa");
            Category tiengHan = new Category("Tiếng Hàn", "Sách học tiếng Hàn");

            // Dụng cụ học sinh
            Category gomTay = new Category("Gôm - Tẩy", "Gôm tẩy");
            Category gotButChi = new Category("Gọt Bút Chì", "Gọt bút chì");
            Category thuoc = new Category("Thước Học Sinh", "Thước");
            Category boDungCu = new Category("Bộ Dụng Cụ Học Tập", "Bộ dụng cụ học tập");

            // ==== BÚT - VIẾT ====
            Category butBi = new Category("Bút Bi - Ruột Bút Bi", "Bút bi và ruột bút bi");
            Category butGel = new Category("Bút Gel - Bút Nước", "Bút gel và bút nước");
            Category butMuc = new Category("Bút Mực - Bút Máy", "Bút mực và bút máy");
            Category butDaQuang = new Category("Bút Dạ Quang", "Bút dạ quang");
            Category butChi = new Category("Bút Chì - Ruột Bút Chì", "Bút chì và ruột bút chì");

            // ==== DỤNG CỤ VĂN PHÒNG ====
            Category biaFile = new Category("Bìa - File Hồ Sơ", "Bìa và file hồ sơ");
            Category kepGiay = new Category("Kẹp Giấy - Kẹp Bướm", "Kẹp giấy và kẹp bướm");
            Category bamKim = new Category("Đồ Bấm Kim - Kim Bấm - Gỡ Kim", "Đồ bấm kim, kim bấm và gỡ kim");
            Category camBut = new Category("Cắm Bút - Bảng Tên", "Cắm bút và bảng tên");

            // ==== SÁCH GIÁO KHOA ====
            List<Category> sachGiaoKhoaList = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                Category lop = new Category("Lớp " + i, "Sách giáo khoa lớp " + i);
                lop.setParentCategory(sachGiaoKhoa);
                sachGiaoKhoaList.add(lop);
            }
            sachGiaoKhoa.getSubCategories().addAll(sachGiaoKhoaList);

            // ==== ĐỒ NGHỀ ĐẾN TRƯỜNG ====
            Category capBaLo = new Category("Cặp - Ba Lô", "Cặp sách, ba lô học sinh");
            Category mayTinh = new Category("Máy Tính", "Máy tính học sinh");
            Category tapVo = new Category("Tập Vở", "Tập, vở học sinh");
            Category baoTapBaoSach = new Category("Bao Tập - Bao Sách", "Bao tập, bao sách");
            Category bangViet = new Category("Bảng Viết - Bông Lau Bảng", "Bảng viết, bông lau bảng");
            Category phanHopDungPhan = new Category("Phấn - Hộp Đựng Phấn", "Phấn, hộp đựng phấn");

            doNgheDenTruong.getSubCategories().addAll(List.of(
                    capBaLo, mayTinh,  tapVo, baoTapBaoSach,
                     bangViet, phanHopDungPhan
            ));
            List.of(capBaLo, mayTinh,  tapVo, baoTapBaoSach,
                            bangViet, phanHopDungPhan)
                    .forEach(c -> c.setParentCategory(doNgheDenTruong));

            // ==== LIÊN KẾT CHA - CON ====
            vanHoc.getSubCategories().addAll(List.of(tieuThuyet, truyenNgan, lightNovel, ngonTinh));
            tieuThuyet.setParentCategory(vanHoc);
            truyenNgan.setParentCategory(vanHoc);
            lightNovel.setParentCategory(vanHoc);
            ngonTinh.setParentCategory(vanHoc);

            kinhTe.getSubCategories().addAll(List.of(nhanVat, quanTri, marketing, phanTich));
            nhanVat.setParentCategory(kinhTe);
            quanTri.setParentCategory(kinhTe);
            marketing.setParentCategory(kinhTe);
            phanTich.setParentCategory(kinhTe);

            tamLyKyNangSong.getSubCategories().addAll(List.of(kyNangSong, renLuyenNhanCach, tamLy, sachTuoiMoiLon));
            kyNangSong.setParentCategory(tamLyKyNangSong);
            renLuyenNhanCach.setParentCategory(tamLyKyNangSong);
            tamLy.setParentCategory(tamLyKyNangSong);
            sachTuoiMoiLon.setParentCategory(tamLyKyNangSong);

            nuoiDayCon.getSubCategories().addAll(List.of(camNangChaMe, phuongPhapGd, phatTrienTriTue, kyNangTre));
            camNangChaMe.setParentCategory(nuoiDayCon);
            phuongPhapGd.setParentCategory(nuoiDayCon);
            phatTrienTriTue.setParentCategory(nuoiDayCon);
            kyNangTre.setParentCategory(nuoiDayCon);

            sachThieuNhi.getSubCategories().addAll(List.of(manga, kienThucBachKhoa, sachTranh, vuaHocVuaChoi));
            manga.setParentCategory(sachThieuNhi);
            kienThucBachKhoa.setParentCategory(sachThieuNhi);
            sachTranh.setParentCategory(sachThieuNhi);
            vuaHocVuaChoi.setParentCategory(sachThieuNhi);

            tieuSuHoiKy.getSubCategories().addAll(List.of(cauChuyen, chinhTri, kinhTeHoiKy, ngheThuat));
            cauChuyen.setParentCategory(tieuSuHoiKy);
            chinhTri.setParentCategory(tieuSuHoiKy);
            kinhTeHoiKy.setParentCategory(tieuSuHoiKy);
            ngheThuat.setParentCategory(tieuSuHoiKy);


            sachNgoaiNgu.getSubCategories().addAll(List.of(tiengAnh, tiengNhat, tiengHoa, tiengHan));
            tiengAnh.setParentCategory(sachNgoaiNgu);
            tiengNhat.setParentCategory(sachNgoaiNgu);
            tiengHoa.setParentCategory(sachNgoaiNgu);
            tiengHan.setParentCategory(sachNgoaiNgu);

            dungCuHocSinh.getSubCategories().addAll(List.of(gomTay, gotButChi, thuoc, boDungCu));
            gomTay.setParentCategory(dungCuHocSinh);
            gotButChi.setParentCategory(dungCuHocSinh);
            thuoc.setParentCategory(dungCuHocSinh);
            boDungCu.setParentCategory(dungCuHocSinh);

            butViet.getSubCategories().addAll(List.of(butBi, butGel, butMuc, butDaQuang, butChi));
            butBi.setParentCategory(butViet);
            butGel.setParentCategory(butViet);
            butMuc.setParentCategory(butViet);
            butDaQuang.setParentCategory(butViet);
            butChi.setParentCategory(butViet);

            dungCuVanPhong.getSubCategories().addAll(List.of(biaFile, kepGiay, bamKim, camBut));
            biaFile.setParentCategory(dungCuVanPhong);
            kepGiay.setParentCategory(dungCuVanPhong);
            bamKim.setParentCategory(dungCuVanPhong);
            camBut.setParentCategory(dungCuVanPhong);



            // ==== LƯU DATABASE ====
            categoryRepository.saveAll(List.of(
                    vanHoc, kinhTe, tamLyKyNangSong, nuoiDayCon,
                    sachThieuNhi, tieuSuHoiKy,
                    sachNgoaiNgu, dungCuHocSinh,
                    butViet, dungCuVanPhong,
                    sachGiaoKhoa, doNgheDenTruong
            ));
        }


        if (employeeRepository.count() == 0) {
            // Tạo tài khoản quản lý mặc định
            Employee admin = new Employee();
            admin.setUsername("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPasswordHash(passwordEncoder.encode("123456")); // Mật khẩu sẽ được mã hóa trong
            admin.setPhone("0123456789");
            admin.setActive(true);
            admin.setRole(Role.MANAGER);
            employeeRepository.save(admin);
        }
    }
}
