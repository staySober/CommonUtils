import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 批量多任务并行执行服务
 * 用法:
 * try (TaskList tasks = BatchTaskService.newTasks()) {
 *     tasks.add(() -> {
 *         // 任务内容1
 *     });
 *     tasks.add(() -> {
 *         // 任务内容2
 *     });
 * }
 */
public class BatchTaskService {

    private static ExecutorService executor = Executors.newFixedThreadPool(100);

    /**
     * 新建一个任务列表
     */
    public static TaskList newTasks() {
        return new TaskList();
    }

    /**
     * 设置线程池线程数
     */
    public void resetThreadCount(int threadCount) {
        executor.shutdown();
        executor = Executors.newFixedThreadPool(threadCount);
    }

    public static class TaskList implements AutoCloseable {

        /**
         * 任务列表
         */
        List<ActionWithException> tasks = new ArrayList<>();

        /**
         * 添加任务
         */
        public void add(ActionWithException task) {
            tasks.add(task);
        }

        /**
         * 并发执行所有任务并等待任务执行完毕
         */
        @Override
        public void close() {
            List<Callable<Object>> callables = new ArrayList<>();

            for (ActionWithException task : tasks) {
                callables.add(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        task.invoke();
                        return null;
                    }
                });
            }

            try {
                List<Future<Object>> futures = executor.invokeAll(callables);
                for (Future<Object> future : futures) {
                    // 阻塞方法
                    if (future.isDone()) {
                        future.get();
                    }
                }
            } catch (CancellationException ex) {
                throw ex;
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                Throwable t = ex.getCause();
                Class<? extends Throwable> exceptionClazz = t.getClass();

                if (ServiceException.class.isAssignableFrom(exceptionClazz)) {
                    ServiceException ex2 = (ServiceException)t;
                    throw new ServiceRuntimeException(ex2.getMsg(), ex2);
                }

                if (RuntimeException.class.isAssignableFrom(exceptionClazz)) {
                    throw (RuntimeException)t;
                }

                throw new RuntimeException(t);
            }
        }
    }
}


// action
public interface ActionWithException {
    void invoke() throws Exception;
}

