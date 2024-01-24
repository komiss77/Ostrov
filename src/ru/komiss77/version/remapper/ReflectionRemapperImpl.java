package ru.komiss77.version.remapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.mappingio.tree.MappingTree;
import ru.komiss77.Ostrov;

final class ReflectionRemapperImpl implements ReflectionRemapper {

    private final Map mappingsByObf;
    private final Map mappingsByDeobf;

    private ReflectionRemapperImpl(final Set mappings) {
        this.mappingsByObf = Collections.unmodifiableMap((Map) mappings.stream().collect(Collectors.toMap(ReflectionRemapperImpl.ClassMapping::obfName, Function.identity())));
        this.mappingsByDeobf = Collections.unmodifiableMap((Map) mappings.stream().collect(Collectors.toMap(ReflectionRemapperImpl.ClassMapping::deobfName, Function.identity())));
    }

    public String remapClassName(final String className) {
        ReflectionRemapperImpl.ClassMapping map = (ReflectionRemapperImpl.ClassMapping) this.mappingsByDeobf.get(className);

        return map == null ? className : map.obfName();
    }

    public String remapFieldName(final Class holdingClass, final String fieldName) {
        ReflectionRemapperImpl.ClassMapping clsMap = (ReflectionRemapperImpl.ClassMapping) this.mappingsByObf.get(holdingClass.getName());
        String result = clsMap == null ? fieldName : (String) clsMap.fieldsDeobfToObf().getOrDefault(fieldName, fieldName);
Ostrov.log(" ===== remapFieldName fieldName="+fieldName+" result="+result);
        return result;
    }

    public String remapMethodName(final Class holdingClass, final String methodName, final Class... paramTypes) {
        ReflectionRemapperImpl.ClassMapping clsMap = (ReflectionRemapperImpl.ClassMapping) this.mappingsByObf.get(holdingClass.getName());

        return clsMap == null ? methodName : (String) clsMap.methods().getOrDefault(methodKey(methodName, paramTypes), methodName);
    }

    private static String methodKey(final String deobfName, final Class... paramTypes) {
        return deobfName + paramsDescriptor(paramTypes);
    }

    private static String methodKey(final String deobfName, final String obfMethodDesc) {
        return deobfName + paramsDescFromMethodDesc(obfMethodDesc);
    }

    private static String paramsDescriptor(final Class... params) {
        StringBuilder builder = new StringBuilder();
        Class[] aclass = params;
        int i = params.length;

        for (int j = 0; j < i; ++j) {
            Class param = aclass[j];

            builder.append(Util.descriptorString(param));
        }

        return builder.toString();
    }

    private static String paramsDescFromMethodDesc(final String methodDescriptor) {
        String ret = methodDescriptor.substring(1);

        ret = ret.substring(0, ret.indexOf(")"));
        return ret;
    }

    static ReflectionRemapperImpl fromMappingTree(final MappingTree tree, final String fromNamespace, final String toNamespace) {
        StringPool pool = new StringPool();
        HashSet mappings = new HashSet();
        Iterator iterator = tree.getClasses().iterator();

        while (iterator.hasNext()) {
            MappingTree.ClassMapping cls = (MappingTree.ClassMapping) iterator.next();
            HashMap fields = new HashMap();
            Iterator iterator1 = cls.getFields().iterator();

            while (iterator1.hasNext()) {
                MappingTree.FieldMapping field = (MappingTree.FieldMapping) iterator1.next();

                fields.put(pool.string(field.getName(fromNamespace)), pool.string(field.getName(toNamespace)));
            }

            HashMap methods = new HashMap();
            Iterator iterator2 = cls.getMethods().iterator();

            while (iterator2.hasNext()) {
                MappingTree.MethodMapping method = (MappingTree.MethodMapping) iterator2.next();

                methods.put(pool.string(methodKey(method.getName(fromNamespace), method.getDesc(toNamespace))), pool.string(method.getName(toNamespace)));
            }

            ReflectionRemapperImpl.ClassMapping map = new ReflectionRemapperImpl.ClassMapping(cls.getName(toNamespace).replace('/', '.'), cls.getName(fromNamespace).replace('/', '.'), Collections.unmodifiableMap(fields), Collections.unmodifiableMap(methods));

            mappings.add(map);
        }

        return new ReflectionRemapperImpl(mappings);
    }

    private static final class ClassMapping {

        private final String obfName;
        private final String deobfName;
        private final Map fieldsDeobfToObf;
        private final Map methods;

        private ClassMapping(final String obfName, final String deobfName, final Map fieldsDeobfToObf, final Map methods) {
            this.obfName = obfName;
            this.deobfName = deobfName;
            this.fieldsDeobfToObf = fieldsDeobfToObf;
            this.methods = methods;
        }

        public String obfName() {
            return this.obfName;
        }

        public String deobfName() {
            return this.deobfName;
        }

        public Map fieldsDeobfToObf() {
            return this.fieldsDeobfToObf;
        }

        public Map methods() {
            return this.methods;
        }

        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            } else if (obj != null && obj.getClass() == this.getClass()) {
                ReflectionRemapperImpl.ClassMapping that = (ReflectionRemapperImpl.ClassMapping) obj;

                return Objects.equals(this.obfName, that.obfName) && Objects.equals(this.deobfName, that.deobfName) && Objects.equals(this.fieldsDeobfToObf, that.fieldsDeobfToObf) && Objects.equals(this.methods, that.methods);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.obfName, this.deobfName, this.fieldsDeobfToObf, this.methods});
        }

        public String toString() {
            return "ClassMapping[obfName=" + this.obfName + ", deobfName=" + this.deobfName + ", fieldsDeobfToObf=" + this.fieldsDeobfToObf + ", methods=" + this.methods + ']';
        }

        ClassMapping(String x0, String x1, Map x2, Map x3, Object x4) {
            this(x0, x1, x2, x3);
        }
    }
}
