package com.github.data.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;

/**
 * Bean工具
 */
public class BeanUtil {

    //默认的基础类型的实例化初始值
    private static final Map<Class, Object> BaseType = new HashMap<Class, Object>() {
        {
            put(int.class, 0);
            put(long.class, 0l);
            put(short.class, 0);
            put(byte.class, 0);
            put(float.class, 0f);
            put(double.class, 0d);
            put(boolean.class, false);
            put(char.class, "");

            put(Integer.class, 0);
            put(Long.class, 0l);
            put(Short.class, 0);
            put(Byte.class, 0);
            put(Float.class, 0f);
            put(Double.class, 0d);
            put(Boolean.class, false);

            put(BigDecimal.class, 0);
        }
    };

    // 参数名发现
    private final static LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    /**
     * javaBean转map
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static Map<String, Object> bean2Map(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        try {
            // 获取javaBean的BeanInfo对象
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass(), Object.class);
            // 获取属性描述器
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                // 获取属性名
                String key = propertyDescriptor.getName();
                // 获取该属性的值
                Method readMethod = propertyDescriptor.getReadMethod();
                // 通过反射来调用javaBean定义的getName()方法
                Object value = readMethod.invoke(obj);
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


    /**
     * 读取一个bean的类型
     *
     * @return
     */
    @SneakyThrows
    public static Map<String, Class> readBeanType(Class<?> cls) {
        Map<String, Class> ret = new HashMap<>();
        // 获取javaBean的BeanInfo对象
        BeanInfo beanInfo = Introspector.getBeanInfo(cls, Object.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            ret.put(propertyDescriptor.getName(), propertyDescriptor.getPropertyType());
        }
        return ret;
    }


    /**
     * 通过map设置bean对象
     *
     * @param obj
     * @param map
     */
    @SneakyThrows
    public static void setBean(final Object obj, final Map<String, Object> map) {
        // 获取javaBean的BeanInfo对象
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass(), Object.class);
        // 获取属性描述器
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            // 获取属性名
            String key = propertyDescriptor.getName();
            if (!map.containsKey(key)) {
                continue;
            }


            // 兼容 @Accessors(chain = true)
            // 获取该属性的值
            Method writeMethod = propertyDescriptor.getWriteMethod();

            //如果为空则通过反射取出来
            if (writeMethod == null) {
                writeMethod = obj.getClass().getMethod(toMethodName(propertyDescriptor.getName()), propertyDescriptor.getPropertyType());
            }

            // 通过反射来调用javaBean定义的getName()方法
            if (writeMethod == null) {
                continue;
            }

            //调用方法
            final Class propertyType = propertyDescriptor.getPropertyType();
            Object sourceObj = map.get(key);
            if (sourceObj == null) {
                continue;
            }

