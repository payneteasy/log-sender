package com.payneteasy.logsender.application;

import net.minidev.asm.BeansAccess;
import org.junit.Assert;
import org.junit.Test;

/**
 * accessors-smart (mockserver -> json-path -> json-smart) was built against ASM 5.0.4 and generates bean accessor
 * classes with ASM at runtime; check that this still works with ASM pinned to 9.11.1 in the parent pom.
 */
public class AccessorsSmartAsmTest {

    public static class Bean {
        public String name;
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    @Test
    public void generatesBeanAccessorWithAsm() {
        BeansAccess<Bean> access = BeansAccess.get(Bean.class);
        Bean bean = access.newInstance();

        access.set(bean, "name", "log-sender");
        access.set(bean, "count", 42);

        Assert.assertEquals("log-sender", bean.name);
        Assert.assertEquals(42, bean.getCount());
        Assert.assertEquals("log-sender", access.get(bean, "name"));
        Assert.assertEquals(42, access.get(bean, "count"));
    }
}
