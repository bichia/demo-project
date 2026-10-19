package com.example.commodity.config;

import com.example.commodity.entity.Brand;
import com.example.commodity.entity.Category;
import com.example.commodity.entity.Product;
import com.example.commodity.entity.User;
import com.example.commodity.repository.BrandRepository;
import com.example.commodity.repository.CategoryRepository;
import com.example.commodity.repository.ProductRepository;
import com.example.commodity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首次启动时初始化：账号、水果分类、产地品牌、水果商品（含商品图）
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final String IMG_API =
            "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?image_size=square&prompt=";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           BrandRepository brandRepository,
                           ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        initUsers();
        Map<String, Category> categories = initCategories();
        Map<String, Brand> brands = initBrands();
        initProducts(categories, brands);
    }

    private void initUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("123456");
        admin.setRole("ADMIN");
        userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword("123");
        user.setRole("USER");
        userRepository.save(user);
    }

    private Map<String, Category> initCategories() {
        Map<String, Category> map = new LinkedHashMap<>();
        String[][] data = {
                {"鲜果类", "🍎", "苹果、梨、桃、橙等时令鲜果", "1"},
                {"热带水果", "🥭", "芒果、榴莲、菠萝、香蕉等热带水果", "2"},
                {"莓果类", "🍓", "草莓、蓝莓、樱桃等新鲜浆果", "3"},
                {"瓜果类", "🍉", "西瓜、哈密瓜、甜瓜等消暑瓜果", "4"}
        };
        for (String[] d : data) {
            Category category = categoryRepository.findByName(d[0]).orElseGet(() -> {
                Category c = new Category();
                c.setName(d[0]);
                c.setIcon(d[1]);
                c.setDescription(d[2]);
                c.setSortOrder(Integer.parseInt(d[3]));
                c.setCreateTime(LocalDateTime.now());
                return categoryRepository.save(c);
            });
            map.put(d[0], category);
        }
        return map;
    }

    private Map<String, Brand> initBrands() {
        Map<String, Brand> map = new LinkedHashMap<>();
        String[][] data = {
                {"阿克苏", "🏔️", "新疆阿克苏冰糖心苹果产地", "1"},
                {"烟台", "🍎", "山东烟台红富士苹果产地", "2"},
                {"佳沛", "🥝", "新西兰佳沛奇异果品牌", "3"},
                {"赣南", "🍊", "江西赣南脐橙产区", "4"},
                {"海南", "🌴", "海南热带水果产区", "5"},
                {"泰国", "🇹🇭", "泰国进口热带水果", "6"},
                {"智利", "🫐", "智利进口樱桃莓果", "7"},
                {"云南", "🌺", "云南高原特色水果产区", "8"},
                {"福建", "🍐", "福建琯溪蜜柚产区", "9"},
                {"新疆吐鲁番", "🍇", "新疆吐鲁番葡萄产区", "10"},
                {"丹东", "🍓", "辽宁丹东草莓产区", "11"}
        };
        for (String[] d : data) {
            Brand brand = brandRepository.findByName(d[0]).orElseGet(() -> {
                Brand b = new Brand();
                b.setName(d[0]);
                b.setLogo(d[1]);
                b.setDescription(d[2]);
                b.setSortOrder(Integer.parseInt(d[3]));
                b.setCreateTime(LocalDateTime.now());
                return brandRepository.save(b);
            });
            map.put(d[0], brand);
        }
        return map;
    }

    private void initProducts(Map<String, Category> categories, Map<String, Brand> brands) {
        if (productRepository.count() > 0) {
            return;
        }
        // 名称, 分类, 品牌, 价格, 库存, 销量, 状态, 描述, 创建时间, 图片提示词
        Object[][] data = {
                {"阿克苏冰糖心苹果 5斤装", "鲜果类", "阿克苏", "59.90", 200, 860, 1,
                        "新疆阿克苏冰糖心，光照充足，甜度爆表，果核周围有透明糖心", "2026-09-01T09:00:00",
                        "阿克苏冰糖心苹果 鲜红色苹果堆叠 新鲜水果产品图 纯白色背景 高清质感"},
                {"烟台红富士苹果 10斤装", "鲜果类", "烟台", "49.90", 150, 720, 1,
                        "山东烟台正宗红富士，脆甜多汁，皮薄肉厚", "2026-09-02T10:30:00",
                        "烟台红富士苹果 红色苹果新鲜水果 电商产品主图 纯白色背景"},
                {"佳沛阳光金果奇异果 6个", "热带水果", "佳沛", "89.90", 120, 650, 1,
                        "新西兰佳沛金果，黄金果肉，软糯香甜，维C丰富", "2026-09-03T14:20:00",
                        "佳沛奇异果 金色果肉切开状态 猕猴桃新鲜水果 电商主图 纯白色背景"},
                {"赣南脐橙 10斤装", "鲜果类", "赣南", "39.90", 300, 1200, 1,
                        "江西赣南脐橙，皮薄汁多，酸甜可口，冬季首选", "2026-09-04T11:00:00",
                        "赣南脐橙 橙色橙子新鲜水果 电商产品主图 纯白色背景 高清"},
                {"泰国金枕榴莲 4-5斤", "热带水果", "泰国", "199.00", 80, 320, 1,
                        "泰国金枕榴莲，果肉金黄，软糯香甜，臭中带甜的极致美味", "2026-09-05T16:45:00",
                        "泰国金枕榴莲 果肉金黄切开 热带水果 电商主图 纯白色背景 质感"},
                {"海南贵妃芒 5斤装", "热带水果", "海南", "45.90", 250, 890, 1,
                        "海南贵妃芒，皮薄肉厚，核小肉多，香甜浓郁", "2026-09-06T08:15:00",
                        "海南贵妃芒芒果 黄红色芒果 热带新鲜水果 电商主图 纯白色背景"},
                {"智利车厘子 2斤礼盒", "莓果类", "智利", "128.00", 0, 540, 0,
                        "智利进口大果车厘子，J级果径，紫红饱满，已售罄", "2026-09-07T19:10:00",
                        "智利车厘子 深红色樱桃 新鲜浆果水果 电商主图 纯白色背景 高清"},
                {"丹东红颜草莓 2斤装", "莓果类", "丹东", "69.90", 180, 1500, 1,
                        "辽宁丹东红颜草莓，大果粒，甜度高，奶香浓郁", "2026-09-08T09:40:00",
                        "丹东红颜草莓 新鲜红色草莓 浆果水果 电商主图 纯白色背景 明亮"},
                {"新疆无籽葡萄 4斤装", "瓜果类", "新疆吐鲁番", "35.90", 200, 980, 1,
                        "新疆吐鲁番无籽葡萄，清甜无籽，颗颗饱满", "2026-09-09T13:25:00",
                        "新疆无籽葡萄 紫红色葡萄串 新鲜水果 电商产品主图 纯白色背景"},
                {"福建红心蜜柚 5斤装", "瓜果类", "福建", "29.90", 400, 2100, 1,
                        "福建琯溪红心蜜柚，皮薄肉红，清甜微酸，消食解腻", "2026-09-10T17:30:00",
                        "福建红心蜜柚 红色果肉切开 柚子新鲜水果 电商主图 纯白色背景"},
                {"云南蓝莓 4盒装", "莓果类", "云南", "79.90", 0, 680, 0,
                        "云南高原蓝莓，大颗粒，花青素丰富，已售罄", "2026-09-11T10:05:00",
                        "云南蓝莓 蓝色浆果新鲜 蓝莓水果 电商主图 纯白色背景"},
                {"海南麒麟西瓜 1个约8斤", "瓜果类", "海南", "38.90", 350, 3200, 1,
                        "海南麒麟西瓜，无籽沙瓤，甜度高，消暑必备", "2026-09-12T10:05:00",
                        "海南麒麟西瓜 绿皮红瓤切开 西瓜新鲜水果 电商主图 纯白色背景"}
        };
        for (Object[] d : data) {
            Product p = new Product();
            p.setName((String) d[0]);
            p.setCategory(categories.get((String) d[1]));
            p.setBrand(brands.get((String) d[2]));
            p.setPrice(new BigDecimal((String) d[3]));
            p.setStock((Integer) d[4]);
            p.setSales((Integer) d[5]);
            p.setStatus((Integer) d[6]);
            p.setDescription((String) d[7]);
            p.setCreateTime(LocalDateTime.parse((String) d[8]));
            p.setImageUrl(IMG_API + URLEncoder.encode((String) d[9], StandardCharsets.UTF_8));
            productRepository.save(p);
        }
    }
}
