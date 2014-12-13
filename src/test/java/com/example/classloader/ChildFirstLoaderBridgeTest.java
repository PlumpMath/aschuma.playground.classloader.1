package com.example.classloader;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

// @RunWith(MockitoJUnitRunner.class)
public class ChildFirstLoaderBridgeTest {

   ChildFirstLoaderBridge bridge = null;

   @Before
   public void setup() {
      bridge = new ChildFirstLoaderBridge();
   }

   @Test
   public void testUrlSystemClassLoader() throws Exception {

      // given
      ClassLoader urlClassLoader = bridge
            .createClassLoader(this.getClass().getClassLoader(), ClassLoader.getSystemClassLoader(),
                  ChildFirstLoaderBridge.RESOURCES);

      // when
      URL url = fieldUtilsURL(urlClassLoader);

      // then
      assertNotNull(url);
   }

   @Test
   public void testUrlSystemClassLoaderParent() throws Exception {

      // given
      ClassLoader urlClassLoader = bridge
            .createClassLoader(this.getClass().getClassLoader(), ClassLoader.getSystemClassLoader().getParent(),
                  ChildFirstLoaderBridge.RESOURCES);

      // when
      URL url = fieldUtilsURL(urlClassLoader);

      // then
      assertNotNull(url);
   }

   @Test
   public void testThreadLocalClassLoader() {

      // given
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      // when
      URL url = fieldUtilsURL(classLoader);

      // then
      assertNull(url);
   }

   @Test
   public void testLocalClassLoader() {

      // given
      ClassLoader classLoader = LoaderBridge.class.getClassLoader();

      // when
      URL url = fieldUtilsURL(classLoader);

      // then

      assertNull(url);
   }

   @Test
   public void testDiffClassLoader() throws Exception {

      // given
      ClassLoader urlClassLoader = bridge.getClassLoader();

      // when
      URL urlFromBridge = stringUtilsURL(urlClassLoader);
      URL urlFromCurrentLoader = stringUtilsURL(this.getClass().getClassLoader());

      // then
      assertNotNull(urlFromBridge);
      assertNotNull(urlFromCurrentLoader);

      System.out.println(urlFromBridge);
      System.out.println(urlFromCurrentLoader);

      assertFalse(urlFromCurrentLoader.equals(urlFromBridge));
   }

   @Test
   public void testNewClassLoader() throws Exception {

      // given
      ClassLoader urlClassLoader = bridge.getClassLoader();

      // when
      URL url = fieldUtilsURL(urlClassLoader);

      // then
      assertNotNull(url);
   }

   private URL stringUtilsURL(ClassLoader classLoader) {
      return classLoader.getResource("org/apache/commons/lang/StringUtils.class");
   }

   private URL fieldUtilsURL(ClassLoader classLoader) {
      return classLoader.getResource("org/apache/commons/lang/reflect/FieldUtils.class");
   }

   @Test
   public void tesStringUtilsCall() {

      // given

      // when
      final ChildFirstLoaderBridge.StringUtilsProxy proxy = bridge.createStringUtilsProxy();

      // then
      assertTrue(proxy.isAllUpperCase("ABC"));
      assertFalse(proxy.isAllUpperCase("AbC"));
   }

   @Test
   public void testLocalStringUtilsIsAllUpperCaseMethodIsNoAvailable() throws Exception {

      // when
      Method[] methods = StringUtils.class.getMethods();
      Method m = null;
      for (Method c : methods) {
         if (c.getName().equals("isAllUpperCase")) {
            m = c;
         }
      }

      // then
      assertNull(m);
   }

}
