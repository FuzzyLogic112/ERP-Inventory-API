package com.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import java.util.List;

// 【启动类】：告诉服务器这是一个 Spring Boot 应用
@SpringBootApplication
public class InventoryApp {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApp.class, args);
    }
}

// ==========================================
// 1. 数据模型 (Entity)：定义数据库长什么样
// ==========================================
@Entity
class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;           // 自动生成的商品ID
    public String name;       // 商品名称
    public Integer stock;     // 当前库存数量

    // 默认构造函数
    public Product() {}
    public Product(String name, Integer stock) {
        this.name = name;
        this.stock = stock;
    }
}

// ==========================================
// 2. 数据库操作层 (Repository)：自动生成增删改查 SQL
// ==========================================
interface ProductRepo extends JpaRepository<Product, Long> {
    // 增加一个自定义查询：根据名字找商品
    Product findByName(String name);
}

// ==========================================
// 3. API 接口层 (Controller)：暴露给前端和用户的网址
// ==========================================
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
class InventoryController {
    
    private final ProductRepo repo;
    public InventoryController(ProductRepo repo) { this.repo = repo; }

    // 接口1：[GET] 查询所有库存列表
    @GetMapping("/list")
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    // 接口2：[POST] 商品入库操作 (进货)
    @PostMapping("/inbound")
    public String inbound(@RequestParam String name, @RequestParam Integer count) {
        if (count <= 0) return "❌ 入库数量必须大于0！";
        
        Product p = repo.findByName(name);
        if (p == null) {
            // 如果是新商品，直接创建
            repo.save(new Product(name, count));
            return "✅ 新商品 [" + name + "] 入库成功，当前库存：" + count;
        } else {
            // 如果是老商品，累加库存
            p.stock += count;
            repo.save(p);
            return "✅ [" + name + "] 追加库存成功，最新库存：" + p.stock;
        }
    }

    // 接口3：[POST] 商品出库操作 (发货/售出)
    @PostMapping("/outbound")
    public String outbound(@RequestParam String name, @RequestParam Integer count) {
        Product p = repo.findByName(name);
        if (p == null) {
            return "❌ 仓库中没有找到商品 [" + name + "]";
        }
        if (p.stock < count) {
            return "❌ 库存不足！[" + name + "] 仅剩 " + p.stock + " 件。";
        }
        
        p.stock -= count; // 扣减库存
        repo.save(p);
        return "📦 [" + name + "] 成功发货 " + count + " 件，剩余库存：" + p.stock;
    }
}