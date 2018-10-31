import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.stream.IntStream;

public class LeaderLatchTest {

    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client.start();

        IntStream.range(0, 100).forEach(id -> startLatch(client, id));

        System.in.read();
        client.close();
    }

    private static void startLatch(CuratorFramework client, final int id)  {
        try {
            final LeaderLatch latch = new LeaderLatch(client, "/leader/" + id, "id-1");
            latch.start();
            latch.addListener(new LeaderLatchListener() {
                public void isLeader() {
                    System.out.println("now leader, latch path: " + id);
                }

                public void notLeader() {
                    System.out.println("not leader");
                }
            });
//            latch.hasLeadership()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
