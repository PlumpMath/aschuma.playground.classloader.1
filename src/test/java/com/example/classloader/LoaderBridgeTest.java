package com.example.classloader;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

// @RunWith(MockitoJUnitRunner.class)
public class LoaderBridgeTest {

   LoaderBridge bridge = null;

   @Before
   public void setup() {
      bridge = new LoaderBridge();
   }

   @Test
   public void testUrlSystemClassLoader() throws Exception {

      // given
      ClassLoader urlClassLoader = bridge
            .createClassLoader(this.getClass().getClassLoader(), ClassLoader.getSystemClassLoader(),
                  LoaderBridge.RESOURCES);

      // when
      URL url = commonsLangArrayUtilsURL(urlClassLoader);

      // then
      assertNotNull(url);
   }

   @Test
   public void testUrlSystemClassLoaderParent() throws Exception {

      // given
      ClassLoader urlClassLoader = bridge
            .createClassLoader(this.getClass().getClassLoader(), ClassLoader.getSystemClassLoader().getParent(),
                  LoaderBridge.RESOURCES);

      // when
      URL url = commonsLangArrayUtilsURL(urlClassLoader);

      // then
      assertNotNull(url);
   }

   @Test
   public void testThreadLocalClassLoader() {

      // given
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      // when
      URL url = commonsLangArrayUtilsURL(classLoader);

      // then
      assertNull(url);
   }

   @Test
   public void testLocalClassLoader() {

      // given
      ClassLoader classLoader = LoaderBridge.class.getClassLoader();

      // when
      URL url = commonsLangArrayUtilsURL(classLoader);

      // then
      assertNull(url);
   }

   @Test
   public void testNewClassLoader() throws Exception {

      // given
      ClassLoader urlClassLoader = bridge.getClassLoader();

      // when
      URL url = commonsLangArrayUtilsURL(urlClassLoader);

      // then
      assertNotNull(url);
   }

   private URL commonsLangArrayUtilsURL(ClassLoader classLoader) {
      return classLoader.getResource("com/google/common/collect/Lists.class");
   }

   @Test
   public void testProxyGuavaProxy() {

      // given

      // when
      final LoaderBridge.GuavaListsProxy guavaListsProxy = bridge.createGuavaListsProxy();

      // then
      assertNotNull(guavaListsProxy);
   }

   @Test
   public void testProxyGuavaProxyCall1() {

      // given
      final LoaderBridge.GuavaListsProxy guavaListsProxy = bridge.createGuavaListsProxy();

      // when
      final List<String> strings = guavaListsProxy.newArrayList("1", "2", "3");

      // then
      assertNotNull(strings);
      assertArrayEquals("ARRAY", new String[] { "1", "2", "3" }, strings.toArray());
   }

   @Test
   public void testProxyGuavaProxyCall2() {

      // given
      final LoaderBridge.GuavaListsProxy guavaListsProxy = bridge.createGuavaListsProxy();

      // when
      final List<String> strings = guavaListsProxy.newArrayListWithExpectedSize(1);

      // then
      assertNotNull(strings);
      assertTrue(strings.isEmpty());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testProxyGuavaProxyCall2ExceptionPropagation() {

      // given
      final LoaderBridge.GuavaListsProxy guavaListsProxy = bridge.createGuavaListsProxy();

      // when

      // then
      List<String> strings = guavaListsProxy.newArrayListWithExpectedSize(-1);
   }
}
