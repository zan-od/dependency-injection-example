package demo.disample.sample;

import demo.disample.annotations.Autowired;
import demo.disample.annotations.Component;

import java.util.List;

@Component
public class ProductService {

    private ProductRepository productRepository;
    private UserRepository userRepository;

    @Autowired
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Product saveProduct(Product product) throws Exception {
        return productRepository.save(product);
    }

    public void deleteProduct(Product product){
        productRepository.delete(product.getId());
    }

    public List<Product> findProductsByNameAndPrice(String name, Double price){
        return productRepository.findByNameAndPrice(name, price);
    }

    public User saveUser(User user) throws Exception {
        return userRepository.save(user);
    }

    public User getUser(Long id){
        return userRepository.getOne(id);
    }
}
