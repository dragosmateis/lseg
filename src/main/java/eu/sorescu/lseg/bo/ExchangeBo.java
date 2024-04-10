package eu.sorescu.lseg.bo;

import java.util.concurrent.ConcurrentHashMap;

public class ExchangeBo {
    public static ExchangeBo of(String id){
        return _CACHE.computeIfAbsent(id, ExchangeBo::new);
    }
    private static ConcurrentHashMap<String,ExchangeBo>_CACHE=new ConcurrentHashMap<>();

    public final String id;

    public ExchangeBo(String id) {
        this.id=id;
    }
    @Override
    public String toString() {
        return "xchg="+id;
    }
}
