package eu.sorescu.lseg.bo;

import java.util.concurrent.ConcurrentHashMap;

public class StockBo {
    public static StockBo of(String id){
        return _CACHE.computeIfAbsent(id, StockBo::new);
    }
    private static final ConcurrentHashMap<String,StockBo> _CACHE=new ConcurrentHashMap<>();

    public final String id;

    public StockBo(String id) {
        this.id=id;
    }

    @Override
    public String toString() {
        return "stock="+id;
    }
}
