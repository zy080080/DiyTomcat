package con.zzy.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import con.zzy.diytomcat.util.ServerXMLUtil;

import java.util.List;

public class Service {
    private String name;
    private Engine engine;
    private Server server;
    private List<Connector> connecotrs;

    public Service(Server server){
        this.server = server;
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine(this);
        this.connecotrs = ServerXMLUtil.getConnectors(this);
    }

    public void start(){
        init();
    }

    private void init(){
        TimeInterval timeInterva = DateUtil.timer();
        for(Connector c : connecotrs){
            c.init();
        }
        LogFactory.get().info("Initialization processed in {} ms", timeInterva.intervalMs());
        for(Connector c : connecotrs){
            c.start();
        }
    }

    public Engine getEngine(){
        return engine;
    }
}
