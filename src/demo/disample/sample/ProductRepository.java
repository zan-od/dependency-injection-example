package demo.disample.sample;

import demo.disample.beans.CrudRepository;

import java.util.List;

public interface ProductRepository extends CrudRepository<Integer, Product> {

    List<Product> findByNameAndPrice(String name, Double price);

}