            //递归类型 ( 当value 为 map)
            if (propertyType != sourceObj.getClass() && sourceObj instanceof Map) {
                Object ret = newClass(propertyType);
                setBean(ret, (Map) sourceObj);
                writeMethod.invoke(obj, ret);
            } else if (sourceObj instanceof Collection || sourceObj.getClass().isArray()) {
                writeMethod.invoke(obj, toTypeFromCollectionObject(propertyType, sourceObj));
            } else {
                writeMethod.invoke(obj, sourceObj);
            }
        }
    }

    /**
     * 转换到目标类型
     *
     * @param <T>
     * @return
     */
    private static <T> T toTypeFromCollectionObject(Class<T> targetCls, Object sourceObj) {

        //处理集合类型
        List items = null;
        if (sourceObj instanceof Collection) {
            items = (List) ((Collection) sourceObj).stream().collect(Collectors.toList());
        } else if (targetCls.isArray()) {
            items = Arrays.stream((Object[]) sourceObj).collect(Collectors.toList());
        } else {
            items = new ArrayList();
        }


        //目标类型
        if (targetCls.isArray()) {
            Object ret = Array.newInstance(targetCls.getComponentType(), items.size());
            for (int i = 0; i < items.size(); i++) {
                Array.set(ret, i, items.get(i));
            }
            return (T) ret;
        }


        if (targetCls == List.class) {
            return (T) new ArrayList(items);
        }
        if (targetCls == Set.class) {
            return (T) new HashSet<>(items);
        }
        if (targetCls == Vector.class) {
            return (T) new HashSet<>(items);
        }
//        sourceObj instanceof Map sourceObj instanceof Collections || sourceObj.getClass().isArray()

        return null;
    }

    /**
     * 转换到方法名
     *
     * @param name
     * @return
     */
    private static String toMethodName(String name) {
        return "set" + name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    /**
     * Bean转到Map
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> toMap(Object obj) {
        Map<String, Object> m = new HashMap<>();
        setMap(m, obj);
        return m;
    }


    @SneakyThrows
    public static <T> T getBeanValue(Object obj, String name) {
        // 获取javaBean的BeanInfo对象
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass(), Object.class);
        // 获取属性描述器
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            // 获取属性名
            String key = propertyDescriptor.getName();
            if (key.equals(name)) {
                Method readMethod = propertyDescriptor.getReadMethod();
                return (T) readMethod.invoke(obj, name);
            }
        }
        return null;
    }


    /**
     * bean转到Map
     *
     * @param map
     * @param obj
     */
    @SneakyThrows
    public static void setMap(final Map<String, Object> map, final Object obj) {
        //如果对象为Map，直接拷贝到目标Map中
        if (obj instanceof Map) {
            map.putAll((Map) obj);
            return;
        }
        // 获取javaBean的BeanInfo对象
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass(), Object.class);
        // 获取属性描述器
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            // 获取属性名
            String key = propertyDescriptor.getName();
            Method readMethod = propertyDescriptor.getReadMethod();
            map.put(key, readMethod.invoke(obj));
        }
    }


    /**
     * 获取bean对象中为null的属性名
     *
     * @param source
     * @return
     */
    public static void getNullPropertyNames(Object source, Set<String> sets) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) sets.add(pd.getName());
        }
    }


    /**
     * 获取方法上的参数名
     *
     * @param method
     * @return
     */
    public static String[] getParameterNames(Method method) {
        return parameterNameDiscoverer.getParameterNames(method);
    }

    /**
     * 取方法名上的参数名，优先遵循是 RequestParam 注解
     *
     * @param method
     * @param index
     * @return
     */
    public static String getMethodParamName(Method method, final int index) {
        String[] paramNames = getParameterNames(method);
        //读取参数名,优先读取注解上的参数名
        String paramName = null;
        Annotation[] parameterAnnotations = method.getParameterAnnotations()[index];
        for (Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof RequestParam) {
                RequestParam requestParam = (RequestParam) parameterAnnotation;
                return requestParam.value();
            }
        }
        if (paramName == null) {
            paramName = paramNames[index];
        }

        return paramName;
    }




    /**
     * 设置BeanMap的项
     *
     * @param ret
     * @param spaceName
     * @param itemKey
     * @param fieldCommentText
     * @param value
     */
    private static void setBeanMapItem(final Map<String, BeanValueInfo> ret, final String spaceName,
                                       final String itemKey, String fieldCommentText, Object value) {
        String key = getItemKeyName(spaceName, itemKey);
        if (StringUtils.hasText(key)) {
            ret.put(key, new BeanValueInfo(key, fieldCommentText, value));
        }
    }

    /**
     * 获取项目的key
     *
     * @param spaceName
     * @param itemKey
     * @return
     */
    private static String getItemKeyName(String spaceName, String itemKey) {
        String ret = StringUtils.hasText(spaceName) && StringUtils.hasText(itemKey) ? spaceName + "." + itemKey : itemKey;
        if (ret.indexOf(".") < 0) {
            return ret;
        }
        //根据spring controller的规则，如果有多级对象，则去掉首个
        String[] items = ret.split("\\.");
        if (items.length > 1) {
            String[] newItems = new String[items.length - 1];
            System.arraycopy(items, 1, newItems, 0, newItems.length);
            ret = TextUtil.join(newItems, ".");
        }
        return ret;
    }


    /**
     * 参数信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeanValueInfo {

        //名称
        private String name;

        //类型
        private String commentText;

        //值
        private Object value;

//        //方法
//        private String commentText;
//
//        //参数注释
//        private Map<String,String> paramCommentText;

    }




    /**
     * 实例化对象
     *
     * @param cls
     */
    @SneakyThrows
    public static <T> T newClass(Class<T> cls) {
        //基础类型,则直接返回null
        Object val = BaseType.get(cls);
        if (val != null) {
            return (T) val;
        }
        //枚举类
        if (cls.isEnum()) {
            return (T) cls.getFields()[0].get(null);
        }

        // 数组与集合
        if (cls.isArray()) {
            return (T) Array.newInstance(cls, 0);
        } else if (cls == List.class) {
            return (T) new ArrayList();
        } else if (cls == Set.class) {
            return (T) new HashSet<>();
        } else if (cls == Map.class) {
            return (T) new HashMap<>();
        }


        Constructor<?> newConstructor = null;
        //遍历出所有的构造方法
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                newConstructor = constructor;
                break;
            }
        }
        Assert.notNull(newConstructor, "未找到可用的构造方法 : " + cls);
        return (T) newConstructor.newInstance();
    }


    /**
     * 获取枚举类的class
     *
     * @param field
     * @return
     */
    public static Class[] getGenericType(Field field) {
        Type genericType = field.getGenericType();
        if (null == genericType) {
            return null;
        }
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            // 得到泛型里的class类型对象
            return Arrays.stream(pt.getActualTypeArguments()).map((it) -> {
                return (Class) it;
            }).collect(Collectors.toList()).toArray(new Class[0]);
        }
        return null;
    }




    /**
     * 获取class的属性
     *
     * @param cls
     * @param fieldName
     * @return
     */
    @SneakyThrows
    private static Field getField(Class cls, String fieldName) {
        for (Field field : cls.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field;
            }
        }
        Field field = null;
        if (cls.getSuperclass() != null) {
            field = getField(cls.getSuperclass(), fieldName);
        }
        return field;
    }



}
