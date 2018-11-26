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
        product.setPrice(20000.0);

        User user = new User();
        user.setId(2);
        user.setUsername("admin");

        User newUser = new User();
        newUser.setUsername("new user");

        ProductService service = (ProductService) Application.getBean(ProductService.class);

        service.saveProduct(product);
        service.deleteProduct(product);

        service.findProductsByNameAndPrice("car", 15350.0);
        service.findProducts1(10.3, 21.0);
        service.findProducts2(10.3, "wire");

        service.saveUser(user);
        service.saveUser(newUser);
        service.getUser(5L);
    }

    public static void main(String[] args) throws Exception {
        Application.run();

        Main main = new Main();
        main.runSample();
    }
}
