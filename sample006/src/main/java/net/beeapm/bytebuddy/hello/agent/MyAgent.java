package net.beeapm.bytebuddy.hello.agent;

import net.beeapm.bytebuddy.hello.interceptor.InstanceMethodSpendAdviceInterceptor;
import net.beeapm.bytebuddy.hello.interceptor.InstanceMethodSpendAdviceInterceptor2;
import net.beeapm.bytebuddy.hello.interceptor.StaticMethodSpendAdviceInterceptor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

/**
 * Created by yuan on 2018/4/1.
 */
public class MyAgent {
    /**
     * @param arguments
     * @param inst
     */
    public static void agentmain(String arguments, Instrumentation inst) {
        System.out.println("Hey, look: I'm instrumenting a running JVM!");
    }
    public static void premain(String arguments, Instrumentation inst) throws Exception{
        System.out.println("\n----this is an bytebuddy sample -----");

        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                    TypeDescription typeDescription,
                                                    ClassLoader classLoader, JavaModule javaModule){
                String className = typeDescription.getCanonicalName();
                System.out.println("++++++++ class name = " + className);
                try {
                    //入参长度为1的任何方法
                    //builder = builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.named("sayGood")));
                    //ElementMatchers.declaresMethod(ElementMatchers.<MethodDescription>any());   //ElementMatchers.<MethodDescription>any()
                    //builder = builder.method(ElementMatchers.<MethodDescription>any())
                    //        .intercept(Advice.to(InstanceMethodSpendAdviceInterceptor.class));
                    //只拦截DeclaredMethod方法
                    builder = builder.visit(Advice.to(InstanceMethodSpendAdviceInterceptor.class).on(ElementMatchers.isMethod().and(ElementMatchers.not(ElementMatchers.<MethodDescription>isStatic()))));
                    builder = builder.visit(Advice.to(InstanceMethodSpendAdviceInterceptor2.class).on(ElementMatchers.isMethod().and(ElementMatchers.not(ElementMatchers.<MethodDescription>isStatic()))));

                    //拦截所有的，包括从父类继承过来的
                    builder = builder.method(ElementMatchers.named("print").and(ElementMatchers.takesArguments(1)))
                            .intercept(Advice.to(StaticMethodSpendAdviceInterceptor.class));
                }catch (Exception e){

                }
                return builder;
            }
        };
        new AgentBuilder.Default()
                .with(new MyListener())
                //.with(AgentBuilder.Listener.StreamWriting.toSystemError())
                .ignore(ElementMatchers.<TypeDescription>none().and(ElementMatchers.nameStartsWith("net.bytebuddy.")))
                .type(ElementMatchers.nameStartsWith("net.beeapm.bytebuddy.hello.sample"))
                .transform(transformer).installOn(inst);
        //agentBuilder.installOn(inst);
    }

    private static class MyListener implements AgentBuilder.Listener {
        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                                     boolean loaded, DynamicType dynamicType) {
            //修改后的类输出
            WeavingClassLog.INSTANCE.log(typeDescription, dynamicType);
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                              boolean loaded) {
        }

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
                            Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        }
    }

}


