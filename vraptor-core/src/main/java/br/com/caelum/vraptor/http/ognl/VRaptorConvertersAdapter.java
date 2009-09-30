
package br.com.caelum.vraptor.http.ognl;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.ResourceBundle;

import ognl.TypeConverter;
import br.com.caelum.vraptor.Converter;
import br.com.caelum.vraptor.core.Converters;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.ioc.RequestScoped;

@RequestScoped
public class VRaptorConvertersAdapter implements TypeConverter {

    private final Converters converters;
    private final ResourceBundle bundle;

    public VRaptorConvertersAdapter(Converters converters, ResourceBundle bundle) {
        this.converters = converters;
        this.bundle = bundle;
    }

    @SuppressWarnings("unchecked")
    public Object convertValue(Map context, Object target, Member member, String propertyName, Object value,
            Class toType) {
        Type genericType = genericTypeToConvert(target, member);
        Class type = rawTypeOf(genericType);
        Container container = (Container) context.get(Container.class);
        Converter<?> converter = converters.to(type, container);
        if (converter == null) {
            // TODO better, validation error?
            throw new IllegalArgumentException("Cannot instantiate a converter for type " + type.getName());
        }
        return converter.convert((String) value, type, bundle);
    }

    private Type genericTypeToConvert(Object target, Member member) {
        if (member instanceof Field) {
            return extractFieldType(member);
        } else if (member instanceof Method) {
            return extractSetterMethodType(target, member);
        } else if (member == null && target.getClass().isArray()) {
            return extractArrayType(target);
        }
        // TODO better
        throw new IllegalArgumentException("Vraptor can only navigate through getter/setter methods, not " + member
                + " from " + target.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    private static Class rawTypeOf(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) genericType).getRawType();
        }
        return (Class) genericType;
    }

    private Type extractArrayType(Object target) {
        return target.getClass().getComponentType();
    }

    private Type extractFieldType(Member member) {
        return ((Field) member).getGenericType();
    }

    private Type extractSetterMethodType(Object target, Member member) {
        Method method = (Method) member;
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length != 1) {
            // TODO better
            throw new IllegalArgumentException("Vraptor can only navigate through setters with one parameter, not "
                    + member + " from " + target.getClass().getName());
        }
        return parameterTypes[0];
    }

}
