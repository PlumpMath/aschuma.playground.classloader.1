package com.example.classloader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

public class LoaderBridge {

   static List<String> RESOURCES = Collections.unmodifiableList(Arrays.asList("guava-14.0.jar"));

   public static interface GuavaListsProxy {
      public <E> List<E> newArrayList(E... e);

      public <E> ArrayList<E> newArrayListWithExpectedSize(int estimatedSize);
   }

   private static class BridgeInvocationHandler implements InvocationHandler {

      private final ClassLoader clazzLoader;
      private final Class delegateClazz;
      private final Object target;

      BridgeInvocationHandler(final ClassLoader clazzLoader, String clazzName, boolean newInstance) {
         this.clazzLoader = clazzLoader;
         try {
            delegateClazz = clazzLoader.loadClass(clazzName);
            target = newInstance ? delegateClazz.newInstance() : null;
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }

      ;

      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
         return callWithLoader(clazzLoader, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
               try {
                  Method m = delegateClazz.getMethod(method.getName(), method.getParameterTypes());
                  return m.invoke(target, args);
               } catch (InvocationTargetException ite) {
                  if (ite.getTargetException() instanceof RuntimeException) {
                     throw (RuntimeException) ite.getTargetException();
                  } else if (ite.getTargetException() instanceof Error) {
                     throw (Error) ite.getTargetException();
                  } else {
                     throw new RuntimeException(ite.getTargetException());
                  }
               }
            }
         });
      }
   }

   private final static Semaphore LOCK_SEMA = new Semaphore(1, true);

   private volatile ClassLoader loaderOfTheOtherWorld;

   LoaderBridge() {
   }

   static ClassLoader createClassLoader(ClassLoader resourceloader, ClassLoader root, List<String> resources) {

      List<URL> urls = new ArrayList<URL>();
      for (String resource : resources) {

         final URL urlResource = resourceloader.getResource(resource);
         urls.add(urlResource);
      }
      URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), root);

      return urlClassLoader;
   }

   static <T> T callWithLoader(ClassLoader loader, Callable<T> callable) throws Exception {

      ClassLoader current = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(loader);
      try {
         return callable.call();
      } finally {
         Thread.currentThread().setContextClassLoader(current);
      }
   }

   public GuavaListsProxy createGuavaListsProxy() {

      final InvocationHandler invocationHandler = new BridgeInvocationHandler(getClassLoader(),
            "com.google.common.collect.Lists", false);
      GuavaListsProxy proxy = (GuavaListsProxy) Proxy
            .newProxyInstance(this.getClass().getClassLoader(), new Class[] { GuavaListsProxy.class },
                  invocationHandler);

      return proxy;
   }

   ClassLoader getClassLoader() {

      LOCK_SEMA.acquireUninterruptibly();
      try {
         if (loaderOfTheOtherWorld == null) {
            loaderOfTheOtherWorld = createClassLoader(this.getClass().getClassLoader(),
                  ClassLoader.getSystemClassLoader(), RESOURCES);
         }
         return loaderOfTheOtherWorld;
      } finally {
         LOCK_SEMA.release();
      }
   }

}
