package demo.disample.beans;

import demo.disample.annotations.Autowired;
import demo.disample.annotations.Component;
import demo.disample.annotations.Repository;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class BeanFactory {
    private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
    private final Map<Class, Class> repositoryImplementations = new ConcurrentHashMap<>();

    public Object getBean(String beanName){
        return singletonBeans.get(beanName);
    }

    public Object getBean(Class clazz){
        return singletonBeans.get(clazz.getName());
    }

    private Object putBean(String beanName, Object bean){
        return singletonBeans.putIfAbsent(beanName, bean);
    }

    private Object putBean(Class clazz, Object bean){
        return putBean(clazz.getName(), bean);
    }

    private synchronized Object loadBean(Class clazz) throws IllegalAccessException, InstantiationException {
        String className = clazz.getName();
        if (!singletonBeans.containsKey(className)) {
            Object bean = clazz.newInstance();

            initializeBean(bean);

            putBean(className, bean);
        }

        return getBean(className);
    }

    private void initializeBean(Object bean){

    }

    public void loadBeans(String packageName) throws Exception {
        List<Class> classes = new ArrayList<>();
        for (String className : listClassNames(packageName)) {
            Class clazz = Class.forName(className);
            if (clazz.isAnnotation())
                continue;

            classes.add(clazz);
        }

        for (Class clazz: classes){
            if (clazz.isInterface()){
                if (isRepository(clazz)){
                    Class implClass = findRepositoryImplementationClass(clazz, classes);
                    if (implClass == null){
                        throw new IllegalStateException("Implementation of @Repository '" + clazz.getName() + "' could not be found. Check your configuration.");
                    }

                    repositoryImplementations.put(clazz, implClass);
                } else {
                    Object impl = getInterfaceImplementation(clazz);
                    if (impl != null){
                        putBean(clazz, impl);
                    }
                }
            } else {
                if (clazz.isAnnotationPresent(Component.class)){
                    loadBean(clazz);
                }
            }
        }

        wireBeans();
    }

    private Class findRepositoryImplementationClass(Class _interface, List<Class> classes) {
        Class rootInterface = getRepositoryAnnotatedInterface(_interface);
        if (rootInterface == null)
            throw new IllegalArgumentException("Parameter " + _interface.getName() + " is not an Repository");

        for (Class impl: classes){
            if (impl.isInterface())
                continue;

            if (rootInterface.isAssignableFrom(impl)){
                return impl;
            }
        }

        return null;
    }

    private boolean isRepository(Class _interface){
        return getRepositoryAnnotatedInterface(_interface) != null;
    }

    private Class getRepositoryAnnotatedInterface(Class _interface){
        if (_interface.isAnnotationPresent(Repository.class))
            return _interface;

        for (Class superInterface: _interface.getInterfaces()) {
            if (isRepository(superInterface))
                return superInterface;
        }

        return null;
    }

    private Object getInterfaceImplementation(Class _interface){
        if (!_interface.isInterface())
            throw new IllegalArgumentException("Parameter " + _interface.getName() + " is not an interface");

        Object bean = getBean(_interface);
        if (bean != null){
            return bean;
        }

        for (Class superclass: _interface.getInterfaces()) {
            bean = getInterfaceImplementation(superclass);
            if (bean != null)
                return bean;
        }

        return null;
    }

    private Object createRepositoryInstance(Class _interface, Class clazz) throws Exception {

        Object instance = clazz.getDeclaredConstructor().newInstance();

        ((EntityMetadata) instance).setGenericTypes(getInterfaceGenericTypes(_interface));

        return instance;
    }

    Class[] getInterfaceGenericTypes(Class _interface){
        Type[] interfaces = _interface.getGenericInterfaces();
        if (interfaces.length == 0)
            return null;

        Type genericInterface = interfaces[0];
        if (genericInterface instanceof ParameterizedType){
            Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();

            Class[] classes = new Class[genericTypes.length];
            for (int i = 0; i < genericTypes.length; i++) {
                if (genericTypes[i] instanceof Class) {
                    classes[i] = (Class) genericTypes[i];
                }
            }

            return classes;
        }

        return null;
    }

    private Object createRepositoryProxy(Class clazz, Object bean){
        InvocationHandler handler = (proxy, method, args) -> {
            //System.out.println("Invoking " + method.getName() + "(" + args.toString() + ")");

            if (method.getName().startsWith("findBy")){
                return ((EntityMetadata) bean).findByConditions(method.getName(), args);
            }

            return method.invoke(bean, args);
        };

        Object proxy = Proxy.newProxyInstance(bean.getClass().getClassLoader(), new Class[] {clazz}, handler);

        return proxy;
    }

    private void wireBeans() throws Exception {
        for (Object object : singletonBeans.values()) {
            Class clazz = object.getClass();
//            for (Field field : clazz.getDeclaredFields()) {
//                if (field.isAnnotationPresent(Autowired.class)) {
//                    field.set(object, getBean(field.getType()));
//                }
//            }

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Autowired.class)) {
                    if (method.getParameterCount() != 1)
                        throw new Exception("Method " + method.getName() + " with @Autowired annotation must have exactly one parameter");

                    Class parameterType = method.getParameterTypes()[0];
                    Object bean;
                    if (repositoryImplementations.containsKey(parameterType)){
                        Class implType = repositoryImplementations.get(parameterType);
                        Object instance = createRepositoryInstance(parameterType, implType);
                        if (instance == null){
                            throw new Exception("Error creating instance of " + implType + " type that implements " + parameterType + " interface.");
                        }

                        bean = createRepositoryProxy(parameterType, instance);
                    } else {
                        bean = getBean(parameterType);
                    }

                    if (bean == null){
                        throw new Exception("Error wiring bean: no beans found for '" + parameterType.getName() + "' type");
                    }

                    method.invoke(object, bean);
                }
            }
        }
    }

    private List<String> listClassNames(String packageName) throws IOException, URISyntaxException {
        List<String> list = new ArrayList<>();

        String path = packageName.replace('.', '/');

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while(resources.hasMoreElements()){
            URL resource = resources.nextElement();

            File folder = new File(resource.toURI());
            listClassFilesRecursive(packageName, folder, list);
        }

        return list;
    }

    private void listClassFilesRecursive(String packageName, File folder, List<String> list){
        for (File file : folder.listFiles()){
            if (file.isDirectory()){
                listClassFilesRecursive(packageName+"."+file.getName(), file, list);
                continue;
            }

            String filename = file.getName();
            if (!filename.endsWith(".class"))
                continue;

            String className = filename.substring(0, filename.lastIndexOf(".class"));
            list.add(packageName + "." + className);
        }
    }

}
