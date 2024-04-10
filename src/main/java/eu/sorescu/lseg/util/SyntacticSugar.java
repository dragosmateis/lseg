package eu.sorescu.lseg.util;

import java.util.function.Consumer;
import java.util.function.Function;

public interface SyntacticSugar {
    static void tryRun(FlexRunnable r){
        tryRun(r,WrappedException::rethrow);
    }
    static void tryRun(FlexRunnable r, Consumer<Throwable> handler){
        try{
            r.run();
        }catch(Throwable t){}
    }

    static <T>T tryGet(FlexSupplier<T> o){
        return tryGet(o,WrappedException::rethrow);
    }

    static <T>T tryGet(FlexSupplier<T> o, Function<Throwable, T> handler){
        try{
            return o.get();
        }catch(Throwable t){
            return handler.apply(t);
        }
    }
    interface FlexRunnable{
        void run()throws Throwable;
    }
    interface FlexSupplier<T>{
        T get()throws Throwable;
    }
    class WrappedException extends RuntimeException{
        private final Throwable throwable;
        public static<T>T rethrow(Throwable t){
            throw new WrappedException(t);
        }

        public WrappedException(Throwable throwable){
            this.throwable=throwable;
        }
        public Throwable unwrap(){
            if(throwable instanceof WrappedException)return ((WrappedException) throwable).unwrap();
            return throwable;
        }
    }
}
