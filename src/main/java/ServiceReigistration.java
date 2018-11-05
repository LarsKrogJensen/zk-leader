import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.codehaus.jackson.map.annotate.JsonRootName;

import java.net.InetAddress;

public class ServiceReigistration {
    private static final String BASE_PATH = "/services";
    private static final String SERVICE_NAME = "notes"; 

    public static void main(String[] args) throws Exception {
        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<>(InstanceDetails.class);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client.start();
        UriSpec uriSpec = new UriSpec("{scheme}://{address}:{port}"); // Scheme, address and port
        ServiceInstance<InstanceDetails> thisInstance = ServiceInstance.<InstanceDetails>builder().name(SERVICE_NAME)
                .uriSpec(uriSpec)
                .serviceType(ServiceType.DYNAMIC)
                .address(InetAddress.getLocalHost().getHostAddress()) // Service information
                .payload(new InstanceDetails()).port(8080) // Port and payload
                .build();


        ServiceDiscovery<InstanceDetails> serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .client(client)
                .basePath(BASE_PATH).serializer(serializer).thisInstance(thisInstance)
                .build();
        serviceDiscovery.start();
        System.in.read();
        serviceDiscovery.close();
        client.close();
    }

    @JsonRootName("details")
    public static class InstanceDetails
    {
        private String        description;

        public InstanceDetails()
        {
            this("");
        }

        public InstanceDetails(String description)
        {
            this.description = description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
