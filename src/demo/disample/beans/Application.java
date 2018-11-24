package demo.disample.beans;

public class Application {

    private static final BeanFactory beanFACTORY = new BeanFactory();

    public static void run(){
        try {
            beanFACTORY.loadBeans("demo.disample");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("APPLICATION FAILED TO START!");
        }
    }

    public static Object getBean(Class clazz){
        return beanFACTORY.getBean(clazz);
    }

}
