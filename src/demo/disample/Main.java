package demo.disample;

import demo.disample.beans.Application;
import demo.disample.sample.Product;
import demo.disample.sample.ProductService;
import demo.disample.sample.User;

class Main {

    public void runSample() throws Exception {
        Product product = new Product();
        product.setId(1);
        product.setName("car");

        User user = new User();
        user.setId(2);
        user.setUsername("admin");

        ProductService service = (ProductService) Application.getBean(ProductService.class);

        service.saveProduct(product);

        service.saveUser(user);

        service.deleteProduct(product);

        service.getUser(5L);

        service.findProductsByNameAndPrice("car", 15350.0);
    }

    public static void main(String[] args) throws Exception {
        Application.run();

        Main main = new Main();
        main.runSample();
    }
}
