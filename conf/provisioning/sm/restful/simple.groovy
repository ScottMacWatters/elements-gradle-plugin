
import net.e6tech.elements.web.cxf.JaxRSServer


atom("simple_server") {
    configuration =  """
    _helloWorld.addresses:
        - "http://0.0.0.0:19001/restful/"
    _helloWorld.resources:
        - class: "sm.HelloAPI"
 """
    _helloWorld = JaxRSServer
}

