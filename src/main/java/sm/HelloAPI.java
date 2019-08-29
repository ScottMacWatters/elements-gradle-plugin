package sm;


import net.e6tech.elements.common.inject.Inject;
import net.e6tech.elements.common.resources.InstanceNotFoundException;
import net.e6tech.elements.common.resources.Provision;
import net.e6tech.elements.common.resources.Resources;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.beans.IntrospectionException;


@Path("/hello/")
public class HelloAPI {

    @Inject
    Provision provision;

    @Inject
    Resources resources;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("ping")
    public String ping() {
        return "ping";
    }


}