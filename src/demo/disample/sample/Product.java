package demo.disample.sample;

import demo.disample.annotations.Column;
import demo.disample.annotations.Id;
import demo.disample.annotations.Table;

@Table(name = "products")
public class Product {

    @Id
    @Column(name = "ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PRICE")
    private double price;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString(){
        return "Product: " + getName() + " (" + getId() + ")";
    }
}
