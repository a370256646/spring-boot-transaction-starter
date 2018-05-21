## 基于springboot的分布式事务解决方案
* 使用kafka作为事件通知
* 利用redis作为分布式协调存储
* 使用mysql存储分布式日志（待完善）

## 使用前提：
* 需要有kafka服务、redis服务

##  1、需要在你的项目中写一个启动器，用来配置你的自定义事务主题与对应的事务处理实例 以下是一个例子：
/**
 * 启动配置
 *
 * @author liucheng
 * @create 2018-05-15 21:00
 **/
@Component
@Order
public class TransactionHandleRegister extends TransactionHandleRegisterImpl implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Class> handleRegisterMap = new HashMap<>();
        //增加一个参与者  test，参与操作实例 Test.class
        handleRegisterMap.put("test", Test.class);
        super.init(handleRegisterMap);
    }
}

##  2、需要在你的项目中写一个事务处理器来处理实际的事务 以下是一个例子：
/**
 * 事务 操作实例
 *
 * @author liucheng
 * @create 2018-05-15 21:00
 **/
public class Test extends TransactionService {

    @Override
    public DistributionTaskInvoke exec(Object... objects) throws Exception {
        DistributionTaskInvoke rlt = new DistributionTaskInvoke();
        //这里写你的事务代码
        System.out.println("test exec");
        return rlt;
    }
}

##  3、使用示例

/**
 * 测试入口
 * @author liucheng
 * @create 2018-05-18 17:41
 **/
@RestController
public class TestWeb {
    @Autowired
    SendEvent sendEvent;

    @RequestMapping("/send/{expects}")
    public void send(@PathVariable("expects") String expects) {
        sendEvent.send("test", Collections.singleton(expects));
    }
}